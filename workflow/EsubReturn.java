//由 eEub 關班清單產生扣件清單

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import sip.util.*;
import common.*;


class EsubReturn {
	static String qcOk = "2";
	static String cancelSending = "5";
	static String backToTGL = "4";


	static boolean sqlLogEnabled = true;

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 1) {
			rc = (new EsubReturn()).doEraList(args[0]);
		}
		else {
			System.out.println("EsubReturn <esub_return_xml>");
			rc += 15;
		}
		System.exit(rc);
	}

	String _connName = "@jdbc_TGL";
	String _fmtTblName = "t_fmt_document";

	String _sqlSelect = "select d.POLICY_CODE, f.PRINTID from T_DOCUMENT d JOIN " 
		+ _fmtTblName  + " f ON d.LIST_ID=f.LIST_ID " 
		+ "where f.HOSTCODE=? and f.SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";

	String _sqlUpdate = "update " + _fmtTblName + " set STAGE=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?"; 

	String _vpName = "VP_TO_BET";	

	public int doEraList (String esubXml) {
		int rc = 0;

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Connection conn = null;
   		PreparedStatement stmtSelect = null;
   		PreparedStatement stmtUpdate = null;
   		ResultSet result = null;

   		String printId_BatchId = null;

   		try {
			conn = (new Sql(_connName)).getConn();
			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);
			stmtUpdate = sqlLogEnabled? new LoggableStatement(conn, _sqlUpdate) : conn.prepareStatement(_sqlUpdate);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document dom = db.parse(new File(esubXml));

			NodeList pipList =  dom.getElementsByTagName("Pip");
			if (pipList != null && pipList.getLength() > 0) {
				for (int i=0; i<pipList.getLength(); ++i) {
					Node pip = pipList.item(i);
					//if (!"2".equals(Xml.getNodeText(pip,"QcResult"))) {//not QC ok
					String qcResult = Xml.getNodeText(pip,"QcResult");
					//boolean qcOk = "2".equals(Xml.getNodeText(pip,"QcResult")); 
						String pipPk = Xml.getNodeText(pip,"PipPk");
						try {
							int idx = pipPk.indexOf('_');
							String hostCode = pipPk.substring(0,idx);
							pipPk = pipPk.substring(idx+1);
							idx = pipPk.lastIndexOf('_');
							int subjobNo = Integer.parseInt(pipPk.substring(idx+1), 16);
							pipPk = pipPk.substring(0,idx);
							idx = pipPk.lastIndexOf('_');
							long jobNo = Long.parseLong(pipPk.substring(idx+1), 16);
							String spoolName = pipPk.substring(0,idx);
						  if (!qcOk.equals(qcResult) || printId_BatchId==null) {
							stmtSelect.setString(1, hostCode);
							stmtSelect.setString(2, spoolName);
							stmtSelect.setLong(3, jobNo);
							stmtSelect.setInt(4, subjobNo);

							result = stmtSelect.executeQuery();
							if (result.next()) {
								String policyCode = result.getString(1);
								String printId = result.getString(2);
								System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, policyCode=%s, printId=%s%n",  sdf.format(new java.util.Date()), 
	 									hostCode, spoolName, jobNo, subjobNo, policyCode, printId);
								if (null!=printId) {
								idx = printId.indexOf(',');
									if (idx > 0) {
										if (!qcOk.equals(qcResult)) {
											if (!addEra(printId_BatchId = printId.substring(0,idx), printId.substring(idx+1), policyCode, backToTGL.equals(qcResult)? "Y":"N")) rc = 21;
										}
										else {
											printId_BatchId = printId.substring(0,idx);
											addEra(printId_BatchId,null,null,null);
										}
									}
								}
							} else {
								System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d not found%n",  sdf.format(new java.util.Date()), 
	 									hostCode, spoolName, jobNo, subjobNo);
								if(rc==0) rc = 11;
							}
							if (result != null) { result.close(); result = null; }
						  }	//if (!qcOk) {

						  	updateStage(stmtUpdate, hostCode, spoolName, jobNo, subjobNo, cancelSending.equals(qcResult)? 16:
						  		qcOk.equals(qcResult)||backToTGL.equals(qcResult)? 4:6);
						} catch (Exception e) {
							e.printStackTrace();	
							if(rc==0) rc = 10;
						}
					//}	
				}

				if (rc!=21 && !commitEra()) {
					rc = 21;
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			rc = 20;
		}
		finally {
			try {
				if (result != null) { result.close(); result = null; }
				if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
				if (conn != null) { conn.close(); conn = null; }
			} catch (Exception e3) {
				e3.printStackTrace();
			}	
		}	

		return rc;		
	}

	StringBuilder _sbEra = null;
	String _batchId = "";

	boolean addEra (String batchId, String seq, String policyCode, String backToTgl) {
		boolean b = true;
		if (!_batchId.equals(batchId))	{
			b = commitEra();
			_batchId = batchId;
			_sbEra = new StringBuilder(String.format("%s,%s,%s,%s%n",  forCSV("batchID"), forCSV("seqNo"), forCSV("policyNo"), forCSV("backToTGL") )); //cvs head
		}
		if (!Utils.isEmpty(seq)) {
			_sbEra.append(String.format("%s,%s,%s,%s%n",  forCSV(batchId), forCSV(seq), forCSV(policyCode), forCSV(backToTgl) ));
		}
		return b;
	}

	boolean commitEra () {
		boolean b = true;
		if (_sbEra != null) {
			try {
				sip.util.SysVar sv = new sip.util.SysVar();
        		String tmpFolder = sv.get("TMP_FOLDER");
        		String eraFile = String.format("%s\\rq_%s.era", tmpFolder, _batchId);
        		str2File(_sbEra.toString(), eraFile);
				ProcessBuilder pb = new ProcessBuilder("sendjob", "local", _batchId.contains("FMT_POS_0190")? "VP_TO_YET":_vpName, eraFile);
	            run_proc(pb);
			} catch (Exception e) {
				e.printStackTrace();
				b = false;
			}
			_sbEra = null; 
		}
		return b;
	}

	void str2File (String str, String fileName) throws Exception {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] ba = str.getBytes("UTF-8");
        fos.write(ba);

        fos.close();
    }

	void run_proc (ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String lineRead;
        while ((lineRead = br.readLine()) != null) {
            System.out.println(lineRead);
        }
        p.waitFor();
    }

    String forCSV (String s) {
        if (!Utils.isEmpty(s)) {
            return String.format("\"%s\"", s.replace("\"", "\"\"")); 
        }
        return "\"\"";
    }


    void updateStage (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, int stage) throws Exception {
            try {
                stmt.setInt(1, stage);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
