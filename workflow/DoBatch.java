import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.util.Calendar;
import sip.util.*;
import common.*;



class DoBatch {
	boolean _testMode = false;

	static String _tteFolder = null;
	static String _peerIP = null;

	public static void main (String[] args) throws Exception {
		int rc = 0x00010000;

        try {
			Files.newOutputStream(Paths.get("c:\\sip\\spool\\dobatch.active"), StandardOpenOption.CREATE, StandardOpenOption.DELETE_ON_CLOSE);
		} catch (Exception ef) {
            ef.printStackTrace();
        }

		SysVar sv = new SysVar();

		String connName = "@jdbc_TGL";

		long listId = Long.parseLong(args[0]);

		String batchTblName = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1]:sv.get("BATCH_TBL","stage.t_fmt_batch@ODSLINK");
		String fmtTblName = args.length >= 3 && !Utils.isEmpty(args[2]) ? args[2]:sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
		String pdfFolder = args.length >= 4 && !Utils.isEmpty(args[3]) ? args[3]:sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");
		String tmpFolder = args.length >= 5 && !Utils.isEmpty(args[4]) ? args[4]:sv.get("TMP_FOLDER","D:\\SIPTEMP");

		_tteFolder = sv.get("TTE_FOLDER","");//\\\\10.67.0.228\\Dynafont
		_peerIP = sv.get("PEER_IP","");

		rc += (new DoBatch(listId, connName, batchTblName, fmtTblName, pdfFolder, tmpFolder)).start();

