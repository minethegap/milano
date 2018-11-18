

import java.io.File;
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

import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;

import common.*;

import com.itextpdf.text.pdf.PdfReader;
import java.util.List;
import java.util.ArrayList;


public class CFMail {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 3) {

            String xmlFile = args[0];
            String jobPath = args[1];
            String jobName0 = args[2];
            String pdfFile = findFirstFile(jobPath);
            System.out.println("attach="+pdfFile);

            String user = "SIP";
            String overWriteOption = "NON";
            String repeatArchive = "Y";

            try {

                URL wsdlURL = CFServiceService.WSDL_LOCATION;
                File wsdlFile = new File("c:\\sip\\cfg\\CFService.xml");
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

                com.transglobe.cf.webservice.Content content = new com.transglobe.cf.webservice.Content();
                content.setContentData(new DataHandler(new FileDataSource(pdfFile)));
                String fn = getFileName(pdfFile);
                content.setContentName(fn);
                content.setContentType(getExtName(fn));

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); 
                Document dom = db.parse(new File(xmlFile));

                NodeList nl = dom.getDocumentElement().getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if ("reportId".equalsIgnoreCase(n.getNodeName())) {
                            String reportId = n.getTextContent();
                            content.setReportId(reportId);
                            System.out.println("reportId="+reportId);
                        }
                        else if ("dataDate".equalsIgnoreCase(n.getNodeName())) {
                            String dStr = n.getTextContent();
                            System.out.println("dataDate="+dStr);
                            java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                            java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                            c.setTime(ca.getTime());
                            javax.xml.datatype.XMLGregorianCalendar dataDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                            content.setDataDate(dataDate);
                        }
                    }
                }

                nl = dom.getElementsByTagName("entry");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    String key = Xml.getNodeText(n, "key");
                    String value = Xml.getNodeText(n, "value");
                    System.out.println(key+"="+value);
                    Index idx = new Index();
                    idx.setKey(key);
                    boolean isDate = value.length()>=10 && value.charAt(4)=='-' && value.charAt(7)=='-';
                    if (isDate) idx.setValue(toDate(value));
                    else idx.setValue(value);
                    content.getIndexList().add(idx);
                }

                com.transglobe.cf.webservice.Response res = port.archiveFile(user, overWriteOption, repeatArchive, content, "Y", "SIPFMT");

                boolean hasErr = true;

                if (res != null) {
                    content = ((com.transglobe.cf.webservice.ArchiveResponse)res).getContent();
                    if (content != null) {
                        System.out.println("guid="+content.getGuid());   
                        hasErr = false;
                    }
                } 
                if (hasErr) {
                	if (res != null) {
                		String errMsg = res.getReturnMsg();
                		if (!Utils.isEmpty(errMsg)) {
                			try {
                				errMsg += "\n";
    							java.nio.file.Files.write(java.nio.file.Paths.get(jobPath+".err"), errMsg.getBytes("Big5"), java.nio.file.StandardOpenOption.APPEND,java.nio.file.StandardOpenOption.CREATE);
							} catch (Exception e) {
								e.printStackTrace();
							}
                		}
                	}
                    rc += 30;
                }

                PdfReader pdfReader = new PdfReader(pdfFile);
				BookmarkPageRange bpr = new BookmarkPageRange(pdfReader);
				ExtractPDF ep = new ExtractPDF(pdfReader);
				List<PageRange> prs = new ArrayList<PageRange>();

                String attName = getFileName(pdfFile);
                String mainName = getMainName(attName);
                
                
                nl = dom.getElementsByTagName("Email");

              if (nl != null && nl.getLength() > 0)
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);

                    int sent = 0;
                    String jobName = String.format("%s-%d", jobName0, 1+i);
                    String subject = Xml.getNodeText(n, "subject");
                    String body =  Xml.getNodeText(n, "content");
                    if (Utils.isEmpty(subject)) subject = jobName;
                    if (Utils.isEmpty(body)) body = jobName;

                    BatchMail bm = new BatchMail(jobName);
                
                    bm.setSubject(subject);
                    bm.setBody(body);
                
                    bm.getListFile().add(String.format("%s,%s,%s", "EmailAddress", "Attachment", "PDFUserPassword"));

                    String list = null;
                    String an = null;
                    NodeList splits = ((Element) n).getElementsByTagName("split");
                    if (splits != null && splits.getLength() > 0) {
                    	prs.clear();
                    	for (int j=0; j<splits.getLength(); ++j) {
                    		PageRange pr = bpr.find(splits.item(j).getTextContent());	
                    		if (!PageRange.isEmpty(pr))
								prs.add(pr);
                    	}
                    	if (prs.size() > 0) {
                            an = String.format("%s_%d.pdf", mainName, 1+i);
							ep.extract(prs, bm.getAttachPath()+"\\"+an);
						}
                    }
                    else {
                        bm.setAttach(pdfFile);
                        an = attName;
                    }

                    if (an != null) {
                        String emailAddress;
                        String pwd;
                        int l;
                        Node n2;
                        NodeList nl2 = ((Element) n).getElementsByTagName("MailTo");
                        if (nl2 != null && nl2.getLength() > 0) {
                            for (l = 0; l < nl2.getLength(); l++) {
                                n2 = nl2.item(l);
                                emailAddress = Xml.getNodeText(n2, "receiverEmail");
                                if (!Utils.isEmpty(emailAddress)) {
                                    pwd = Xml.getNodeText(n2, "attachmentPassword");
                                    list = String.format("%s,%s,%s", emailAddress, an, pwd);
                                    ++sent;
                                    bm.getListFile().add(list);
                                    System.out.println(list);
                                }
                            }
                        }
                    
                        nl2 = ((Element) n).getElementsByTagName("MailCC");
                        if (nl2 != null && nl2.getLength() > 0) {
                            for (l = 0; l < nl2.getLength(); l++) {
                                n2 = nl2.item(l);
                                //emailAddress = Xml.getNodeText(n2, "ccReceiverEmail");
                                emailAddress = Xml.getNodeText(n2, "ReceiverEmail");
                                if (!Utils.isEmpty(emailAddress)) {
                                    pwd = Xml.getNodeText(n2, "attachmentPassword");
                                    list = String.format("%s,%s,%s", emailAddress, an, pwd);
                                    ++sent;
                                    bm.getListFile().add(list);
                                    System.out.println(list);
                                }
                            }
                        }
                    }

                    if (sent > 0)
                        bm.commit();
                    else
                        bm.delete();
                }
              else {
                rc += 31;  
                System.out.println("No Email tag");
              }  

            } catch (Exception e) {
                e.printStackTrace();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                e.printStackTrace(new java.io.PrintStream(baos));
                try {
    				java.nio.file.Files.write(java.nio.file.Paths.get(jobPath+".err"), baos.toString().getBytes("Big5"), java.nio.file.StandardOpenOption.APPEND,java.nio.file.StandardOpenOption.CREATE);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
                rc += 98;
            }
        }
        else {
            System.out.println("CFMail <req_xml> <job_path> <job_name>");
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

    static String getMainName (String s) {
    	int idx = s.lastIndexOf('.');
    	if (idx >= 0)
    		return s.substring(0,idx);
    	return s;	
    }

    static String findFirstFile (String path) {
        String filename = "";
        File dir = new File(path);
        String[] children = dir.list();
        if (children != null && children.length > 0) {
            filename = children[0];
        }
        if (!"".equals(filename))
            filename = path + "\\" + filename;
        return filename;
    }

    static javax.xml.datatype.XMLGregorianCalendar toDate (String dStr) throws Exception {
        java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                            java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                            c.setTime(ca.getTime());
                            javax.xml.datatype.XMLGregorianCalendar date = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return date;                 
    }
}
