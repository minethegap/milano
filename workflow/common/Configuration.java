package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

	private boolean sqlLogEnabled;

	private static Configuration instance;

	private Map<String, String> parameter = new HashMap<String,String> ();

	private Configuration() {
	}

	public static synchronized Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
			instance.initConfiguration();
		}
		return instance;
	}

	private void initConfiguration() {

		StringBuffer sqlStm = new StringBuffer("select short_name, approval_flow from t_fmt_tpl_cfg");

		Connection conn = this.getConnection();

		try {
			PreparedStatement stmt = sqlLogEnabled ? new LoggableStatement(conn, sqlStm.toString())
					: conn.prepareStatement(sqlStm.toString());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String shortName = rs.getString("short_name");
				String approval_flow = rs.getString("approval_flow");
				if (approval_flow == null)
				{
					approval_flow = "";
				}
				parameter.put(shortName, approval_flow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getValue(String key) {
		String value = null;
		if (parameter != null) {
			value = parameter.get(key);
		}
		return value;
	}

	public void reInitConfiguration() {
		parameter = new HashMap<String,String> (); 
		initConfiguration();
	}

	private Connection getConnection() {
		
		Connection connection = null;
		connection = (new Sql("@jdbc_TGL")).getConn();

		/*String DB_URL = "jdbc:oracle:thin:@10.67.67.122:1521/ebaoprd1";
		String USER = "sip";
		String PASS = "sip12345";
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connecting to database...");*/
		
		
		return connection;
	}
	
	public static final void main (String argus[])
	{
		Configuration  c = Configuration.getInstance();
	}

}