		System.exit(rc);
	}

	long _listId;
	String _connName;
	String _connName2 = "@jdbc_ODS";
	String _batchTblName;
	String _fmtTblName;
	String _pdfFolder;
	String _tmpFolder;

	DbInPara _dbInPara;
	String _batchId = null;
	String _status = null;
	Date _staTim = null;
	Date _endTim = null;
	Date dataDate = null ;


	public DoBatch (long listId, String connName, String  batchTblName, String fmtTblName, String pdfFolder, String tmpFolder) {
		_listId = listId;	
		_connName = connName;
		_batchTblName = batchTblName;
		_fmtTblName = fmtTblName;
		_pdfFolder = pdfFolder;
		_tmpFolder = tmpFolder;
		_dbInPara = new DbInPara("T_DOCUMENT_B");
	}

	public int start () throws Exception {

		int rc = 100; 

		boolean isEnd = false;
		boolean isToPrintPolicy = false;

		boolean isUpdateEUDC = false;

		String sqlSelPrev = null;

		int totalJobs = 0;

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat bdf = new SimpleDateFormat("yyyy-MM-dd");
		
		while (true) {
			
			String timStr = "1=1";

			getBatchSatus();

			if (!isUpdateEUDC) {
				do_UpdateEUDC();
				Thread.sleep(1000*20);
				isUpdateEUDC = true;
			}

			if ("P".equals(_status)) {
				if (_staTim!=null)
					timStr = String.format("d.UPDATE_TIMESTAMP>=to_date('%s','YYYY-MM-DD HH24:MI:SS')", sdf.format(_staTim));
			}
			else if ("S".equals(_status) || "G".equals(_status)) {
				isEnd = true;
				if (_staTim!=null && _endTim!=null)
					timStr = String.format("d.UPDATE_TIMESTAMP>=to_date('%s','YYYY-MM-DD HH24:MI:SS') and d.UPDATE_TIMESTAMP<=to_date('%s','YYYY-MM-DD HH24:MI:SS')", sdf.format(_staTim), sdf.format(_endTim));
				else if (_staTim!=null)
					timStr = String.format("d.UPDATE_TIMESTAMP>=to_date('%s','YYYY-MM-DD HH24:MI:SS')", sdf.format(_staTim));
				else if (_endTim!=null)
					timStr = String.format("d.UPDATE_TIMESTAMP<=to_date('%s','YYYY-MM-DD HH24:MI:SS')", sdf.format(_endTim));
			}
			else if ("1".equals(_status) || "2".equals(_status)) {
				if (_staTim!=null)
					timStr = String.format("d.UPDATE_TIMESTAMP>=to_date('%s','YYYY-MM-DD HH24:MI:SS')", sdf.format(_staTim));
				if ("1".equals(_status)) {
					if (!isToPrintPolicy) {
						if(isPipEnd()) {
							do_ToPrint_Policy();
							isToPrintPolicy = true;
						}
					}
				}
			}
			else {
				System.out.format("Error batch status=%s%n", _status);
				break;
			}

			DbInPara dbInPara = _dbInPara;
			String sqlSel =  dbInPara.sqlSelect.replace("?", timStr);
			if (!sqlSel.equals(sqlSelPrev)) {
				sqlSelPrev = sqlSel;
				System.out.println(sqlSel);
			}
			DbIn dbIn = new DbIn(dbInPara.dbinName, dbInPara.connName, sqlSel, dbInPara.sqlInsert, dbInPara.sipURL, dbInPara.vp, 0, 0, dbInPara.merge, dbInPara.mergeSizeMax);
			dbIn.setBatchId(_batchId);
			int cnt = dbIn.start();
			totalJobs += cnt;

			if (isEnd && 0==cnt) break;
		
			Thread.sleep(1000*60);
		}

		if (isEnd) {//core batch end and dbin end
			System.out.format("batch %s, total %d%n", _batchId, totalJobs);

			updateDbInEnd();

			if (!isToPrintPolicy) {
				do_ToPrint_Policy();
				isToPrintPolicy = true;
			}

			System.out.println("to FDW for ad hoc");
			do_ToFDW(_batchId,true);//to FDW for ad hoc

			isFmtEnd(_batchId);
			updateFmtEnd();

			//do grouping
			do_ToGroup();
			updateTim("GROUP_END_TIM");

			System.out.println("to FDW for batch");
			do_ToFDW(_batchId,false);//to FDW for batch

			System.out.println("sending to print vender");
			do_ToPrint();
			updateTim("PRINT_TIM");

			System.out.println("sending to esub");
			do_ToEsub();
			updateTim("ESUB_TIM");

			do_MergeCLMA();

			rc = 0;
		}

		return rc;
	}

	void getBatchSatus () {
		Connection conn = null;
   		PreparedStatement stmtSel = null;
  		ResultSet result = null;
		
		try {
  				conn = (new Sql(_connName)).getConn();
				stmtSel = conn.prepareStatement("select * from t_batch_job_etlfct_stg where LIST_ID<=? order by LIST_ID desc");
				stmtSel.setLong(1, _listId);
				result = stmtSel.executeQuery();
				if (result.next()) {
					long tlistid = result.getLong("LIST_ID");
				  if (tlistid==_listId) {
					_status = result.getString("TBL_STS");
					//_staTim = result.getDate("RUN_START_TIM");
					Date endTim = result.getDate("RUN_END_TIM");
					if (endTim != null && !endTim.equals(_endTim)) {
						_endTim = endTim;
						System.out.format("batch %d TBL_STS=%s RUN_END_TIM=%s%n", _listId, _status,
								 (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(_endTim) );	
					}
					if (_batchId == null) {
						_batchId = String.format("B%s_%d", (new java.text.SimpleDateFormat("yyMMdd")).format(result.getDate("DATA_DT")), _listId);
						dataDate = result.getDate("DATA_DT");
					}

					if (_staTim==null && result.next()) {//prev. batch
						_staTim = result.getDate("RUN_END_TIM");

						if (_staTim!=null)
							System.out.format("last batch %d RUN_END_TIM=%s%n",  result.getLong("LIST_ID"),
								 (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(_staTim) );
					}

				  }	//if (tlistid==_listId) {
				}

  			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (result != null) { result.close(); result = null; }
					if (stmtSel != null) { stmtSel.close(); stmtSel = null; }
					if (conn != null) { conn.close(); conn = null; }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
	}

	void updateTim (String timName) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strSql;
		Connection conn = null;
   		PreparedStatement stmt = null;
		try {
  				conn = (new Sql(_connName2)).getConn();
				stmt = conn.prepareStatement(strSql="update " + "t_fmt_batch" + " set " + timName + "=sysdate where LIST_ID=?");
				System.out.format("%s %s;%d%n", sdf.format(new java.util.Date()), strSql, _listId);
				stmt.setLong(1, _listId);
				stmt.executeUpdate();
  			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (stmt != null) { stmt.close(); stmt = null; }
					if (conn != null) { conn.close(); conn = null; }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
	}

	void updateDbInEnd () {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strSql;
		Connection conn = null;
   		PreparedStatement stmt = null;
		try {
  				conn = (new Sql(_connName2)).getConn();
				stmt = conn.prepareStatement(strSql="update " + "t_fmt_batch" + " set DBIN_END_TIM=sysdate where LIST_ID=?");
				System.out.format("%s %s;%d%n", sdf.format(new java.util.Date()), strSql, _listId);
				stmt.setLong(1, _listId);
				stmt.executeUpdate();
  			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (stmt != null) { stmt.close(); stmt = null; }
					if (conn != null) { conn.close(); conn = null; }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
	}

	void updateFmtEnd () {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strSql;
		Connection conn = null;
   		PreparedStatement stmt = null;
		try {
  				conn = (new Sql(_connName2)).getConn();
				stmt = conn.prepareStatement(strSql="update " + "t_fmt_batch" + " set FMT_END_TIM=sysdate where LIST_ID=?");
				System.out.format("%s %s;%d%n", sdf.format(new java.util.Date()), strSql, _listId);
				stmt.setLong(1, _listId);
				stmt.executeUpdate();
  			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (stmt != null) { stmt.close(); stmt = null; }
					if (conn != null) { conn.close(); conn = null; }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
	}

	void do_ToGroup () {
		try {
			//String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\ToGroup.sql")));
			String sqlSelect = (new SysVar()).getSql("ToGroup.sql");
			System.out.println(sqlSelect);
			sqlSelect = sqlSelect.replace("?", "'"+_batchId+"'");
			String sqlUpdate = String.format("update %s set MERGED=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", _fmtTblName);
			System.out.println(sqlUpdate);	
			boolean rc = (new ToGroup(_connName, sqlSelect, sqlUpdate, _pdfFolder)).start();
		} catch (Exception e) {
				e.printStackTrace();
			}
	}

	void do_ToEsub () {
		try {
			String sqlSelect = (new SysVar()).getSql("ToEsub.sql");
			System.out.println(sqlSelect);
			sqlSelect = sqlSelect.replace("?", "'"+_batchId+"'");
			String sqlUpdate = String.format("update %s set STAGE=2 where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?", _fmtTblName);
			System.out.println(sqlUpdate);
			boolean rc = (new ToEsub(_connName, sqlSelect, sqlUpdate, _pdfFolder, _tmpFolder, _batchId)).start();
		} catch (Exception e) {
				e.printStackTrace();
			}
	}

	void do_ToPrint () {
		boolean rc = false;
		try {
			if ((new File("c:\\sip\\cfg\\ToPrintShortNames.sql")).exists()) {

				rc = (new ToPrintMulti(_batchId)).start();

			} else {

			String sqlSelect = (new SysVar()).getSql("ToPrint.sql");
			System.out.println(sqlSelect);
			sqlSelect = sqlSelect.replace("?", "'"+_batchId+"'");
			rc = (new ToPrint(false, _connName, sqlSelect, _pdfFolder, _fmtTblName, false, "VP_TO_BET", _batchId)).start(_testMode);

			}
		} catch (Exception e) {
				e.printStackTrace();
			}
	}

	void do_ToFDW (String batchId, boolean isAdhoc) {
		//String jobName = "ToFDW ToFDW_A.sql";
		//if (batchId != null) jobName = "ToFDW ToFDW.sql " + batchId;
		String jobName =  isAdhoc?  "ToFDW ToFDW_A.sql " + batchId : "ToFDW ToFDW.sql " + batchId;
		ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_JAVA", "c:\\sip\\cfg\\empty.spl", jobName, _batchId);
		//ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_JAVA", "c:\\sip\\cfg\\runJava_sipjt.xml", jobName);
		try {
            run_proc(pb);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

	void do_ToPrint_Policy () {
		String jobName = "ToPrint ToPrint_Policy.sql ONCE";
		ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_JAVA", "c:\\sip\\cfg\\empty.spl", jobName, "Batch "+Long.toString(_listId) );
		try {
            run_proc(pb);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

	void do_UpdateEUDC () {
		if (!Utils.isEmpty(_tteFolder)) {
			updateTTE("local", "細明體.tte");
			if (!Utils.isEmpty(_peerIP)) {
				updateTTE(_peerIP, "細明體.tte");
			}
			updateTTE("local", "標楷體.tte");
			if (!Utils.isEmpty(_peerIP)) {
				updateTTE(_peerIP, "標楷體.tte");
			}
		}
	}

	void updateTTE (String srv, String tte) {
		String srcTTE = String.format("%s\\%s", _tteFolder, tte);
		System.out.format("update %s->%s%n", srcTTE, srv);
		ProcessBuilder pb = new ProcessBuilder("sendjob", srv, "VP_UpdateEUDC", srcTTE, tte, _batchId);
		try {
            run_proc(pb);
        } catch (Exception e) {
			e.printStackTrace();
		}
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

	boolean isFmtEnd (String batchId) {
		boolean rc = false;

		int prevCnt = -1;
		
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strSql;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			conn = (new Sql(_connName2)).getConn();
			stmt = conn.prepareStatement(strSql = "SELECT count(LIST_ID) FROM " + "t_fmt_document" +
											" WHERE STAGE=0 AND ERRMSG IS NULL AND BATCHID=?");
			stmt.setString(1, batchId);

			System.out.format("%s %s;%s%n", sdf.format(new java.util.Date()), strSql, batchId);

			while (true) {
				result = stmt.executeQuery();
				int cnt = result.next()? result.getInt(1):-1;
				result.close(); result = null;
				if (cnt != prevCnt) {
					prevCnt = cnt;
					System.out.format("%s count=%d%n",  sdf.format(new java.util.Date()), cnt);
				}
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

	void do_MergeCLMA () {
		String jobName = "MergeCLMA "+Long.toString(_listId);
		ProcessBuilder pb = new ProcessBuilder("sendjob", "local", "VP_JAVA", "c:\\sip\\cfg\\empty.spl", jobName, "Batch "+Long.toString(_listId) );
		try {
            run_proc(pb);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean isPipEnd () {
		boolean rc = false ;
		
		int prevCnt = -1;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat bdf = new SimpleDateFormat("yyyy-MM-dd");
		String strSql;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		Date startTim = dataDate;
		Date endTim = dataDate;

		String strSql2;
		Connection conn2 = null;
		PreparedStatement stmt2 = null;
		ResultSet result2 = null;

		
		try {
			conn = (new Sql(_connName)).getConn();
			stmt = conn.prepareStatement(strSql = " SELECT COUNT(*) FROM T_BATCH_TRIGGER_EVENT WHERE EVENT_ID = 25000 AND PROCESSED <> 'Y'");
			stmt2 = conn.prepareStatement(strSql2 = " SELECT COUNT(*) " 
													+ " FROM T_POLICY_PRINT_JOB p LEFT JOIN T_FMT_POLICY f ON f.JOB_ID = p.JOB_ID"
													+ " WHERE 1=1 AND NVL(f.STAGE, 0) = 0" 
													+ " AND p.JOB_TYPE_DESC = 'NB'" 
													+ " AND p.print_date between to_date('"+ startTim +"', 'yyyy/MM/dd')"
													+ " AND to_date('" + endTim +"', 'yyyy/MM/dd')"
													);

			Calendar c = Calendar.getInstance(); 
			c.setTime(endTim); 
			c.add(Calendar.DATE, 1);
			endTim = (java.sql.Date)c.getTime(); 
			
			strSql2 =  strSql2.replace(bdf.format(startTim), "'"+ startTim +"'");
			strSql2 =  strSql2.replace(bdf.format(endTim), "'"+ endTim +"'");			
			System.out.format("strSql2 = %s ",strSql2);

			while (true) {
				result = stmt.executeQuery();
				int cnt = result.next()? result.getInt(1):-1;
				result.close(); result = null;
				
				//is PIP All In?
				result2 = stmt2.executeQuery();
				int cnt2 = result.next()? result2.getInt(1):-1;
				result2.close(); result2 = null;

				if(cnt != prevCnt) {
					prevCnt = cnt;
					System.out.format("%s PIP Count=%d%n", sdf.format(new java.util.Date()), cnt);
				}
				if(cnt2 != prevCnt){
					prevCnt = cnt;
					System.out.format("%s PIP Count=%d%n", bdf.format(new java.util.Date()), cnt2);
				}

				if(cnt==0 && cnt2==0) {
					rc = true;
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(result != null) { result.close(); result = null; }
				if(stmt != null) { stmt.close(); stmt = null; }
				if(conn != null) { conn.close(); conn = null; }
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return rc;
	}
}
