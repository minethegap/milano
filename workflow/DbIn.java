//DbIn.java
//
//read data(clob) from database and send (via sip web service) it to vp
//
//config file: dbin.xml
// 

import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.net.*;
import javax.xml.namespace.QName;
import javax.jws.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;
import sipservertype.*;
import sip.util.*;
import common.*;

class DbIn {
	static boolean sqlLogEnabled = true;

	public static void main (String[] args) {
		int rc = 0x00010000;

		if (args.length >= 1) {
			try {
				String dbinName = args[0];
/*
				System.out.println("Working directory=" + System.getProperty("user.dir"));

				//read the dbin.xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File("..\\cfg\\dbin.xml"));

				Node dbinNode = null;
				NodeList nl = dom.getElementsByTagName("dbin");
				for (int i=0; i<nl.getLength(); ++i) {
					Node n = nl.item(i);
					//System.out.println(Xml.getAttrText(n, "name"));
					if (dbinName.equals(Xml.getAttrText(n, "name"))) {
						dbinNode = n;
						break;
					}
				}

				if (dbinNode != null) {
					String connName = Xml.getNodeText(dbinNode, "connName");
					String sqlSelect = Xml.getNodeText(dbinNode, "sqlSelect");
					String sqlInsert = Xml.getNodeText(dbinNode, "sqlInsert");
					String sipURL = Xml.getNodeText(dbinNode, "sipURL");
					String vp = Xml.getNodeText(dbinNode, "vp");
					String scanIntStr = Xml.getNodeText(dbinNode, "scanInt");
					String maxJobsStr = Xml.getNodeText(dbinNode, "maxJobs");
					String mergeStr = Xml.getNodeText(dbinNode, "merge");
					String mergeSizeMaxStr = Xml.getNodeText(dbinNode, "mergeSizeMax");
					int scanInt = 30; //secs
					int maxJobs = 0;
					int merge = 0;
					int mergeSizeMax = 200; //MB

					if (!Utils.isEmpty(scanIntStr))
						scanInt = Integer.parseInt(scanIntStr);
					if (!Utils.isEmpty(maxJobsStr))
						maxJobs = Integer.parseInt(maxJobsStr);
					if (!Utils.isEmpty(mergeStr))
						merge = Integer.parseInt(mergeStr);
					if (!Utils.isEmpty(mergeSizeMaxStr))
						mergeSizeMax = Integer.parseInt(mergeSizeMaxStr);

					if (Utils.isEmpty(sipURL))
						sipURL = "http://127.0.0.1:5999";
					if (Utils.isEmpty(vp))
						vp = "VP";

					if (Utils.isEmpty(connName))
						System.err.println("connName not defined");
					if (Utils.isEmpty(sqlSelect))
						System.err.println("sqlSelect not defined");

					if (!Utils.isEmpty(connName) && !Utils.isEmpty(sqlSelect)) {
						int cnt = (new DbIn(dbinName, connName, sqlSelect, sqlInsert, sipURL, vp, scanInt, maxJobs, merge, mergeSizeMax)).start();
						if (cnt >= 0)
							System.out.println("Total jobs=" + cnt);
						else {
							rc += 22;
						}
					}
					else {
						rc += 21;
					}
				}
				else {
					System.err.println(dbinName + " not defined in dbin.xml");
					rc += 20;
				}
*/
				DbInPara dbInPara = new DbInPara(dbinName);
				if (!Utils.isEmpty(dbInPara.connName) && !Utils.isEmpty(dbInPara.sqlSelect)) {
						int cnt = (new DbIn(dbinName, dbInPara.connName, dbInPara.sqlSelect, dbInPara.sqlInsert, dbInPara.sipURL, dbInPara.vp, dbInPara.scanInt, dbInPara.maxJobs, dbInPara.merge, dbInPara.mergeSizeMax)).start();
						if (cnt >= 0)
							System.out.println("Total jobs=" + cnt);
						else {
							rc += 22;
						}
					}
					else {
						rc += 21;
					}
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("DbIn <name_in_dbin_xml>");
			rc += 15;
		}

		System.exit(rc);
	}
	

	static final QName SERVICE_NAME = new QName("urn:sipserverType", "sipserver");

	String _batchId=null;

	String _dbinName;
	String _connName;
	String _sqlSelect;
	String _sqlInsert;
	String _sipURL;
	String _vp;
	int _scanInt;
	int _maxJobs;
	int _merge;
	int _mergeSizeMax;
	//int _apAttrs;

	DocumentBuilder _db = null;
	java.text.SimpleDateFormat _sdf;

	public DbIn (String dbinName, String connName, String sqlSelect, String sqlInsert, String sipURL, String vp, int scanInt, int maxJobs, int merge, int mergeSizeMax) {
	//	System.out.format("dbinName=%s, connName=%s,%nsqlSelect=%s,%nsqlInsert=%s,%nsipURL=%s, vp=%s, scanInt=%d, maxJobs=%d, merge=%d, mergeSizeMax=%d%n",
	//			dbinName, connName, sqlSelect, sqlInsert, sipURL, vp, scanInt, maxJobs, merge, mergeSizeMax);
		_dbinName = dbinName;
		_connName = connName;
		_sqlSelect = (new sip.util.SysVar()).repSql(sqlSelect);
		_sqlInsert = sqlInsert;
		_sipURL = sipURL;
		_vp = vp;
		_scanInt = scanInt; //scan db per xxx sec(s), scan once if scanInt==0
		_maxJobs = maxJobs; //read max. xxx jobs per scan, read to eof if maxJobs==0
		_merge = merge;		//merge multi xml(s) into one if merge > 0
		_mergeSizeMax = mergeSizeMax * 1024 * 1024;

		_sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void setBatchId (String batchId) {
		_batchId = batchId;
	}

	public int start () throws Exception {
		return _merge==0? startOne():startMerge();
	}

	public int startOne () throws Exception {
		int rc = -1;
		int jobs = 0;

		Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		PreparedStatement stmtInsert = null;
  		ResultSet result = null;

		try {
			String ipAddrStr = "";
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ipAddr = addr.getAddress();
			for (int i=0; i<ipAddr.length; i++) {
				if (i > 0) ipAddrStr += ".";
				ipAddrStr += ipAddr[i]&0xFF;
			}

			DbConnStr dc = new DbConnStr();
			String strDB = dc.getConnStr(_connName);
			String strDrv = dc.getJdbcDriver(_connName);

			Class.forName(strDrv).newInstance();

			URL wsdlURL = new URL(_sipURL+"/?sipserver2.wsdl");
			Sipserver ss = new Sipserver(wsdlURL, SERVICE_NAME);
			SipserverPortType port = ss.getSipserver();

			//enable MTOM
			BindingProvider bp = (BindingProvider) port;
			SOAPBinding binding = (SOAPBinding) bp.getBinding();
			binding.setMTOMEnabled(true);

			do {
				int cnt = 0;

				conn = DriverManager.getConnection(strDB);
				conn.setAutoCommit(false);

				stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);
				if (!Utils.isEmpty(_sqlInsert))
					stmtInsert = sqlLogEnabled? new LoggableStatement(conn, _sqlInsert) : conn.prepareStatement(_sqlInsert);

				java.util.Date stSta = new java.util.Date();
				result = stmtSelect.executeQuery();
				java.util.Date stEnd = new java.util.Date();
				System.out.format("Query start=%s, end=%s, duration=%dms%n", _sdf.format(stSta), _sdf.format(stEnd), stEnd.getTime()-stSta.getTime());
				
				while (result.next()) {
					String jdt = null;
					int pri = 0;
					String attr = null;
					String attr2 = null;
	 				String rptType = result.getString(1);
	 				long rptNo = result.getLong(2);
	 				Clob rptData = result.getClob(3);
	 				byte[] baData = rptData==null? new byte[0] : clobString(rptData,rptNo).getBytes("UTF-8");
	 				try {
						pri = result.getInt("pri");
					} catch (SQLException e) {
					}
					try {
						attr = result.getString("attr");
					} catch (SQLException e) {
					}
					try {
						attr2 = result.getString("attr2");
					} catch (SQLException e) {
					}
					try {
						jdt = result.getString("jdt");
					} catch (SQLException e) {
					}

	 				System.out.format("rptType=%s, rptNo=%d, rptData char(s)=%d, len=%d%n", rptType, rptNo, rptData==null? 0:rptData.length(), baData.length);

	 				DataHandler reqFile = new DataHandler(new ByteArrayDataSource(baData, "application/octet-stream"));

	 				Holder<String> jobId = new Holder<String>();
					Holder<String> error = new Holder<String>();
					Holder<DataHandler> resFile = new Holder<DataHandler>();

					port.sendJobAndGetJobFile(ipAddrStr, Utils.isEmpty(_batchId)?_dbinName:_batchId, "Y".equals(jdt)? _vp+"_JDT":_vp, attr==null? rptType+"-"+rptNo : rptType+"-"+rptNo+"-"+attr, reqFile, pri==0?-2:0xfffe|(pri<<16), "", null,
						jobId, error, resFile);

					System.out.format("%s jobId=%s, error=%s%n", _sdf.format(new java.util.Date()), jobId.value, error.value);

					if (stmtInsert != null && !Utils.isEmpty(jobId.value)) {
						String hostCode = "01";
						String spoolName;
						long jobNo;
						int subjobNo = 1;//** 0 or 1 ???
						//jobId is hostCode*spoolName#jobNo
						int li = jobId.value.lastIndexOf('#');
						jobNo = Long.parseLong(jobId.value.substring(li+1));
						spoolName = jobId.value.substring(0, li);
						li = spoolName.indexOf('*');
						if (-1 != li) {
							hostCode = spoolName.substring(0, li);
							spoolName = spoolName.substring(li+1);
						}
						//stmtInsert.clearParameters();
						stmtInsert.setString(1, rptType);
						stmtInsert.setLong(2, rptNo);
						stmtInsert.setString(3, hostCode);
						stmtInsert.setString(4, spoolName);
						stmtInsert.setLong(5, jobNo);
						stmtInsert.setInt(6, subjobNo);
						stmtInsert.setString(7, attr2);
						if (_sqlInsert.contains("BATCHID")) stmtInsert.setString(8, _batchId);
						stmtInsert.executeUpdate();

						conn.commit();
					}

	 				cnt++;
	 				if (_maxJobs > 0 && cnt >= _maxJobs)
	 					break;
	 			}
	 			//if (stmtInsert != null && cnt>0)
	 			//	conn.commit();

	 			jobs += cnt;

	 			System.out.format("%s Jobs=%d, Total=%d%n", _sdf.format(new java.util.Date()), cnt, jobs);

	 			if (result != null) { result.close(); result = null; }
				if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
				if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
				if (conn != null) { conn.close(); conn = null; }

	 			if (_scanInt > 0)
	 				Thread.sleep(1000*_scanInt);
	 		} while (_scanInt > 0);

	 		rc = jobs;
		}
		catch (Exception e) {
			handleException(e);
		}
		finally {
			if (result != null) { result.close(); result = null; }
			if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
		}

		return rc;
	}

	public int startMerge () throws Exception {
		int rc = -1;
		int jobs = 0;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		_db = dbf.newDocumentBuilder(); 

		Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		PreparedStatement stmtInsert = null;
  		ResultSet result = null;

		try {
			String ipAddrStr = "";
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ipAddr = addr.getAddress();
			for (int i=0; i<ipAddr.length; i++) {
				if (i > 0) ipAddrStr += ".";
				ipAddrStr += ipAddr[i]&0xFF;
			}

			DbConnStr dc = new DbConnStr();
			String strDB = dc.getConnStr(_connName);
			String strDrv = dc.getJdbcDriver(_connName);

			Class.forName(strDrv).newInstance();

			URL wsdlURL = new URL(_sipURL+"/?sipserver2.wsdl");
			Sipserver ss = new Sipserver(wsdlURL, SERVICE_NAME);
			SipserverPortType port = ss.getSipserver();

			//enable MTOM
			BindingProvider bp = (BindingProvider) port;
			SOAPBinding binding = (SOAPBinding) bp.getBinding();
			binding.setMTOMEnabled(true);

			do {
				int cnt = 0;

				conn = DriverManager.getConnection(strDB);
				conn.setAutoCommit(false);

				stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);
				if (!Utils.isEmpty(_sqlInsert))
					stmtInsert = sqlLogEnabled? new LoggableStatement(conn, _sqlInsert) : conn.prepareStatement(_sqlInsert);

				java.util.Date stSta = new java.util.Date();
				result = stmtSelect.executeQuery();
				java.util.Date stEnd = new java.util.Date();
				System.out.format("Query start=%s, end=%s, duration=%dms%n", _sdf.format(stSta), _sdf.format(stEnd), stEnd.getTime()-stSta.getTime());

				while (result.next()) {
					String jdt = null;
					int pri = 0;
					String attr = null;
					String attr2 = null;
	 				String rptType = result.getString(1);
	 				long rptNo = result.getLong(2);
	 				Clob rptData = result.getClob(3);
	 				String strData = rptData==null? "" : clobString(rptData,rptNo);
	 				try {
						pri = result.getInt("pri");
					} catch (SQLException e) {
					}
					try {
						attr2 = result.getString("attr2");
					} catch (SQLException e) {
					}
					try {
						jdt = result.getString("jdt");
					} catch (SQLException e) {
					}
	 			
	 				System.out.format("rptType=%s, rptNo=%d, rptData char(s)=%d%n", rptType, rptNo, rptData==null? 0:rptData.length());

	 				if (!"FMT_POS_0190".equals(rptType) && isVaildXml(strData))
	 					appendData(ipAddrStr, port, stmtInsert, rptType, rptNo, strData, pri, attr2, jdt);
	 				else {
	 					DataHandler reqFile = new DataHandler(new ByteArrayDataSource(strData.getBytes("UTF-8"), "application/octet-stream"));

	 				Holder<String> jobId = new Holder<String>();
					Holder<String> error = new Holder<String>();
					Holder<DataHandler> resFile = new Holder<DataHandler>();

					port.sendJobAndGetJobFile(ipAddrStr, Utils.isEmpty(_batchId)?_dbinName:_batchId, "Y".equals(jdt)? _vp+"_JDT":_vp, attr==null? rptType+"-"+rptNo : rptType+"-"+rptNo+"-"+attr, reqFile, pri==0?-2:0xfffe|(pri<<16), "", null,
						jobId, error, resFile);

					System.out.format("%s jobId=%s, error=%s%n", _sdf.format(new java.util.Date()), jobId.value, error.value);

					if (stmtInsert != null && !Utils.isEmpty(jobId.value)) {
						String hostCode = "01";
						String spoolName;
						long jobNo;
						int subjobNo = 1;//** 0 or 1 ???
						//jobId is hostCode*spoolName#jobNo
						int li = jobId.value.lastIndexOf('#');
						jobNo = Long.parseLong(jobId.value.substring(li+1));
						spoolName = jobId.value.substring(0, li);
						li = spoolName.indexOf('*');
						if (-1 != li) {
							hostCode = spoolName.substring(0, li);
							spoolName = spoolName.substring(li+1);
						}
						//stmtInsert.clearParameters();
						stmtInsert.setString(1, rptType);
						stmtInsert.setLong(2, rptNo);
						stmtInsert.setString(3, hostCode);
						stmtInsert.setString(4, spoolName);
						stmtInsert.setLong(5, jobNo);
						stmtInsert.setInt(6, subjobNo);
						stmtInsert.setString(7, attr2);
						if (_sqlInsert.contains("BATCHID")) stmtInsert.setString(8, _batchId);
						stmtInsert.executeUpdate();

						conn.commit();
					}

	 				}//"else {" of "if (isVaildXml(strData))"

	 				cnt++;
	 				if (_maxJobs > 0 && cnt >= _maxJobs)
	 					break;
	 			}

	 			if (_mergeCnt > 0) {
					sendData(ipAddrStr, port, stmtInsert);
				}

	 			//if (stmtInsert != null && cnt>0)
	 			//	conn.commit();

	 			jobs += cnt;

	 			System.out.format("%s Jobs=%d, Total=%d%n", _sdf.format(new java.util.Date()), cnt, jobs);

	 			if (result != null) { result.close(); result = null; }
				if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
				if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
				if (conn != null) { conn.close(); conn = null; }

	 			if (_scanInt > 0)
	 				Thread.sleep(1000*_scanInt);
	 		} while (_scanInt > 0);

	 		rc = jobs;
		}
		catch (Exception e) {
			handleException(e);
		}
		finally {
			if (result != null) { result.close(); result = null; }
			if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
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

	boolean isVaildXml (String xmlStr) {
		boolean rc = false;

		try {
			_db.parse(new org.xml.sax.InputSource(new StringReader(xmlStr)));
			rc = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return rc;
	}


	int _jobSeq = 0;
	int _mergeCnt = 0; 
	int _mergeSize = 0; 
	String _curRptType = null;
	long[] _arrRptNo = null;
	String[] _arrAttr2 = null;
	StringBuilder _sbRptData = null;
	int _pri = 0;
	String _jdt = null;

	void appendData (String ipAddrStr, SipserverPortType port, PreparedStatement stmtInsert, String rptType, long rptNo, String strData, int pri, String attr2, String jdt) throws Exception {
		if (_mergeCnt >= _merge || _mergeSize >= _mergeSizeMax ||
				!rptType.equals(_curRptType) && _mergeCnt > 0) {
			sendData(ipAddrStr, port, stmtInsert);
		}

		if (_mergeCnt==0) {
			if (_arrRptNo==null) _arrRptNo = new long[_merge];
			if (_arrAttr2==null) _arrAttr2 = new String[_merge];
			_curRptType = rptType;
			_sbRptData = new StringBuilder("<JOB>");
			_pri = pri;
			_jdt = jdt;
		}
		_sbRptData.append(String.format("<DOC _SubjobDesc=\"%s-%d\">", rptType, rptNo));
		_sbRptData.append(xmlStr(strData));
		_sbRptData.append("</DOC>");
		_arrAttr2[_mergeCnt] = attr2;
		_arrRptNo[_mergeCnt++] = rptNo;
		_mergeSize += strData.length();
	}

	String xmlStr (String str) {
		int idx2, idx = str.indexOf("<?");
		if (idx >= 0 && (idx2 = str.indexOf("?>")) > idx)
			return str.substring(idx2 + 2);
		return str;	
	}

	void sendData (String ipAddrStr, SipserverPortType port, PreparedStatement stmtInsert) throws Exception {
		_sbRptData.append("</JOB>");
		byte[] baData = _sbRptData.toString().getBytes("UTF-8");

		DataHandler reqFile = new DataHandler(new ByteArrayDataSource(baData, "application/octet-stream"));

	 	Holder<String> jobId = new Holder<String>();
		Holder<String> error = new Holder<String>();
		Holder<DataHandler> resFile = new Holder<DataHandler>();

		port.sendJobAndGetJobFile(ipAddrStr, Utils.isEmpty(_batchId)?_dbinName:_batchId, "Y".equals(_jdt)? _vp+"_JDT":_vp, _curRptType+"-"+(++_jobSeq)+"-"+_mergeCnt, reqFile, _pri==0?-2:0xfffe|(_pri<<16), "", null,
						jobId, error, resFile);

		System.out.format("%s jobId=%s, error=%s%n", _sdf.format(new java.util.Date()), jobId.value, error.value);

		if (stmtInsert != null && !Utils.isEmpty(jobId.value)) {
					String hostCode = "01";
					String spoolName;
					long jobNo;
						
					//jobId is hostCode*spoolName#jobNo
					int li = jobId.value.lastIndexOf('#');
					jobNo = Long.parseLong(jobId.value.substring(li+1));
					spoolName = jobId.value.substring(0, li);
					li = spoolName.indexOf('*');
					if (-1 != li) {
						hostCode = spoolName.substring(0, li);
						spoolName = spoolName.substring(li+1);
					}
					for (int subjobNo=0; subjobNo<_mergeCnt; ++subjobNo) {
						//stmtInsert.clearParameters();
						stmtInsert.setString(1, _curRptType);
						stmtInsert.setLong(2, _arrRptNo[subjobNo]);
						stmtInsert.setString(3, hostCode);
						stmtInsert.setString(4, spoolName);
						stmtInsert.setLong(5, jobNo);
						stmtInsert.setInt(6, 1+subjobNo);
						stmtInsert.setString(7, _arrAttr2[subjobNo]);
						if (_sqlInsert.contains("BATCHID")) stmtInsert.setString(8, _batchId);
						stmtInsert.executeUpdate();
					}
					stmtInsert.getConnection().commit();
		}			

		_mergeCnt = 0;
		_mergeSize = 0;
	}


	String clobString (Clob rptData, long pk) {
		String retStr = "";
		try {
			retStr = rptData.getSubString(1, (int)rptData.length());
		} catch (Exception e) {
			System.out.format("error to get string of list_id %d:%n", pk);
			e.printStackTrace();
		}
		return retStr;
	}

}
