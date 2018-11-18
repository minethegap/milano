
import java.io.*;
import java.sql.*;
import sip.util.*;
import common.*;

class JobLog {

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 5) {
			try {
				String jobLog = args[0];
				String hostCode = args[1];
				String spoolName = args[2];
				String jobNo = args[3];
				String tblName = args[4];
				String connName = args.length > 5? args[5] : "@jdbc_TGL"; 
				String jobName = args.length > 6? args[6] : null;

				Connection conn = (new Sql(connName)).getConn();
				conn.setAutoCommit(true);
/*
				PreparedStatement stmtSelect = conn.prepareStatement("select *  from " + tblName + " where ERRMSG is null and HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=1");
				stmtSelect.setString(1,hostCode);
				stmtSelect.setString(2,spoolName);
				stmtSelect.setString(3,jobNo);
*/ 
				StringBuilder sbMsg = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jobLog)));         
		        String line;
		        while ((line = br.readLine()) != null) {
		        	String lstr = line.toLowerCase();
		        	if (lstr.contains("exception") || lstr.contains(" error") || lstr.contains(" fault")  || lstr.contains("return") && !lstr.endsWith("return 0") && !lstr.contains("return font")
		        	 || lstr.contains("not found") || lstr.contains("failed") ) {
		        		sbMsg.append(line);   
		        		sbMsg.append("\n");
		        	}
		        }
		        br.close();
				
				String msg = sbMsg.toString();
				if (msg.length() > 2000)
					msg = msg.substring(msg.length() - 2000);

				//PreparedStatement stmt = conn.prepareStatement("update " + tblName + " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=1");
				PreparedStatement stmt = conn.prepareStatement(jobName==null?"update " + tblName + " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and ERRMSG IS NULL"
						: "insert into " + tblName +  " (LOG_ID,ERRMSG,HOSTCODE,SPOOLNAME,JOBNO,JOBNAME) values (log_seq.nextval,?,?,?,?,?)" );
				stmt.setString(1,msg);
				stmt.setString(2,hostCode);
				stmt.setString(3,spoolName);
				stmt.setString(4,jobNo);
				if (jobName != null) stmt.setString(5,jobName);
				stmt.executeUpdate();
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("JobLog <job_log> <hostcode> <spoolname> <jobno> <tbl_name> [conn_name]");
			rc += 15;
		}
		System.exit(rc);
	}
}