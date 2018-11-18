
package common;

import java.io.*;
import java.sql.*;
import java.util.*;
import sip.util.*;


public class Sql {
	Connection _conn = null;

	public Sql (String connStrName) {
		try {
			DbConnStr dbConnStr = new DbConnStr();
			Class.forName(dbConnStr.getJdbcDriver(connStrName)).newInstance();
			//_conn = DriverManager.getConnection(dbConnStr.getConnStr(connStrName));
			int cntRetry = 10;
			String connStr = dbConnStr.getConnStr(connStrName);
			do {
				try {
					_conn = DriverManager.getConnection(connStr);
				} catch (SQLRecoverableException e) { 
					e.printStackTrace();
					Thread.sleep(3*1000);
					--cntRetry;
				}
			} while (_conn==null && cntRetry>0);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	public int getInt (String sql, String retCol, Object... para) {
		showSql(sql, para);
		int ret = 0;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _conn.prepareStatement(sql);
			for (int i=0; i<para.length; i++) {
				if (para[i] instanceof String)
					stmt.setString(i+1, (String)para[i]);
				else
					stmt.setInt(i+1, (int)para[i]);
			}
			result = stmt.executeQuery();
			if (result.next()) {
				ret = result.getInt(retCol);
			}
		} catch (SQLException e) {    
			showSql(sql, para);
			while (e != null) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				e = e.getNextException();
			}
		} finally {	
			try {
				if (result != null) { result.close(); result = null; }	
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (Exception e) { 
			}
		}//finally
		return ret;
	}

	public String getText (String sql, String retCol, Object... para) {
		showSql(sql, para);
		String str = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _conn.prepareStatement(sql);
			for (int i=0; i<para.length; i++) {
				if (para[i] instanceof String)
					stmt.setString(i+1, (String)para[i]);
				else
					stmt.setInt(i+1, (int)para[i]);
			}
			result = stmt.executeQuery();
			if (result.next()) {
				str = result.getString(retCol);
			}
		} catch (SQLException e) {    
			showSql(sql, para);
			while (e != null) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				e = e.getNextException();
			}
		} finally {	
			try {
				if (result != null) { result.close(); result = null; }	
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (Exception e) { 
			}
		}//finally
		return str!=null? str:"";
	}

	public List<String> getTextList (String sql, String retCol, Object... para) {
		showSql(sql, para);
		ArrayList<String> strList = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _conn.prepareStatement(sql);
			for (int i=0; i<para.length; i++) {
				if (para[i] instanceof String)
					stmt.setString(i+1, (String)para[i]);
				else
					stmt.setInt(i+1, (int)para[i]);
			}
			result = stmt.executeQuery();
			while (result.next()) {
				if (null==strList) strList = new ArrayList<String>();
				strList.add(result.getString(retCol));
			}
		} catch (SQLException e) {  
			showSql(sql, para);
			while (e != null) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				e = e.getNextException();
			}
		} finally {	
			try {
				if (result != null) { result.close(); result = null; }	
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (Exception e) { 
			}
		}//finally
		return strList;
	}

	public List<StringIntPair> getPairList (String sql, Object... para) {
		showSql(sql, para);
		ArrayList<StringIntPair> paList = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _conn.prepareStatement(sql);
			for (int i=0; i<para.length; i++) {
				if (para[i] instanceof String)
					stmt.setString(i+1, (String)para[i]);
				else
					stmt.setInt(i+1, (int)para[i]);
			}
			result = stmt.executeQuery();
			while (result.next()) {
				if (null==paList) paList = new ArrayList<StringIntPair>();
				paList.add(new StringIntPair(result.getString(1),result.getInt(2)));
			}
		} catch (SQLException e) {  
			showSql(sql, para);
			while (e != null) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				e = e.getNextException();
			}
		} finally {	
			try {
				if (result != null) { result.close(); result = null; }	
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (Exception e) { 
			}
		}//finally
		return paList;
	}

	public List<StringIntPair> getStringIntList (String sql, Object... para) {
		showSql(sql, para);
		ArrayList<StringIntPair> strList = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _conn.prepareStatement(sql);
			for (int i=0; i<para.length; i++) {
				if (para[i] instanceof String)
					stmt.setString(i+1, (String)para[i]);
				else
					stmt.setInt(i+1, (int)para[i]);
			}
			result = stmt.executeQuery();
			while (result.next()) {
				if (null==strList) strList = new ArrayList<StringIntPair>();
				strList.add(new StringIntPair(result.getString(1),result.getInt(2)));
			}
		} catch (SQLException e) {  
			showSql(sql, para);
			while (e != null) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				e = e.getNextException();
			}
		} finally {	
			try {
				if (result != null) { result.close(); result = null; }	
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (Exception e) { 
			}
		}//finally
		return strList;
	}

	public Connection getConn () {
		return _conn;
	}

	public void showSql (String sql, Object... para) {
		String msg = sql;
		for (int i=0; i<para.length; i++) msg += ";"+para[i];
		System.out.println(msg);
	}

}
