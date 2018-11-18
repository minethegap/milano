import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import sip.util.*;
import common.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;
import sipservertype.*;


class ToPrintMulti {
	static boolean sqlLogEnabled = true;

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 1) {
			String batchId = args[0];
			try {
				(new ToPrintMulti(batchId)).start();
			} catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("ToPrintMulti <batchId>");
			rc += 15;
		}
		System.exit(rc);
	}

	String _batchId;
	String _sqlToPrint;
	SipserverPortType _port;

	String _connName = "@jdbc_TGL";

	static final javax.xml.namespace.QName SERVICE_NAME = new javax.xml.namespace.QName("urn:sipserverType", "sipserver");

	public ToPrintMulti (String batchId) throws Exception {
		_batchId = batchId;
		_sqlToPrint = (new SysVar()).getSql("ToPrint.sql");

		File wsdlFile = new File("c:\\sip\\cfg\\sipserver2_lb.wsdl");
		if (!wsdlFile.exists()) wsdlFile = new File("c:\\sip\\cfg\\sipserver2.wsdl");
		java.net.URL wsdlURL = null;
		try {
			wsdlURL = wsdlFile.toURI().toURL();
		} catch (java.net.MalformedURLException e) {
            e.printStackTrace();
        }
		Sipserver ss = new Sipserver(wsdlURL, SERVICE_NAME);
		_port = ss.getSipserver();

		//enable MTOM
		BindingProvider bp = (BindingProvider) _port;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
	}

	public boolean start () throws Exception {

		boolean rc = true;

		Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		ResultSet result = null;
 
		String sqlSelect = 
			new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\ToPrintShortNames.sql")));

		try {	
			int cnt = 0;

			String shortName, prevShortName = "";
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			conn = (new Sql(_connName)).getConn();

			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, sqlSelect) : conn.prepareStatement(sqlSelect);

			stmtSelect.setString(1, _batchId);

			if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)stmtSelect).getQueryString());
		    } else {         	
		    	System.out.format("%s%n", sqlSelect);
		    }

			result = stmtSelect.executeQuery();

			while (result.next()) {

				shortName = result.getString(1);

				toPrint(shortName);

				++cnt;
			}

			System.out.format("wait %d job(s) to finish...%n", cnt);

			cnt = 120; //120 min timeout
			while (cnt-- > 0) {
					result.close(); result = null;
        			
        			Thread.sleep(60*1000);//1 min

        			stmtSelect.setString(1, _batchId);

					result = stmtSelect.executeQuery();
					if (!result.next()) break;//finished

					shortName = result.getString(1);
					if (!prevShortName.equals(shortName)) {
						prevShortName = shortName;
						System.out.format("%s wait %s%n", sdf.format(new java.util.Date()), shortName);
					}
        		}

		} catch (Exception e) {
            rc = false;
			e.printStackTrace();
		} finally {
			if (result != null) { result.close(); result = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
		}
		
		return rc;
	}

	void toPrint (String shortName) throws Exception {
		String sqlStr = _sqlToPrint.replace("?", 
			String.format("'%s' and f.short_name='%s'", _batchId, shortName) );

		DataHandler reqFile = new DataHandler(new ByteArrayDataSource(sqlStr.getBytes("UTF-8"), "application/octet-stream"));

		 				Holder<String> jobId = new Holder<String>();
						Holder<String> error = new Holder<String>();
						Holder<DataHandler> resFile = new Holder<DataHandler>();

						String jobName = String.format("ToPrint %s %s", _batchId, shortName);

						_port.sendJobAndGetJobFile("", "ToPrintMulti", "VP_TO_PRINT", jobName, reqFile, -2, "", null,
							jobId, error, resFile);

						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						System.out.format("%s %s, jobId=%s, error=%s%n", sdf.format(new java.util.Date()), jobName, jobId.value, error.value);

	}
	
}
