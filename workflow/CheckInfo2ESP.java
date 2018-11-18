import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import common.*;

public class CheckInfo2ESP {
	static boolean sqlLogEnabled = true;

	public static void main (String args[]) {
		int rc = 0x00010000;
		if (args.length >= 3) {
	    	try {	
	    		String xmlFile = args[0];
	    		String jobName = args[1];
	    		String jobId = args[2];
	    		int idx = jobName.lastIndexOf('-');
	    		if (idx>=0) jobName = jobName.substring(idx+1);
	    		new CheckInfo2ESP().do_it(xmlFile, jobName, jobId);   
	    	} catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		} else {
			System.out.println("CheckInfo2ESP <xml> <jobName> <jobId>");
			rc += 15;
		}
		System.exit(rc);
	}

	Connection _connESP = null;
    PreparedStatement _stmtData = null;
    PreparedStatement _stmtBatch = null;

	void do_it (String xmlFile, String jobName, String jobId) throws Exception {
		String batchId = jobName+"_"+jobId;
		//load the input xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(xmlFile));
				NodeList nl = dom.getElementsByTagName("DOC");

		if (nl != null && nl.getLength() > 0) {
			_connESP = (new Sql("@jdbc_ESP")).getConn();
        	_connESP.setAutoCommit(false);

        	String strData="INSERT INTO esp.POSTMAIL_DATA_REQ (REQ_SYS_ID,BATCH_NO,SEQ_NO,POSTMAIL_CATEGORY,OUTSOURCING_POST," + 
	            "MAIL_SEND_MODE,RECEIVER_ID_NO,RECEIVER_TYPE,RECEIVER_NAME,RECEIVER_ZIPCODE,RECEIVER_ADDRESS,REQ_SYS_REF_ID,CREATE_DATA_DATETIME,AUTO_REG_NUM_CODE)" + 
	            "VALUES ('SIP',?,?,?,'Y', ?,?,'01',?,?,?,?,sysdate,?)";
	        String strBatch="INSERT INTO esp.POSTMAIL_DATA_BATCH_REQ (REQ_SYS_ID,BATCH_NO,BATCH_COUNT,CREATE_DATETIME)"+
            	" VALUES ('SIP',?,?,sysdate)";

        	if (sqlLogEnabled) {
        		_stmtData = new LoggableStatement(_connESP, strData);
        		_stmtBatch = new LoggableStatement(_connESP, strBatch);
        	} else {
		        _stmtData = _connESP.prepareStatement(strData);
				_stmtBatch = _connESP.prepareStatement(strBatch);
			}
			int done = 0;
			for (int i = 0; i < nl.getLength(); i++) {
                    
                Node n = nl.item(i);
                NodeList nl2 = ((Element)n).getElementsByTagName("sendInfo");
                if (nl2 != null && nl2.getLength() > 0) {
                	Node sendInfo = nl2.item(0);
                	++done;

                	String reportId = "";
                	NodeList nl3 = ((Element)n).getElementsByTagName("formNo");
                	if (nl3 != null && nl3.getLength() > 0) {
                		reportId = nl3.item(0).getTextContent();
                	}
                	
                	String sendMode = "04";
                	String receiverId = Xml.getNodeText(sendInfo, "receiverId");
                	String receiverName = Xml.getNodeText(sendInfo, "recipientName");
                	String receiverZip = Xml.getNodeText(sendInfo, "sendZipCode");
                	String receiverAddr = Xml.getNodeText(sendInfo, "sendAddress");
                	String postMailRegNumber = Xml.getNodeText(sendInfo, "postMailRegNumber");

                	String uid = String.format("%s_%d", jobId, 1+i);

                	_stmtData.setString(1,batchId);
		            _stmtData.setInt(2,done);
		            _stmtData.setString(3,getStr(reportId));
		            _stmtData.setString(4,getStr(sendMode));
		            _stmtData.setString(5,getStr(receiverId));
		            _stmtData.setString(6,getStr(receiverName));
		            _stmtData.setString(7,getStr(receiverZip));
		            _stmtData.setString(8,getStr(receiverAddr));
		            _stmtData.setString(9,uid);
		            _stmtData.setString(10,postMailRegNumber);
		            if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)_stmtData).getQueryString());
		            }
		            _stmtData.executeUpdate(); 

                }
			}

			_stmtBatch.setString(1,batchId);
                _stmtBatch.setInt(2,done);
                _stmtBatch.executeUpdate(); 
                if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)_stmtBatch).getQueryString());
		            }

                _connESP.commit();
                _connESP.close();
                _connESP = null;
		}
	}

	String getStr (String str) {
        if (Utils.isEmpty(str)) return "null";
        return str;
    }
}