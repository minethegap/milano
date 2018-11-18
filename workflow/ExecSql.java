
import java.sql.*;
import common.*;

class ExecSql {
	static boolean sqlLogEnabled = true;

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 2) {
			String connName = args[0];
			String sqlCmd = args[1];
			System.out.format("connName=%s%nsqlCmd=%s%n", connName, sqlCmd);
			try {
				Connection conn = (new Sql(connName)).getConn();
				conn.setAutoCommit(true);
				PreparedStatement stmt = sqlLogEnabled? new LoggableStatement(conn, sqlCmd) : conn.prepareStatement(sqlCmd);
				if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)stmt).getQueryString());
		            }
				stmt.execute();
			} catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("ExecSql <connName> <sqlCmd>");
			rc += 15;
		}
		System.exit(rc);
	}
}