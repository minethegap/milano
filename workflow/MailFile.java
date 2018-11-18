

import java.io.*;
import java.sql.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.activation.*;
//import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

//import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
//import org.apache.cxf.interceptor.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import com.tgl.esp.ws.server.service.*;

import sip.util.*;
import common.*;

public class MailFile {
	
	public static void main (String args[]) throws Exception {
        int rc = 0x00010000;

        if (args.length >= 3) { 

            String xmlFile = args[0];
            String pdfFile = args[1];
            String jobName = args[2];
            
            System.out.println("attach="+pdfFile);

            Connection conn = null;
            PreparedStatement stmtInsert = null;
          
            try {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); 
                Document dom = db.parse(new File(xmlFile));


                String policyCode = "";
                String policyHolderId = "";
                NodeList nl = dom.getElementsByTagName("policyCode");
                if (nl != null && nl.getLength() > 0) policyCode = nl.item(0).getTextContent();
                nl = dom.getElementsByTagName("policyHolderId");
                if (nl != null && nl.getLength() > 0) policyHolderId = nl.item(0).getTextContent();

                String sendingMethod = "";
                nl = dom.getElementsByTagName("sendingMethod");
                if (nl != null && nl.getLength() > 0) sendingMethod = nl.item(0).getTextContent();

                nl = dom.getElementsByTagName("emailReceiver");

                if ( ("1".equals(sendingMethod) || "3".equals(sendingMethod)) && nl != null && nl.getLength() > 0) {

                String shortName = dom.getElementsByTagName("shortName").item(0).getTextContent();
                String tplId = getTplId(shortName);	

                String subject = jobName;
                String body = "";

                File wsdlFile = new File("c:\\sip\\cfg\\ESP.xml");
                MailManagementWSService ss = new MailManagementWSService(wsdlFile.toURI().toURL(), new QName("http://service.server.ws.esp.tgl.com/", "MailManagementWSService"));
        		MailManagementWS port = ss.getMailManagementWSPort();  

                com.tgl.esp.ws.server.service.RawTemplateContentRequest _getRawTemplateByID_arg0 = new com.tgl.esp.ws.server.service.RawTemplateContentRequest();
                _getRawTemplateByID_arg0.setTemplateId(tplId);
                com.tgl.esp.ws.server.service.TemplateContentResponse _getRawTemplateByID__return = port.getRawTemplateByID(_getRawTemplateByID_arg0);
                System.out.println("getRawTemplateByID.result=" + _getRawTemplateByID__return);
                TemplateContent tc = _getRawTemplateByID__return.getTemplateContent();
                subject = tc.getSubject();
                body = tc.getBody();

                if (Utils.isEmpty(subject)) subject = jobName;
                if (Utils.isEmpty(body)) body = "";

                String attName = getFileName(pdfFile);

                BatchMail bm = new BatchMail(jobName);
                bm.setSubject(subject);
                bm.setBody(body);
                bm.setAttach(pdfFile);

                DbConnStr dc = new DbConnStr();
                String strDB = dc.getConnStr("jdbc_ESP");
                String strDrv = dc.getJdbcDriver("jdbc_ESP");

                Class.forName(strDrv).newInstance();

                conn = DriverManager.getConnection(strDB);
                conn.setAutoCommit(false);

                stmtInsert = conn.prepareStatement("INSERT INTO esp.CUST_CONTACT_LOG (CONTACT_SYS_ID,CONTACT_SYS_OPERATOR_ID," + 
"FUNCTION_CATEGORY,REPORT_ID,POLICY_NO,PROPOSER_ID,CONTACT_METHOD,CONTACT_END_POINT_VAL,CONTACT_DATE_TIME,CONTACT_SUBJECT,CONTACT_BODY)" +
"VALUES ('FMT','SIP',?,?,?,?,'EMAIL',?,?,?,?)");

                bm.getListFile().add(String.format("%s,%s", "EmailAddress", "Attachment"));
                
                for (int i = 0; i < nl.getLength(); i++) {
                    String emailAddr;
                    Node n = nl.item(i);
                    String list = String.format("%s,%s", emailAddr=n.getTextContent(), attName);
                    bm.getListFile().add(list);
                    System.out.println(list);

                    stmtInsert.setString(1,get_dept(shortName));
                    stmtInsert.setString(2,shortName);
                    stmtInsert.setString(3,policyCode);
                    stmtInsert.setString(4,policyHolderId);
                    stmtInsert.setString(5,emailAddr);
                    stmtInsert.setDate(6, new java.sql.Date(System.currentTimeMillis()) );
                    stmtInsert.setString(7,subject);
                    stmtInsert.setString(8,body);

                    stmtInsert.executeUpdate();
                }

                conn.commit();

                bm.commit();

            	}

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
            finally {
                if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
                if (conn != null) { conn.close(); conn = null; }
            }
        }
        else {
            System.out.println("MailFile <xml> <pdf> <job_name>");
            rc += 15;
        }

        System.exit(rc);
    }

    static String getFileName (String s) {
    	int idx = s.lastIndexOf('\\');
    	if (idx >= 0)
    		return s.substring(idx+1);
    	return s;	
    }

    static String getExtName (String s) {
    	int idx = s.lastIndexOf('.');
    	if (idx >= 0)
    		return s.substring(idx+1);
    	return "";	
    }

    static String getTplId_old (String sn) throws Exception {
        au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader("c:\\sip\\cfg\\emailTPL.txt"));
    
        String[] nextLine;
      	while ((nextLine = reader.readNext()) != null) {
      		if (sn.equals(nextLine[0]))
      			return nextLine[1];
        }
       	return "";
    }

    static String getTplId (String sn) throws Exception {
        au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader("c:\\sip\\cfg\\emailTPL.txt"));
    
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (sn.equals(nextLine[0]))
                return nextLine[1];
        }
        return "";
    }

    static String get_dept (String shortName) {
        String dept = "";
        int idx = shortName.indexOf('_');
        if (idx >= 0) {
            shortName = shortName.substring(idx+1);
            idx = shortName.indexOf('_');   
            if (idx >= 0) {
                dept = shortName.substring(0,idx);
            }
        }
        return dept;
    }
}
