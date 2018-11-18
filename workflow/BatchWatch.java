
import java.io.*;
import java.nio.file.*;
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

class BatchWatch {

	public static void main (String[] args) throws Exception {
		//int rc = 0x00010000;

		Files.newOutputStream(Paths.get("c:\\sip\\spool\\batchwatch.active"), StandardOpenOption.CREATE, StandardOpenOption.DELETE_ON_CLOSE);
		
		SysVar sv = new SysVar();
		String connName = "@jdbc_TGL";
		String batchTblName = args.length >= 1 && !Utils.isEmpty(args[0]) ? args[0]:sv.get("BATCH_TBL","stage.t_fmt_batch@ODSLINK");
		String fmtTblName = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1]:sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
		String pdfFolder = args.length >= 3 && !Utils.isEmpty(args[2]) ? args[2]:sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");
		String tmpFolder = args.length >= 4 && !Utils.isEmpty(args[3]) ? args[3]:sv.get("TMP_FOLDER","D:\\SIPTEMP");

		(new BatchWatch()).start(connName, batchTblName, fmtTblName, pdfFolder, tmpFolder);

		//System.exit(rc);, 
	}

	static final javax.xml.namespace.QName SERVICE_NAME = new javax.xml.namespace.QName("urn:sipserverType", "sipserver");
	
	SipserverPortType _port;

	public void start (String connName, String batchTblName, String fmtTblName, String pdfFolder, String tmpFolder) {

		File wsdlFile = new File("c:\\sip\\cfg\\sipserver2.wsdl");
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


		String sqlSelCmd = "select b.LIST_ID b_listid, bj.* from t_batch_job_etlfct_stg bj " + 
		                    "left join " + batchTblName + " b on bj.LIST_ID=b.LIST_ID " + 
		                    "order by bj.LIST_ID desc";


		String sqlInsCmd = "insert into " + batchTblName + " (LIST_ID) values (?)";
	
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.text.SimpleDateFormat dtFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");

		Connection conn = null;
   		PreparedStatement stmtSel = null;
  		PreparedStatement stmtIns = null;
  		ResultSet result = null;

		System.out.format("%s %s%n", sdf.format(new java.util.Date()), sqlSelCmd);

  		do {

  			try {
  				conn = (new Sql(connName)).getConn();
				conn.setAutoCommit(true);
				stmtSel = conn.prepareStatement(sqlSelCmd);
				result = stmtSel.executeQuery();
				if (result.next()) {
					Object b_listid = result.getObject(1);
					if (null == b_listid) {
						long listId = result.getLong("LIST_ID");
						stmtIns = conn.prepareStatement(sqlInsCmd);
						stmtIns.setLong(1, listId);

						System.out.format("%s %s;%d%n", sdf.format(new java.util.Date()), sqlInsCmd, listId);

						stmtIns.execute();

						run_DoBatch(listId, batchTblName, fmtTblName, pdfFolder, tmpFolder);
					}
				}

  			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (result != null) { result.close(); result = null; }
					if (stmtSel != null) { stmtSel.close(); stmtSel = null; }
					if (stmtIns != null) { stmtIns.close(); stmtIns = null; }
					if (conn != null) { conn.close(); conn = null; }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			try {
  				Thread.sleep(1000*60*5);
  			} catch (Exception e2) {
  				System.out.format("%s ", sdf.format(new java.util.Date()));
				e2.printStackTrace();
			}

		} while (true);				
  			
	}

	void run_DoBatch (long listId, String batchTblName, String fmtTblName, String pdfFolder, String tmpFolder) {

		try {
    					DataHandler reqFile = new DataHandler(new ByteArrayDataSource(new byte[0], "application/octet-stream"));
						//DataHandler reqFile = new DataHandler(new FileDataSource("c:\\sip\\cfg\\runJava_sipjt.xml"));

		 				Holder<String> jobId = new Holder<String>();
						Holder<String> error = new Holder<String>();
						Holder<DataHandler> resFile = new Holder<DataHandler>();

						//String jobName = String.format("DoBatch %d %s %s %s %s", listId, batchTblName, fmtTblName, pdfFolder, tmpFolder);
						//String jobName = String.format("DoBatch %d %s %s", listId, batchTblName, fmtTblName);
						String jobName = String.format("DoBatch %d", listId);

						_port.sendJobAndGetJobFile("local", "BatchWatch", "VP_JAVA", jobName, reqFile, -2, "", null,
							jobId, error, resFile);

						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						System.out.format("%s %s, jobId=%s, error=%s%n", sdf.format(new java.util.Date()), jobName, jobId.value, error.value);

			} catch (Exception e) {
				e.printStackTrace();
			}			

	}

}
