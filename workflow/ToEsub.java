//ToEsub.java

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.zip.*;
import sip.util.*;
import common.*;


public class ToEsub {

	static String listHead = "<PipInfo><Head><ShiftPK>%s</ShiftPK><ShiftOriginalTime>%s</ShiftOriginalTime><BussTypeCode>%s</BussTypeCode><CkDeptCode>%s</CkDeptCode>"
		+ "<PipCount>%d</PipCount></Head><PackageList><Package id=\"%s\">";
	static String listTail = "</Package></PackageList></PipInfo>";
	static String listItem = "<Pip><PipPK>%s</PipPK><PdfPath>/PDF/%s</PdfPath><PipNum>%s</PipNum><FieldList>"
	//	+ "<companyCode1> </companyCode1><companyCode2> </companyCode2><companyCode3> </companyCode3><companyCode4> </companyCode4>%s"
		+ "%s"
		+ "<spoolname>%s*%s</spoolname><jobno>%d</jobno><subjobno>%d</subjobno></FieldList></Pip>";

    public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 6) {
        	String connName = args[0];
        	String sqlName = args[1];
        	String tblName = args[2];
        	String pdfFolder = args[3];
        	String jobPath = args[4];
        	String batchPrefix = args[5];

        	try {
        		System.out.println(connName);
        		System.out.println(sqlName);
	        	String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\"+sqlName)));
	        	System.out.println(sqlSelect);
	        	String sqlUpdate = String.format("update %s set STAGE=2 where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", tblName);
	        	System.out.println(sqlUpdate);

	        	if (!Utils.isEmpty(connName) && !Utils.isEmpty(sqlSelect)) {
					boolean b = (new ToEsub(connName, sqlSelect, sqlUpdate, pdfFolder, jobPath, batchPrefix)).start();
					if (b)
						;
					else {
						rc += 22;
					}
				}
				else {
					rc += 21;
				}
	        } catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
        }
        else {
			System.out.println("ToEsub <db_conn_name> <sql_name> <tbl_name> <pdf_folder> <job_path> <batch_prefix>");
			rc += 15;
		}

		System.exit(rc);
    }


    String _connName;
	String _sqlSelect;
	String _sqlUpdate;
	String _pdfFolder;
    String _outFolder;
    String _batchPrefix;

    String _batchShortName;
    int _batchSeq;
    int _nItem;

    Connection conn = null;
    PreparedStatement stmtUpdate = null;

    public ToEsub (String connName, String sqlSelect, String sqlUpdate, String pdfFolder, String outFolder, String batchPrefix) {
    	_connName = connName;
    	_sqlSelect = sqlSelect;
    	_sqlUpdate = sqlUpdate;
    	_pdfFolder = pdfFolder;
    	_outFolder = outFolder;
    	_batchPrefix = batchPrefix;

    	_batchShortName = "";
    	_batchSeq = 0;
    	_nItem = 0;
    }

    public boolean start () throws Exception {
    	boolean rc = false;

  		PreparedStatement stmtSelect = null;
  		ResultSet result = null;

		try {
			DbConnStr dc = new DbConnStr();
			String strDB = dc.getConnStr(_connName);
			String strDrv = dc.getJdbcDriver(_connName);

			Class.forName(strDrv).newInstance();

			int cnt = 0;

			conn = DriverManager.getConnection(strDB);
			conn.setAutoCommit(false);

			stmtSelect = conn.prepareStatement(_sqlSelect);
			stmtUpdate = conn.prepareStatement(_sqlUpdate);

			result = stmtSelect.executeQuery();
			while (result.next()) {
				String hostCode = result.getString(1);
				String spoolName = result.getString(2);
				long jobNo = result.getLong(3);
				int subjobNo = result.getInt(4);
				String shortName = result.getString(5);
				long listId = result.getLong(6);;
				Clob rptData = result.getClob(7);
	 			String strData = rptData.getSubString(1, (int)rptData.length());

	 			System.out.format("hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",
	 				hostCode, spoolName, jobNo, subjobNo, shortName, listId);

				if (!shortName.equals(_batchShortName)) {
					commitBatch();
					newBatch(shortName);
				}

				addItem(hostCode, spoolName, jobNo, subjobNo, strData);

				++cnt;
			}

			commitBatch();

			System.out.println("Total pdfs=" + cnt);
			rc = true;
    	}
		catch (Exception e) {
			handleException(e);
		}
		finally {
			if (result != null) { result.close(); result = null; }
			if (stmtUpdate != null) { stmtUpdate.close(); stmtUpdate = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
		}
    	return rc;
    }

    void handleException (Exception e) {
		if (e instanceof SQLException) {
			System.err.println("SQLState=" + ((SQLException)e).getSQLState());
			System.err.println("ErrorCode=" + ((SQLException)e).getErrorCode());
		}
		e.printStackTrace();
	}

//	ZipOutputStream _zipOut;
	StringBuilder _sbList;
	String _shiftPk;

	void addItem (String hostCode, String spoolName, long jobNo, int subjobNo, String xmlStr) throws Exception {
		++_nItem;

		String pipPk = String.format("%s_%s_%08x_%06x", hostCode, spoolName, jobNo, subjobNo);

		System.out.format("pipPk%d=%s%n", _nItem, pipPk);

		String srcPdf = String.format("%s/%s/%08x/%06x.pdf", hostCode, spoolName, jobNo, subjobNo); 

	/*	String srcPdf = String.format("%s\\%s\\%s\\%08x_%05d.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo); 
		File file = new File(srcPdf);
		FileInputStream fis = new FileInputStream(file);
		_zipOut.putNextEntry(new ZipEntry(_shiftPk+"/"+pipPk+".pdf")); 
		byte[] buf = new byte[4*1024];
		int count;
		while ((count = fis.read(buf)) > 0) {
			_zipOut.write(buf, 0, count);
		}
		_zipOut.closeEntry();
		fis.close(); */

		String documentNo = "";

		StringBuilder sbIdx = new StringBuilder();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document dom = db.parse(new org.xml.sax.InputSource(new StringReader(xmlStr)));
		NodeList nl = dom.getElementsByTagName("index").item(0).getChildNodes();
		for (int i=0; i<nl.getLength(); ++i) {
			Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
            	String nodeName = n.getNodeName();
            	String nodeText = n.getTextContent();

            						boolean isList = false;
                                    if (nodeName.endsWith("List")) {
                                        String key2 = nodeName.substring(0, nodeName.length()-4);
                                        NodeList nl2 = ((Element)n).getElementsByTagName(key2);
                                        if (nl2!= null && nl2.getLength() > 0) {
                                            isList = true;
                                            Node n2 = nl2.item(0);
                                            nodeName = n.getNodeName();
            								nodeText = n.getTextContent();
                                        }
                                    } 

            	sbIdx.append(String.format("<%s>%s</%s>", nodeName, nodeText, nodeName));
            	if ("policyCode".equals(nodeName))
            		sbIdx.append(String.format("<policyCodeLen>%d</policyCodeLen>", nodeText.length() ));
            	else if ("documentNo".equals(nodeName))
            		documentNo = nodeText;
            }
		}

		_sbList.append(String.format(listItem, pipPk, srcPdf, /*pipPk*/documentNo, sbIdx.toString(), 
			hostCode, spoolName, jobNo, subjobNo));

		//	stmtUpdate.setString(1, _batchPrefix);
		 			stmtUpdate.setString(1, hostCode);
		 			stmtUpdate.setString(2, spoolName);
		 			stmtUpdate.setLong(3, jobNo);
		 			stmtUpdate.setInt(4, subjobNo);
		 			stmtUpdate.executeUpdate();
	}

	void newBatch (String shortName) throws Exception {
		_nItem = 0;
		_batchShortName = shortName;
		++_batchSeq;
		_shiftPk = String.format("%s-%s-%d", _batchPrefix, _batchShortName, _batchSeq);
		System.out.println("shiftPk="+_shiftPk);
		_sbList = new StringBuilder();
	/*	String zipFile = String.format("%s\\%s.zip", _outFolder, _shiftPk);
		_zipOut = new ZipOutputStream(new FileOutputStream(new File(zipFile)));
		_zipOut.putNextEntry(new ZipEntry(_shiftPk+"/")); */
	}

	void commitBatch () throws Exception {
		if (_nItem > 0) {
		//	_zipOut.finish();
		//	_zipOut.close();

			System.out.format("Items=%d%n", _nItem);

			java.util.Date date = new java.util.Date();

			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String xmlFile = String.format("%s\\%s.xml", _outFolder, _shiftPk);
			File file = new File(xmlFile);
			FileOutputStream fos = new FileOutputStream(file);

			byte[] ba = String.format(listHead, _shiftPk, sdf.format(date), _batchShortName, get_dept(_batchShortName), _nItem, _shiftPk).getBytes("UTF-8");
			fos.write(ba);

			ba = _sbList.toString().getBytes("UTF-8");
			fos.write(ba);

			ba = listTail.toString().getBytes("UTF-8");
			fos.write(ba);

			fos.close();

			boolean sent = send_job();

			if (sent)
				conn.commit();
			else
				conn.rollback();

			_nItem = 0;	
		}
	}

	String get_dept (String shortName) {
		String dept = "";
		int idx = shortName.indexOf('_');
		if (idx >= 0) {
			shortName = shortName.substring(idx+1);
			idx = shortName.indexOf('_');	
			if (idx >= 0) {
				dept = shortName.substring(0,idx);
			}
		}
		return dept;
	}

	boolean send_job ()  {
	/*	String jobFile = String.format("%s\\%s.zip", _outFolder, _shiftPk);
		String docName = String.format("%s_%d.zip", _shiftPk, _nItem);
		ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_ESUB_CHECK", jobFile, docName);
		run_proc(pb); */
		String jobFile = String.format("%s\\%s.xml", _outFolder, _shiftPk);
		String docName = String.format("%s_%d.xml", _shiftPk, _nItem);
		ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_ESUB_IN", jobFile, docName);
		return run_proc(pb);
	}

	boolean run_proc (ProcessBuilder pb) {
		boolean rc = false;
		try {
			pb.redirectErrorStream(true);
			Process p = pb.start();
			InputStreamReader isr = new InputStreamReader(p.getInputStream());
	    	BufferedReader br = new BufferedReader(isr);
			String lineRead;
			while ((lineRead = br.readLine()) != null) {
				System.out.println(lineRead);
			}
			p.waitFor();
			if (p.exitValue()==0x00030000) rc = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rc;
	}
}
