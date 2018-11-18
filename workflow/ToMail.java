//ToMail.java
//
//ToMail <TEST> <sql_name> <tbl_name> <pdf_folder> 

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tgl.esp.ws.server.service.MailManagementWS;
import com.tgl.esp.ws.server.service.MailManagementWSService;
import com.tgl.esp.ws.server.service.TemplateContent;

import common.LoggableStatement;
import common.Sql;
import common.Utils;
import common.WatermarkPDF;
import common.Xml;
import sip.util.SysVar;


public class ToMail {
	
	private static final Logger LOGGER = Logger.getLogger(ToMail.class);
	
	
    static boolean sqlLogEnabled = true;

    private static final String wmPath = "D:\\Case\\TGL\\WM\\";
   
    public static void main (String args[]) {
    	
    	LOGGER.info("Starting ToMail Taks......!!");
    	
    	int rc = 0x00010000;

    	try {	    
	        	String connName = "@jdbc_TGL";

                SysVar sv = new SysVar();

                boolean testMode = args.length >= 1 && "TEST".equals(args[0]) ? true:false;
                String sqlName = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1]:"ToMail.sql";
	        	String fmtTblName = args.length >= 3 && !Utils.isEmpty(args[2]) ? args[2]:sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
				String pdfFolder = args.length >= 4 && !Utils.isEmpty(args[3]) ? args[3]:sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");
	        	
	        	//String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\"+sqlName)));
                String sqlSelect = sv.getSql(sqlName);

                System.out.println(sqlName);
                System.out.println(sqlSelect);
	        	System.out.println(fmtTblName);
                System.out.println(pdfFolder);
	        	
	        	ToMail toMail = new ToMail(connName, sqlSelect, pdfFolder, fmtTblName);
        		
        		while (true) {
        			toMail.start(testMode);
        			Thread.sleep(5*60*1000);
        		}

	    } catch (Exception e) {
				e.printStackTrace();
		}

		rc += 20;
        
