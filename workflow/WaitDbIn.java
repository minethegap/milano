import java.sql.*;
import common.*;

class WaitDbIn {
	static boolean sqlLogEnabled = true;

	public static void main (String[] args) {
		int rc = 0x00010000;

		if (args.length >= 3) {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String connName = "@jdbc_ODS";
			String tblName = "T_FMT_DOCUMENT";

			String hostCode = args[0];
			String spoolName = args[1];
			String jobNo = args[2]; 

			if (args.length >= 4) tblName = args[3];
			if (args.length >= 5) connName = args[4];

			String sqlCmd = "select subjobno from " + tblName + " where hostcode=? and spoolname=? and jobno=? order by subjobno desc";
			System.out.format("%s connName=%s%nsqlCmd=%s%n", sdf.format(new java.util.Date()), connName, sqlCmd);

			try {
				ResultSet result = null;
				Connection conn = (new Sql(connName)).getConn();
				PreparedStatement stmt = sqlLogEnabled? new LoggableStatement(conn, sqlCmd) : conn.prepareStatement(sqlCmd);
				stmt.setString(1,hostCode);
				stmt.setString(2,spoolName);
				stmt.setString(3,jobNo);
				if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)stmt).getQueryString());
		            }

				while (true) {
        			result = stmt.executeQuery();
                    if (result.next()) {
                    	int subjobno = result.getInt(1);
                    	System.out.format("%s subjobs=%d%n", sdf.format(new java.util.Date()), subjobno);
                    	break;
                    }
        			Thread.sleep(10*1000);//10 secs
        		}

			} catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("WaitDbIn <hostCode> <spoolName> <jobNo> [tblName] [connName]");
			rc += 15;
		}

		System.exit(rc);
	}
}
