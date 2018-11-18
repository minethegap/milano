//ToGroup.java

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.zip.*;
import sip.util.*;
import common.*;


public class ToGroup {

    public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 4) {
        	String connName = args[0];
        	String sqlName = args[1];
        	String tblName = args[2];
        	String pdfFolder = args[3];

          	try {
        		System.out.println(connName);
        		System.out.println(sqlName);
             	String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\"+sqlName)));
	        	System.out.println(sqlSelect);
	        	String sqlUpdate = String.format("update %s set MERGED=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", tblName);
	        	System.out.println(sqlUpdate);
	        	System.out.println(pdfFolder);

	        	if (!Utils.isEmpty(connName) && !Utils.isEmpty(sqlSelect)) {
					boolean b = (new ToGroup(connName, sqlSelect, sqlUpdate, pdfFolder)).start();
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
			System.out.println("ToGroup <db_conn_name> <sql_name> <tbl_name> <pdf_folder>");
			rc += 15;
		}

		System.exit(rc);
    }


    String _connName;
	String _sqlSelect;
	String _sqlUpdate;
	String _pdfFolder;

	int _cnt_pdf = 0;
	int _cnt_ok = 0;
	int _cnt_merge_ok = 0;
  	int _cnt_merge_err = 0;
  	int _cnt_merge_real = 0;

    public ToGroup (String connName, String sqlSelect, String sqlUpdate, String pdfFolder) {
    	_connName = connName;
    	_sqlSelect = sqlSelect;
    	_sqlUpdate = sqlUpdate;
    	_pdfFolder = pdfFolder;
    }

    public boolean start () throws Exception {
    	boolean rc = false;

    	String mergeErrMsg = "";

    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	System.out.format("%s start to grouping%n", sdf.format(new java.util.Date()) );

    	long prevListId = 0;
    	int prevOrder = 0; 
    	ArrayList<PdfObj> pdfList = new ArrayList<PdfObj>();

    	Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		PreparedStatement stmtUpdate = null;
  		PreparedStatement stmtUpdateErr = null;
  		ResultSet result = null;

		try {
			DbConnStr dc = new DbConnStr();
			String strDB = dc.getConnStr(_connName);
			String strDrv = dc.getJdbcDriver(_connName);

			Class.forName(strDrv).newInstance();

			conn = DriverManager.getConnection(strDB);
			conn.setAutoCommit(false);

			stmtUpdateErr = conn.prepareStatement( String.format("update %s set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", "t_fmt_document") );
			stmtUpdate = conn.prepareStatement(_sqlUpdate);
			stmtSelect = conn.prepareStatement(_sqlSelect);

			result = stmtSelect.executeQuery();
			while (result.next()) {
				String hostCode = result.getString("HOSTCODE");
				String spoolName = result.getString("SPOOLNAME");
				long jobNo = result.getLong("JOBNO");
				int subjobNo = result.getInt("SUBJOBNO");

				long listId = result.getLong("LIST_ID");
				long groupListId = result.getLong("GROUP_LIST_ID");
				int groupOrder = result.getInt("GROUP_ORDER");

	 			System.out.format("hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, listId=%d, groupListId=%d, groupOrder=%d%n",
	 				hostCode, spoolName, jobNo, subjobNo, listId, groupListId, groupOrder);

	 			if (prevListId != groupListId) {
	 				if (pdfList.size() > 0) {
	 					mergeErrMsg = "";
	 					if (pdfList.size() == prevOrder) {
	 						if (do_merge(conn, stmtUpdate, pdfList)) {_cnt_ok += pdfList.size(); ++_cnt_merge_ok; if (pdfList.size()>1) _cnt_merge_real++;}
		 					else { ++_cnt_merge_err; mergeErrMsg = "Merge PDF error";}
	 					}
	 					else {
	 						System.out.format("Error groupListId=%d%n", prevListId);
	 						++_cnt_merge_err;
	 						mergeErrMsg = "Merge error";
	 					}
	 					if (!Utils.isEmpty(mergeErrMsg)) updateErrMsg(conn, stmtUpdateErr, pdfList, mergeErrMsg);
	 					pdfList.clear();
	 				}
	 			}

	 			pdfList.add(new PdfObj(hostCode,spoolName,jobNo,subjobNo));

	 			prevListId = groupListId;
	 			prevOrder = groupOrder;

				++_cnt_pdf;
			}

			if (pdfList.size() > 0) {
						mergeErrMsg = "";
	 					if (pdfList.size() == prevOrder) {
	 						if (do_merge(conn, stmtUpdate, pdfList)) {_cnt_ok += pdfList.size(); ++_cnt_merge_ok; if (pdfList.size()>1) _cnt_merge_real++;}
		 					else { ++_cnt_merge_err; mergeErrMsg = "Merge PDF error";}
	 					}
	 					else {
	 						System.out.format("Error groupListId=%d%n", prevListId);
	 						++_cnt_merge_err;
	 						mergeErrMsg = "Merge error";
	 					}
	 					if (!Utils.isEmpty(mergeErrMsg)) updateErrMsg(conn, stmtUpdateErr, pdfList, mergeErrMsg);
	 					pdfList.clear();
	 				}

			System.out.format("%s cnt_pdf=%d, cnt_ok=%d, cnt_err=%d, cnt_merge_ok=%d(real=%d), cnt_merge_err=%d%n", sdf.format(new java.util.Date()), 
						_cnt_pdf, _cnt_ok, _cnt_pdf-_cnt_ok, _cnt_merge_ok, _cnt_merge_real, _cnt_merge_err);

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

	boolean do_merge (Connection conn, PreparedStatement stmtUpdate, ArrayList<PdfObj> pdfList) {
		boolean ok = true;

	 	if (pdfList.size() > 1)
	 		ok = do_merge_pdf(pdfList);

	 	if (ok) {
	 		try {
		 		int i = 0;
		 		for (PdfObj p: pdfList) {
		 			stmtUpdate.setInt(1, i==0? pdfList.size() : 0);
		 			stmtUpdate.setString(2, p.hostCode);
		 			stmtUpdate.setString(3, p.spoolName);
		 			stmtUpdate.setLong(4, p.jobNo);
		 			stmtUpdate.setInt(5, p.subjobNo);
		 			stmtUpdate.executeUpdate();
		 			i++;
		 		}
		 		conn.commit();
		 	} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof SQLException) {
					try {
						conn.rollback();
					} catch (Exception e2) { e2.printStackTrace(); }
				}
				if (pdfList.size() > 1) {
					//roll back pdf
					PdfObj p = pdfList.get(0);
					String pdfName0 = String.format("%s\\%s\\%s\\%08x\\%06x_0.pdf", _pdfFolder, p.hostCode, p.spoolName, p.jobNo, p.subjobNo);
					String pdfName = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, p.hostCode, p.spoolName, p.jobNo, p.subjobNo);
					try {
						Files.move(Paths.get(pdfName0), Paths.get(pdfName), StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}
			}					
		}

		return ok;
	}

	void updateErrMsg (Connection conn, PreparedStatement stmtUpdate, ArrayList<PdfObj> pdfList, String mergeErrMsg) {
		try {
		 		for (PdfObj p: pdfList) {
		 			stmtUpdate.setString(1,  mergeErrMsg);
		 			stmtUpdate.setString(2, p.hostCode);
		 			stmtUpdate.setString(3, p.spoolName);
		 			stmtUpdate.setLong(4, p.jobNo);
		 			stmtUpdate.setInt(5, p.subjobNo);
		 			stmtUpdate.executeUpdate();
		 		}
		 		conn.commit();
		 	} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof SQLException) {
					try {
						conn.rollback();
					} catch (Exception e2) { e2.printStackTrace(); }
				}
			}
	}

	boolean do_merge_pdf (ArrayList<PdfObj> pdfList) {
		boolean ok = true;
		ArrayList<String> pdfNameList = new ArrayList<String>();
		String pdfName0 = null;
		String pdfName1 = null;
		String pdfName2 = null;
		int i = 0;
		for (PdfObj p: pdfList) {
			String pdfName = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, p.hostCode, p.spoolName, p.jobNo, p.subjobNo);
			pdfNameList.add(pdfName);
			if (i==0) {
				pdfName0 = String.format("%s\\%s\\%s\\%08x\\%06x_0.pdf", _pdfFolder, p.hostCode, p.spoolName, p.jobNo, p.subjobNo); 
				pdfName1 = pdfName;
				pdfName2 = String.format("%s\\%s\\%s\\%08x\\%06x_2.pdf", _pdfFolder, p.hostCode, p.spoolName, p.jobNo, p.subjobNo); 
			}
			++i;
		}

		ok = (new MergePDF()).merge(pdfNameList, pdfName2);

		if (ok) {
			try {
				Files.move(Paths.get(pdfName1), Paths.get(pdfName0), StandardCopyOption.REPLACE_EXISTING);
				Files.move(Paths.get(pdfName2), Paths.get(pdfName1), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
				ok = false;
			}
		}

		return ok;
	}

	class PdfObj {
		public String hostCode;
		public String spoolName;
		public long jobNo;
		public int subjobNo;

		public PdfObj (String h, String s, Long j, int sj) {
			hostCode = h;
			spoolName = s;
			jobNo = j;
			subjobNo = sj;	
		}
	}

}
