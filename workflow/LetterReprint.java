
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import org.xml.sax.InputSource;
import common.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;
import com.tgl.esp.ws.server.service.*;


class LetterReprint {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");
	private static final String wmPath = "D:\\Case\\TGL\\WM\\";

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 2) {
			try {
				String inXml = args[0];
				String outPdf = args[1];

				URL wsdlURL = CFServiceService.WSDL_LOCATION;
                File wsdlFile = new File("c:\\SIP\\cfg\\CFService.xml");
                try {
                    if (wsdlFile.exists()) {
                        wsdlURL = wsdlFile.toURI().toURL();
                    } else {
                        wsdlURL = new URL(args[0]);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
          
                CFServiceService ss = new CFServiceService(wsdlURL, SERVICE_NAME);
                CFService port = ss.getCFServicePort();  

                //enable MTOM
    			BindingProvider bp = (BindingProvider) port;
    			SOAPBinding binding = (SOAPBinding) bp.getBinding();
    			binding.setMTOMEnabled(true);

    			rc += (new LetterReprint()).reprint(port, inXml, outPdf);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("LetterReprint <in_xml> <out_pdf>");
			rc += 15;
		}
		System.exit(rc);
	}

	final String EMPTY_STR = "\"\"";

	int reprint (CFService port, String inXml, String outPdf) throws Exception {
		int rc = 30;

		//load the input xml
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document dom = db.parse(new File(inXml));

		String listID = null;

		try {
			listID = dom.getElementsByTagName("listID").item(0).getTextContent();
		} catch (Exception e) {
			System.out.println("no listID");
		}

		String shortName = dom.getElementsByTagName("reportID").item(0).getTextContent();	
		String cfGid = dom.getElementsByTagName("cfGid").item(0).getTextContent(); 
		String claimMode = dom.getElementsByTagName("claimMode").item(0).getTextContent();  
		String deliverMethodType = dom.getElementsByTagName("deliverMethodType").item(0).getTextContent();   
        
        DownloadFile df = new DownloadFile();
        if (df.download(port, cfGid, outPdf)) {
        	rc = 0; //get from CF ok
        	String tplId = null;
            String reportName = null;
            String fromDisplay = null;
            String loginName = null;
            String departmentName = Utils.shortNameToDeptName(shortName);

        	boolean bCLAIM = "CLAIM".equalsIgnoreCase(claimMode);
        	//boolean bCLAIM = true;
        	boolean bEMAIL = "EMAIL".equalsIgnoreCase(deliverMethodType);
        	/*if (bCLAIM || bEMAIL)*/ {
        		rc = 34; //assume sql error
        		Connection conn = (new Sql("@jdbc_ODS")).getConn();	
        		//PreparedStatement stmtSelect = conn.prepareStatement("select * from T_FMT_TPL_CFG where SHORT_NAME=?");
                PreparedStatement stmtSelect = conn.prepareStatement("SELECT tc.SHORT_NAME, tc.WM_REPRINT,tc.EMAIL_TPLID,tc.WM_EMAIL_FRONT,tc.WM_EMAIL_BACK,t.TEMPLATE_NAME" 
 + ", tc.EMAIL_FROMDISPLAY, tc.EMAIL_USERID LoginName" 
 + " FROM T_TEMPLATE t" 
 + " JOIN T_FMT_TPL_CFG tc ON tc.SHORT_NAME=t.SHORT_NAME"
 + " where tc.SHORT_NAME=?" );
        		stmtSelect.setString(1,shortName);
        		ResultSet result = stmtSelect.executeQuery();
				if (result.next()) {
					String wmFront = result.getString("WM_EMAIL_FRONT");
					String wmBack = result.getString("WM_EMAIL_BACK");
					String wmReprint = result.getString("WM_REPRINT");
					tplId = result.getString("EMAIL_TPLID");
                    reportName = result.getString("TEMPLATE_NAME");
                    fromDisplay =  result.getString("EMAIL_FROMDISPLAY");  
                    loginName =  result.getString("LoginName");  //EMAIL_USERID

					java.util.List<String> wmFrontList = new java.util.ArrayList<String>();
					java.util.List<String> wmBackList = new java.util.ArrayList<String>();
					if (bEMAIL && !Utils.isEmpty(wmFront)) wmFrontList.add(wmPath+wmFront);
					if (bEMAIL && !Utils.isEmpty(wmBack)) wmBackList.add(wmPath+wmBack);
					if (/*bCLAIM && */!Utils.isEmpty(wmReprint)) {
						wmFrontList.add(wmPath+wmReprint);
						wmBackList.add(wmPath+wmReprint);
					}

					if (!wmFrontList.isEmpty() || !wmBackList.isEmpty()) {
						rc = 21; //assume add WM error
						String outPdf_ = outPdf+"_";
						if ((new WatermarkPDF()).start(outPdf, outPdf_, wmFrontList, wmBackList)) {
							try {
								Files.move(Paths.get(outPdf_), Paths.get(outPdf), StandardCopyOption.REPLACE_EXISTING);
								rc = 0; //add WM ok
							} catch (Exception e3) {
								e3.printStackTrace();
							}
						}
					}
					else rc = 0; //no need to add WM					
				}
				else rc = 0; //no need to add WM
        	}

        	if (rc==0 && bCLAIM) {
        		Node receiverInfo = dom.getElementsByTagName("receiverInfo").item(0);
        		if (bEMAIL) {
        			String emailAddress = Xml.getNodeText(receiverInfo, "emailAddress");
        			String emailPassword = Xml.getNodeText(receiverInfo, "emailPassword");
                    String receiverName = Xml.getNodeText(receiverInfo, "receiverName");
        			if (!Utils.isEmpty(emailAddress) /* && !Utils.isEmpty(receiverName) */) {
        				if (Utils.isEmpty(tplId)) tplId = shortName;

        				String subject = shortName;
        				String body = shortName;

        				MailManagementWSService ss = new MailManagementWSService((new File("c:\\sip\\cfg\\ESP.xml")).toURI().toURL(), new QName("http://service.server.ws.esp.tgl.com/", "MailManagementWSService"));
			        	MailManagementWS port_esp = ss.getMailManagementWSPort(); 
			        	//enable MTOM
			    		BindingProvider bp = (BindingProvider) port_esp;
			    		SOAPBinding binding = (SOAPBinding) bp.getBinding();
			    		binding.setMTOMEnabled(true); 
        				com.tgl.esp.ws.server.service.RawTemplateContentRequest _getRawTemplateByID_arg0 = new com.tgl.esp.ws.server.service.RawTemplateContentRequest();
                    	_getRawTemplateByID_arg0.setTemplateId(tplId);
                    	if (true) {
                                com.tgl.esp.ws.server.service.TemplateContentResponse _getRawTemplateByID__return = port_esp.getRawTemplateByID(_getRawTemplateByID_arg0);
                                //System.out.println("getRawTemplateByID.result=" + _getRawTemplateByID__return);
                                TemplateContent tc = _getRawTemplateByID__return.getTemplateContent();
                                if (tc != null) {
                                    if (!Utils.isEmpty(tc.getSubject()))
                                        subject = tc.getSubject();
                                    if (!Utils.isEmpty(tc.getBody()))
                                        body = tc.getBody();
                                }
                            }

                        if (shortName.equals(subject) || shortName.equals(body)) 
                            throw new Exception(String.format("No email template for %s", shortName));   

                        if (Utils.isEmpty(loginName)) 
                            throw new Exception(String.format("No loginName for %s", shortName));
                         
                        if (Utils.isEmpty(fromDisplay))  
                            throw new Exception(String.format("No fromDisplay for %s", shortName));    

                    	BatchMail bm = new BatchMail("SIP-Remail"+shortName);

                        subject = subject.replace("%ReportName%", reportName);
                        
                        bm.setSubject(subject);
                        bm.setBody(body);  

                        bm.setLoginName(loginName);
                        bm.setFromDisplay(fromDisplay);
						bm.setApproveName(Utils.shortNameToApproveName(shortName, dom));

                        bm.getListFile().add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", "EmailAddress", "Attachment", "PDFUserPassword", "CustomerPersonID", "GroupCode", "PolicyCode", "InsuranceReportID", "CertificateCode", "OrganizationCode", "SalesName", "UnitCode", "InsuranceReportDate", "CustomerName", "ReportName", "ToDisplay", "DepartmentName", "ExternRefNum",
                                "ClientRepre", "UnderwriterMail", "UnderwriterDiv", "UnderwriterName",
                                "Contact", "UWDivision", "Underwriter", "Tel", "Ext", "InsuredName"));
                    	
        				String attName = String.format("%s.pdf", reportName);
        				bm.setAttach(outPdf, attName);

        				String customerPersonID = "";
	                    String groupCode = "";
	                    String insuranceID = "";
	                    String insuranceReportID = "";
	                    String loginID = "";
	                    String organizationCode = "";
	                    String salesName = "";
	                    String unitCode = "";
	                    String insuranceReportDate = "";
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

                    Document dom_doc = null;

                    if ((Utils.isEmpty(receiverName) || "FMT_UNB_0100".equals(shortName) || "FMT_UNB_0110".equals(shortName)
                        || "FMT_CLM_0010".equals(shortName) || "FMT_CLM_0020".equals(shortName)) && !Utils.isEmpty(listID)) {
                    	try {
                    		Connection conn = (new Sql("@jdbc_TGL")).getConn();	
                    		PreparedStatement stmtSelect = conn.prepareStatement("SELECT c.CONTENT FROM T_DOCUMENT d" +
	                    		" JOIN T_DOCUMENT_DATA dd ON d.LIST_ID=dd.LIST_ID" +
	 							" JOIN T_CLOB c ON dd.CLOB_ID=c.CLOB_ID" +
	 							"where d.LIST_ID=?");
        					stmtSelect.setString(1,listID);
				        	ResultSet result = stmtSelect.executeQuery();
							if (result.next()) {
				                Clob rptData = result.getClob("CONTENT");
	 							byte[] baData = rptData.getSubString(1, (int)rptData.length()).getBytes("UTF-8");

	 							dom_doc = db.parse(new ByteArrayInputStream(baData));
							}
                    	}
                    	catch (Exception es) {
							es.printStackTrace();
						}
				    }


                    if (dom_doc != null) {
                    	if (Utils.isEmpty(receiverName)) {
                    		NodeList nl = dom_doc.getElementsByTagName("emailReceiver");    
                			if (nl != null && nl.getLength() > 0) {
                				Node emailReceiver = nl.item(0);
                        		receiverName = Xml.getNodeText(emailReceiver, "receiverName");
                			}
                		}
	                    if ("FMT_UNB_0100".equals(shortName)) {
	                        ClientRepre = Xml.getNodeText(dom_doc, "/WSLetterUnit/emailReceiverList/emailReceiver/receiverName");
	                        UnderwriterMail = Xml.getNodeText(dom_doc, "/WSLetterUnit/emailReceiverList/emailReceiver[2]/receiverEmail");
	                        UnderwriterDiv = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeDept");
	                        UnderwriterName = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeName");
							Tel = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeTel");
							Ext = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeExt");
	                    }
	                    else if ("FMT_UNB_0110".equals(shortName)) {
	                        Contact = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/extendVOList/extendVO/riContact");
	                        UnderwriterMail = Xml.getNodeText(dom_doc, "/WSLetterUnit/emailReceiverList/emailReceiver[1]/receiverEmail");
	                        UWDivision = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/extendVOList/extendVO/UWDivision");
	                        Underwriter = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeName");
	                        Tel = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeTel");
							Ext = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/employeeExt");
	                    }

                        InsuredName = Xml.getNodeText(dom_doc, "/WSLetterUnit/extension/insuredId");

                	}

                    
        				String list = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", emailAddress, attName, emailPassword, customerPersonID, groupCode, insuranceID, insuranceReportID, loginID, organizationCode, salesName, unitCode, insuranceReportDate, receiverName, reportName, toDisplay, departmentName, cfGid,
                                        ClientRepre, UnderwriterMail, UnderwriterDiv, UnderwriterName,
                                        Contact, UWDivision, Underwriter, Tel, Ext, InsuredName);
                        bm.getListFile().add(list);
                        System.out.println(list);
                        bm.commit();
        			}
                    else throw new Exception("No emailAddress / receiverName");
        		}
        		else if ("POST_MAIL".equalsIgnoreCase(deliverMethodType)) {
        			String receiverID = Xml.getNodeText(receiverInfo, "receiverID");
        			String receiverName = Xml.getNodeText(receiverInfo, "receiverName");
        			///if (Utils.isEmpty(receiverID)) receiverID = receiverName;
        			String postmailAddressZipCode = Xml.getNodeText(receiverInfo, "postmailAddressZipCode");
        			String postmailAddress = Xml.getNodeText(receiverInfo, "postmailAddress");
        			if (!Utils.isEmpty(postmailAddress)) {
        				String duplex = "1";
                		String deliverMethod = "3";
        				BatchPrint bp = new BatchPrint(true, shortName, duplex, deliverMethod, false, "FMT_POS_0190".equals(shortName)?"VP_TO_YET":"VP_TO_BET");
        				String infoStr = "";
        				String pdfName = String.format("%s.pdf", shortName);
        				String sendMode = "03";
        				infoStr = String.format(",%s,%s,%s",
                      		EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    	    EMPTY_STR,
                            EMPTY_STR,
                    		EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                    		);
                    	infoStr += String.format(",%s,%s,%s", 
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                            );
                    	infoStr += String.format(",%s,%s,%s,%s",
                    		forCSV(receiverID),
                    		forCSV(receiverName),
                    		forCSV(postmailAddressZipCode),
                    		forCSV(postmailAddress)
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR, //postWay
                            EMPTY_STR  //ackDueDate
                            );
        				String printId = bp.add(infoStr, outPdf, pdfName, shortName, sendMode, Utils.isEmpty(receiverID)?receiverName:receiverID, receiverName, postmailAddressZipCode, postmailAddress, cfGid, null, null);
        				bp.commit();
        			}
                    else throw new Exception("No postmailAddress");
        		}
        	}
        }
		return rc;
	}

	String forCSV (String s) {
        if (!Utils.isEmpty(s)) {
            return String.format("\"%s\"", s.replace("\"", "\"\"")); 
        }
        return "\"\"";
    }
}
