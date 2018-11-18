import java.io.*;
import java.nio.file.*;
import java.sql.*;
import sip.util.*;
import common.*;


class AdhocWatch {

	public static void main (String[] args) throws Exception {
		//int rc = 0x00010000;

		SysVar sv = new SysVar();

		String connName = "@jdbc_TGL";

		String fmtTblName = args.length >= 1 && !Utils.isEmpty(args[0]) ? args[0]:sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
		String pdfFolder = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1]:sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");

		(new AdhocWatch()).start(connName, fmtTblName, pdfFolder);

		//System.exit(rc);
	}

	public void start (String connName, String fmtTblName, String pdfFolder) throws Exception {
		boolean fst = true;
		DbInPara dbInPara = new DbInPara("T_DOCUMENT_A");
		//DbInPara dbInParaJ = new DbInPara("T_DOCUMENT_A_JDT");

		while (true) {

			String batchId = String.format("A%s", (new java.text.SimpleDateFormat("yyMMddHHmmss")).format(new java.util.Date()));
			
			DbIn dbIn = new DbIn(dbInPara.dbinName, dbInPara.connName, dbInPara.sqlSelect, dbInPara.sqlInsert, dbInPara.sipURL, dbInPara.vp, 0, 0, /*dbInPara.scanInt, dbInPara.maxJobs,*/ dbInPara.merge, dbInPara.mergeSizeMax);
			dbIn.setBatchId(batchId);
			int cnt = dbIn.start();

			//DbIn dbInJ = new DbIn(dbInParaJ.dbinName, dbInParaJ.connName, dbInParaJ.sqlSelect, dbInParaJ.sqlInsert, dbInParaJ.sipURL, dbInParaJ.vp, 0, 0, /*dbInParaJ.scanInt, dbInParaJ.maxJobs,*/ dbInParaJ.merge, dbInParaJ.mergeSizeMax);
			//dbInJ.setBatchId(batchId);
			//int cntJ = dbInJ.start();
			int cntJ = 0;

			if ((cnt > 0 || cntJ > 0) && isFmtEnd(connName, fmtTblName, batchId)) {
				//String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\ToGroup.sql")));
				String sqlSelect =  (new SysVar()).getSql("ToGroup.sql");
				if (fst) System.out.println(sqlSelect);
				sqlSelect = sqlSelect.replace("?", "'"+batchId+"'");
				String sqlUpdate = String.format("update %s set MERGED=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", fmtTblName);
				if (fst) System.out.println(sqlUpdate);
				boolean rc = (new ToGroup(connName, sqlSelect, sqlUpdate, pdfFolder)).start();
				fst = false;
			}

			Thread.sleep(1000*60);
		}

	}

	boolean isFmtEnd (String connName, String fmtTblName, String batchId) {
		boolean rc = false;

		String strSql;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			conn = (new Sql(connName)).getConn();
			stmt = conn.prepareStatement(strSql = "SELECT count(LIST_ID) FROM " + fmtTblName +
											" WHERE SPOOLNAME='S_A_LTR' AND STAGE=0 AND ERRMSG IS NULL AND BATCHID=?");
			stmt.setString(1, batchId);

			System.out.format("%s;%s%n", strSql, batchId);

			while (true) {
				result = stmt.executeQuery();
				int cnt = result.next()? result.getInt(1):-1;
				result.close(); result = null;
				if (cnt==0) {
					rc = true;
					break;
				}
			
				Thread.sleep(1000*60);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (result != null) { result.close(); result = null; }
				if (stmt != null) { stmt.close(); stmt = null; }
				if (conn != null) { conn.close(); conn = null; }
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return rc;
	}

}
