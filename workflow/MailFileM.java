

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

public class MailFileM {
	
	public static void main (String args[]) throws Exception {
        int rc = 0x00010000;

        if (args.length >= 4) { 

            String xmlFile = args[0];
            String pdfFolder = args[1];
            String relPdfFolder = args[2];
            String jobName = args[3];
            int subjobNo = 0;
            int done = 0;
            boolean testMode = false;

            if (args.length > 4 && "TEST".equals(args[4]))
                testMode = true; 

            String shortName = null;
            String deptId = null;

            BatchMail bm = null;

            String subject = null;
            String body = null;
            
            Connection conn = null;
            PreparedStatement stmtInsert = null;
          
            try {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); 
                Document dom = db.parse(new File(xmlFile));

                Node root = dom.getDocumentElement();
                Node subjob = root.getFirstChild();

                while (subjob != null) {
                    if (subjob.getNodeType()==Node.ELEMENT_NODE) {
                        ++subjobNo;
                        String sendingMethod = "";
                        NodeList nl = ((Element)subjob).getElementsByTagName("sendingMethod");
                        if (nl != null && nl.getLength() > 0) sendingMethod = nl.item(0).getTextContent();
                        if ("1".equals(sendingMethod) || "3".equals(sendingMethod)) {
                            nl = ((Element)subjob).getElementsByTagName("emailReceiver");    
                            if (nl != null && nl.getLength() > 0) {
                                if (bm == null) {
                                    int idx = jobName.indexOf('-');
                                    shortName = jobName.substring(0,idx);
                                    deptId = get_dept(shortName);
                                    String tplId = getTplId(shortName); 

                                    File wsdlFile = new File("c:\\sip\\cfg\\ESP.xml");
                                    MailManagementWSService ss = new MailManagementWSService(wsdlFile.toURI().toURL(), new QName("http://service.server.ws.esp.tgl.com/", "MailManagementWSService"));
                                    MailManagementWS port = ss.getMailManagementWSPort();  

                                    com.tgl.esp.ws.server.service.RawTemplateContentRequest _getRawTemplateByID_arg0 = new com.tgl.esp.ws.server.service.RawTemplateContentRequest();
                                    _getRawTemplateByID_arg0.setTemplateId(tplId);
                                    if (!testMode) {
                                    com.tgl.esp.ws.server.service.TemplateContentResponse _getRawTemplateByID__return = port.getRawTemplateByID(_getRawTemplateByID_arg0);
                                    System.out.println("getRawTemplateByID.result=" + _getRawTemplateByID__return);
                                    TemplateContent tc = _getRawTemplateByID__return.getTemplateContent();
                                    subject = tc.getSubject();
                                    body = tc.getBody();
                                    }

                                    if (Utils.isEmpty(subject)) subject = jobName;
                                    if (Utils.isEmpty(body)) body = "";  

                                    bm = new BatchMail(jobName);
                                    bm.setSubject(subject);
                                    bm.setBody(body);  

                                    bm.getListFile().add(String.format("%s,%s", "EmailAddress", "Attachment"));

                                    DbConnStr dc = new DbConnStr();
                                    String strDB = dc.getConnStr("jdbc_ESP");
                                    String strDrv = dc.getJdbcDriver("jdbc_ESP");

                                    Class.forName(strDrv).newInstance();

                                    conn = DriverManager.getConnection(strDB);
                                    conn.setAutoCommit(false);

                                    stmtInsert = conn.prepareStatement("INSERT INTO esp.CUST_CONTACT_LOG (CONTACT_SYS_ID,CONTACT_SYS_OPERATOR_ID," + 
"FUNCTION_CATEGORY,REPORT_ID,POLICY_NO,PROPOSER_ID,CONTACT_METHOD,CONTACT_END_POINT_VAL,CONTACT_DATE_TIME,CONTACT_SUBJECT,CONTACT_BODY)" +
"VALUES ('FMT','SIP',?,?,?,?,'EMAIL',?,?,?,?)");
                                } //if (bm == null) {  

                                String policyCode = "";
                                String policyHolderId = "";
                                NodeList nl2 = dom.getElementsByTagName("policyCode");
                                if (nl2 != null && nl2.getLength() > 0) policyCode = nl2.item(0).getTextContent();
                                nl2 = dom.getElementsByTagName("policyHolderId");
                                if (nl2 != null && nl2.getLength() > 0) policyHolderId = nl2.item(0).getTextContent();  

                                String documentNo = "";
                                nl2 = ((Element)subjob).getElementsByTagName("documentNo");  
                                if (nl2 != null && nl2.getLength() > 0) documentNo = nl2.item(0).getTextContent(); 
                                else documentNo = policyCode;

                                String attName = String.format("%s-%s-%06x.pdf", documentNo, relPdfFolder.replaceAll("\\\\", "-"), subjobNo);; 
                                String pdfFile = String.format("%s\\%s\\%06x.pdf", pdfFolder, relPdfFolder, subjobNo);
                                bm.setAttach(pdfFile, attName);

                                for (int i = 0; i < nl.getLength(); i++) {
                                    String emailAddr;
                                    Node n = nl.item(i);
                                    String list = String.format("%s,%s", emailAddr=n.getTextContent(), attName);
                                    bm.getListFile().add(list);
                                    System.out.println(list);

                                    if (!testMode) {
                                    stmtInsert.setString(1,deptId);
                                    stmtInsert.setString(2,shortName);
                                    stmtInsert.setString(3,policyCode);
                                    stmtInsert.setString(4,policyHolderId);
                                    stmtInsert.setString(5,emailAddr);
                                    stmtInsert.setDate(6, new java.sql.Date(System.currentTimeMillis()) );
                                    stmtInsert.setString(7,subject);
                                    stmtInsert.setString(8,body);

                                    stmtInsert.executeUpdate();
                                    }
                                }

                                ++done;
                            } //if (nl != null && nl.getLength() > 0) {
                        } //if ("1".equals(sendingMethod) || "3".equals(sendingMethod)) {
                    } //if (subjob.getNodeType()==Node.ELEMENT_NODE) {
                    subjob = subjob.getNextSibling();
                } //while (subjob != null) {

                if (conn != null) conn.commit();    

                if (bm != null) bm.commit();

                System.out.println("done="+done);

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
            System.out.println("MailFileM <xml> <pdf_folder> <rel_pdf_folder> <job_name> [TEST]");
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