        System.exit(rc);
    }


    String _connName;
	String _sqlSelect;
	String _pdfFolder;
	String _fmtTblName;

	PreparedStatement _stmtUpdateEMAILED = null;
	PreparedStatement _stmtUpdateERRMSG = null;

	String _lastErr;

    public ToMail (String connName, String sqlSelect, String pdfFolder, String fmtTblName) {
    	_connName = connName;
    	_sqlSelect = sqlSelect;
    	_pdfFolder = pdfFolder;
    	_fmtTblName = fmtTblName;
    }

    public void start () throws Exception {
    	start(false);
    }

    public void start (boolean testMode) throws Exception {

    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   		MailManagementWSService ss = new MailManagementWSService((new File("c:\\sip\\cfg\\ESP.xml")).toURI().toURL(), new QName("http://service.server.ws.esp.tgl.com/", "MailManagementWSService"));
        MailManagementWS port = ss.getMailManagementWSPort(); 
        //enable MTOM
    	BindingProvider bp = (BindingProvider) port;
    	SOAPBinding binding = (SOAPBinding) bp.getBinding();
    	binding.setMTOMEnabled(true); 

    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 

    	Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		ResultSet result = null;

  		Connection connESP = null;
        PreparedStatement stmtInsert = null;

  		String prev_shortName = null;
  		//String prev_tplId = null;
        String deptId = null;
  		BatchMail bm = null;
  		String subject = null;
        String body = null;

        java.util.List<String> wmFrontList = new java.util.ArrayList<String>();
        java.util.List<String> wmBackList = new java.util.ArrayList<String>();
        WatermarkPDF watermarkPDF = new WatermarkPDF();

        int jobs = 0;
        int done = 0;
        int mails = 0;

		try {
			conn = (new Sql(_connName)).getConn();
			conn.setAutoCommit(false);

			connESP = (new Sql("@jdbc_ESP")).getConn();
			connESP.setAutoCommit(false);

            stmtInsert = connESP.prepareStatement("INSERT INTO esp.CUST_CONTACT_LOG (CONTACT_SYS_ID,CONTACT_SYS_OPERATOR_ID," + 
"FUNCTION_CATEGORY,REPORT_ID,POLICY_NO,PROPOSER_ID,CONTACT_METHOD,CONTACT_END_POINT_VAL,CONTACT_DATE_TIME,CONTACT_SUBJECT,CONTACT_BODY)" +
"VALUES ('FMT','SIP',?,?,?,?,'EMAIL',?,?,?,?)");

            String sqlEMAILED = "update " + _fmtTblName + " set EMAILED=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			String sqlERRMSG = "update " + _fmtTblName + " set ERRMSG=concat(?,ERRMSG) where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
            _stmtUpdateEMAILED = sqlLogEnabled? new LoggableStatement(conn, sqlEMAILED) : conn.prepareStatement(sqlEMAILED);
			_stmtUpdateERRMSG = sqlLogEnabled? new LoggableStatement(conn, sqlERRMSG) : conn.prepareStatement(sqlERRMSG);
			
			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);
			result = stmtSelect.executeQuery();
			while (result.next()) {
				String hostCode = result.getString("HOSTCODE");
				String spoolName = result.getString("SPOOLNAME");
				long jobNo = result.getLong("JOBNO");
				int subjobNo = result.getInt("SUBJOBNO");
				String tplId = result.getString("EMAIL_TPLID");
				String shortName = result.getString("SHORT_NAME");
                long listId = result.getLong("LIST_ID");
                String reportName = result.getString("TEMPLATE_NAME");

                String wmFront = result.getString("WM_EMAIL_FRONT");
                String wmBack = result.getString("WM_EMAIL_BACK");

                String fromDisplay = null;
                String loginName = null;

                String departmentName = Utils.shortNameToDeptName(shortName);

                _lastErr = null;

                try {
                    fromDisplay =  result.getString("EMAIL_FROMDISPLAY");   
                } catch (Exception ef) {};
                try {
                    loginName =  result.getString("LoginName");  //EMAIL_USERID
                } catch (Exception el) {};

				System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",  sdf.format(new java.util.Date()), 
	 				hostCode, spoolName, jobNo, subjobNo, shortName, listId);

                if (Utils.isEmpty(tplId)) tplId = shortName;

				Clob rptData = result.getClob("CONTENT");
	 			byte[] baData = rptData.getSubString(1, (int)rptData.length()).getBytes("UTF-8");

	 			Document dom = db.parse(new ByteArrayInputStream(baData));
	 			
	 			
                ++jobs;

                String emailed = "N";

	 			NodeList nl = dom.getElementsByTagName("emailReceiver");    
                if (nl != null && nl.getLength() > 0) {
                	boolean noTpl = false;
                 	String policyCode = "";
                    String policyHolderId = "";
                    NodeList nl2 = dom.getElementsByTagName("policyCode");
                    if (nl2 != null && nl2.getLength() > 0) policyCode = nl2.item(0).getTextContent();
                    nl2 = dom.getElementsByTagName("policyHolderId");
                    if (nl2 != null && nl2.getLength() > 0) policyHolderId = nl2.item(0).getTextContent();  

                    System.out.format("policyCode=%s,policyHolderId=%s%n", policyCode, policyHolderId);

                    Node omicardInfo = null;
                    nl2 = dom.getElementsByTagName("omicardInfo");
                    if (nl2 != null && nl2.getLength() > 0) omicardInfo = nl2.item(0);
                    String customerPersonID = "";
                    String groupCode = "";
                    String insuranceID = "";
                    String insuranceReportID = "";
                    String loginID = "";
                    String organizationCode = "";
                    String salesName = "";
                    String unitCode = "";
                    String  insuranceReportDate = "";
                    String toDisplay = "親愛的客戶";

                    String ClientRepre = "";
                    String UnderwriterMail = "";
                    String UnderwriterDiv = "";
                    String UnderwriterName = "";
                    String Contact = "";
                    String UWDivision = "";
                    String Underwriter = "";
                    String Tel = "";
					String Ext = "";
                    String InsuredName = "";
                    
                    
                    if ("FMT_UNB_0100".equals(shortName)) {
                        ClientRepre = Xml.getNodeText(dom, "/WSLetterUnit/emailReceiverList/emailReceiver/receiverName");
                        UnderwriterMail = Xml.getNodeText(dom, "/WSLetterUnit/emailReceiverList/emailReceiver[2]/receiverEmail");
                        UnderwriterDiv = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeDept");
                        UnderwriterName = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeName");
						Tel = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeTel");
						Ext = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeExt");
                    }
                    else if ("FMT_UNB_0110".equals(shortName)) {
                        Contact = Xml.getNodeText(dom, "/WSLetterUnit/extension/extendVOList/extendVO/riContact");
                        UnderwriterMail = Xml.getNodeText(dom, "/WSLetterUnit/emailReceiverList/emailReceiver[1]/receiverEmail");
                        UWDivision = Xml.getNodeText(dom, "/WSLetterUnit/extension/extendVOList/extendVO/UWDivision");
                        Underwriter = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeName");
                        Tel = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeTel");
						Ext = Xml.getNodeText(dom, "/WSLetterUnit/extension/employeeExt");
                    }
                    //else if ("FMT_CLM_0010".equals(shortName)) {                        
                        InsuredName = Xml.getNodeText(dom, "/WSLetterUnit/extension/insuredId");
                    //}

                    if (null != omicardInfo) {
                        customerPersonID = Xml.getNodeText(omicardInfo, "customerPersonID");   
                        groupCode = Xml.getNodeText(omicardInfo, "groupCode");
                        insuranceID = Xml.getNodeText(omicardInfo, "policyCode");
                        insuranceReportID = Xml.getNodeText(omicardInfo, "insuranceReportID");
                        loginID = Xml.getNodeText(omicardInfo, "certificateCode");
                        organizationCode = Xml.getNodeText(omicardInfo, "organizationCode");
                        salesName = Xml.getNodeText(omicardInfo, "salesName"); 
                        unitCode = Xml.getNodeText(omicardInfo, "unitCode");  
                        insuranceReportDate = Xml.getNodeText(omicardInfo, "insuranceReportDate");  
                    }


                	if (!shortName.equals(prev_shortName)) {
                		
                        deptId = get_dept(shortName);
                        subject = shortName;
                        body = shortName;

                        wmFrontList.clear();
                        wmBackList.clear();
                        if (!Utils.isEmpty(wmFront)) wmFrontList.add(wmPath+wmFront);
                        if (!Utils.isEmpty(wmBack)) wmBackList.add(wmPath+wmBack);

                		//if (!tplId.equals(prev_tplId)) {
                			//prev_tplId = tplId;
                			com.tgl.esp.ws.server.service.RawTemplateContentRequest _getRawTemplateByID_arg0 = new com.tgl.esp.ws.server.service.RawTemplateContentRequest();
                            _getRawTemplateByID_arg0.setTemplateId(tplId);
                            if (!testMode) {
                                com.tgl.esp.ws.server.service.TemplateContentResponse _getRawTemplateByID__return = port.getRawTemplateByID(_getRawTemplateByID_arg0);
                                //System.out.println("getRawTemplateByID.result=" + _getRawTemplateByID__return);
                                TemplateContent tc = _getRawTemplateByID__return.getTemplateContent();
                                if (tc != null) {
                                    if (!Utils.isEmpty(tc.getSubject()))
                                        subject = tc.getSubject();
                                    if (!Utils.isEmpty(tc.getBody()))
                                        body = tc.getBody();
                                }
                            }
                		//}

                		if (bm != null) {
                            if (mails > 0) bm.commit(); else bm.delete();
                            bm = null;
                            System.out.format("mails=%d%n", mails);
                            mails = 0;
                            if (conn != null) conn.commit();    
                            if (connESP != null) connESP.commit();    
                        }

                        if (shortName.equals(subject) || shortName.equals(body)) {
                            System.out.format("no email template for %s%n", shortName);
                            _lastErr = String.format("no email template for %s%n", shortName);
                        	noTpl = true;
                        }
                        else if (Utils.isEmpty(loginName)) {
                            System.out.format("no loginName for %s%n", shortName);
                            _lastErr = String.format("no loginName for %s%n", shortName);
                            noTpl = true;
                        }
                        else if (Utils.isEmpty(fromDisplay)) {
                            System.out.format("no fromDisplay for %s%n", shortName);
                            _lastErr = String.format("no fromDisplay for %s%n", shortName);
                            noTpl = true;
                        }
                        else {
                        prev_shortName = shortName;

                		bm = new BatchMail("SIP-"+shortName);

                        subject = subject.replace("%ReportName%", reportName);

                        bm.setSubject(subject);
                        bm.setBody(body);  

                        bm.setLoginName(loginName);
                        bm.setFromDisplay(fromDisplay);
                        
                        
                        LOGGER.debug("Default Approve Name : " + Utils.shortNameToApproveName(shortName, dom));
                        bm.setApproveName(Utils.shortNameToApproveName(shortName, dom));
                        
                        if ("FMT_CLM_0010".equalsIgnoreCase(shortName) || "FMT_CLM_0020".equalsIgnoreCase(shortName)) {
        					String expression = "WSLetterUnit/extension/safeLevel";
        					String safeLevel = Xml.getNodeText(dom, expression);
        					LOGGER.debug("Safe Level = " + safeLevel);
        					if ("1".equals(safeLevel)) {
        						LOGGER.debug("match \"Need Approve Process\" condition --> safeLevel = " + safeLevel + " = warning case! " + listId + "\n");
        						 bm.setApproveName(Utils.shortNameToApproveName(shortName, dom));
        						 LOGGER.debug("Approve flow = " + Utils.shortNameToApproveName(shortName, dom));
        					}
        					else if ("2".equals(safeLevel))
        					{
        						LOGGER.debug("SafeLevel = " + safeLevel + ", No need go through approve process :" + listId + "\n");
        						bm.setApproveName(null);
        						LOGGER.debug("Approve flow = null");
        					}
        				}
                       

                        bm.getListFile().add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", "EmailAddress", "Attachment", "PDFUserPassword", "CustomerPersonID", "GroupCode", "PolicyCode", "InsuranceReportID", "CertificateCode", "OrganizationCode", "SalesName", "UnitCode", "InsuranceReportDate", "CustomerName", "ReportName", "ToDisplay", "DepartmentName", "ExternRefNum",
                                "ClientRepre", "UnderwriterMail", "UnderwriterDiv", "UnderwriterName",
                                "Contact", "UWDivision", "Underwriter", "Tel", "Ext", "InsuredName"));
                    	}
                	}

                	String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo);

                  if (noTpl) {
                  		
                  }	else
                  if ((new File(srcPdf)).exists()) {
                  	boolean attOk = false;

                	//String attName = String.format("%s-%s-%s-%08x-%06x.pdf", policyCode, hostCode, spoolName, jobNo, subjobNo);
                    String attName = String.format("%s-%s-%s-%s-%08x-%06x.pdf", reportName, policyCode, hostCode, spoolName, jobNo, subjobNo);

                    if (!wmFrontList.isEmpty() || !wmBackList.isEmpty()) {
                        if (attOk = watermarkPDF.start(srcPdf, bm.getAttachPath() + "\\"+ attName, wmFrontList, wmBackList)) {
                            //srcPdf = null;
                        }
                        srcPdf = null;
                    }
                    
                    if (srcPdf != null) attOk=bm.setAttach(srcPdf, attName);

                    if (attOk)
                     for (int i=0; i<nl.getLength(); ++i) {
                        Node emailReceiver = nl.item(i);
                        String emailAddr = Xml.getNodeText(emailReceiver, "receiverEmail");
                        String attPwd = Xml.getNodeText(emailReceiver, "attachmentPassword");
                        String customerName = Xml.getNodeText(emailReceiver, "receiverName");
                        //System.out.format("emailAddr=%s%n", emailAddr);
                        if (!Utils.isEmpty(emailAddr) && !Utils.isEmpty(customerName)) {
                            String list = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", emailAddr, attName, attPwd, customerPersonID, groupCode, insuranceID, insuranceReportID, loginID, organizationCode, salesName, unitCode, insuranceReportDate, customerName, reportName, toDisplay, departmentName, listId,
                                        ClientRepre, UnderwriterMail, UnderwriterDiv, UnderwriterName,
                                        Contact, UWDivision, Underwriter, Tel, Ext, InsuredName);
                            bm.getListFile().add(list);
                            System.out.println(list);
                            ++mails;

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

                            emailed = "Y";     

                            ++done;        
                        }
                        else {
                        	System.out.format("no receiverEmail /  receiverName%n");
                    		_lastErr = String.format("no receiverEmail /  receiverName%n");
                        }
                    }

                  } else {
                    System.out.format("Not found %s%n", srcPdf);
                    _lastErr = String.format("Not found %s%n", srcPdf);
                  }
                }//if (nl != null && nl.getLength() > 0) {

                updateEmailed(_stmtUpdateEMAILED, hostCode, spoolName, jobNo, subjobNo, emailed);  

                if ("N".equals(emailed) && !Utils.isEmpty(_lastErr))  
                	updateErrMsg(_stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, _lastErr);

			}//while (result.next()) {

            if (bm != null) {
                            if (mails > 0) bm.commit(); else bm.delete();
                            bm = null;
                            System.out.format("mails=%d%n", mails);
                            mails = 0;
                            if (conn != null) conn.commit();    
                            if (connESP != null) connESP.commit();    
                        }

    	}
		catch (Exception e) {
			handleException(e);
		}
		finally {
			if (mails > 0) {
				conn.rollback();
				connESP.rollback();
			}
			if (bm!=null) bm.delete();
			if (result != null) { result.close(); result = null; }
			if (_stmtUpdateEMAILED != null) { _stmtUpdateEMAILED.close(); _stmtUpdateEMAILED = null; }
			if (_stmtUpdateERRMSG != null) { _stmtUpdateERRMSG.close(); _stmtUpdateERRMSG = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
			if (connESP != null) { connESP.close(); connESP = null; }
		}

        System.out.format("jobs=%d, done=%d%n", jobs, done);

    }

    void handleException (Exception e) {
		if (e instanceof SQLException) {
			System.err.println("SQLState=" + ((SQLException)e).getSQLState());
			System.err.println("ErrorCode=" + ((SQLException)e).getErrorCode());
		}
		e.printStackTrace();
	}

	void updateEmailed (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, String emailed) throws Exception {
            try {
                stmt.setString(1, emailed);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    void updateErrMsg (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, String errMsg) throws Exception {
            try {
                stmt.setString(1, errMsg.length()>2000? errMsg.substring(0,2000):errMsg);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    String get_dept (String shortName) {
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
