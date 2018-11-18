//send pdf to CF for online letter

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

public class ArchFile {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 2) {

            String xmlFile = args[0];
            String pdfFile = args[1];
            String relPdfFolder = args.length>2? args[2]:"";

            String user = "SIP";
            String overWriteOption = "ALL";
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
                //String fn = getFileName(pdfFile);
                //content.setContentName(fn);
                //content.setContentType(getExtName(fn));

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); 
                Document dom = db.parse(new File(xmlFile));

                NodeList nl = dom.getElementsByTagName("index").item(0).getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if ("reportId".equalsIgnoreCase(n.getNodeName())) {
                            content.setReportId(n.getTextContent());
                        }
                        else if ("dataDate".equalsIgnoreCase(n.getNodeName())) {
                            String dStr = n.getTextContent();
                            java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                            java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                            c.setTime(ca.getTime());
                            javax.xml.datatype.XMLGregorianCalendar dataDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                            content.setDataDate(dataDate);
                        }
                        else {
                            boolean isList = false;
                                    String key = n.getNodeName();
                                    if (key.endsWith("List")) {
                                        String key2 = key.substring(0, key.length()-4);
                                        NodeList nl2 = ((Element)n).getElementsByTagName(key2);
                                        if (nl2!= null && nl2.getLength() > 0) {
                                            isList = true;
                                            for (int k = 0; k < nl2.getLength(); k++) {
                                                Node n2 = nl2.item(k);
                                                addIdx(content, key2, n2.getTextContent() );
                                            }
                                        }
                                    } 
                                    if (!isList) {
                                        String val = n.getTextContent();
                                        addIdx(content, key, val);
                                    }
                        }
                    }
                }

                /*nl = dom.getElementsByTagName("isOverriding");
                if (nl != null && nl.getLength() > 0 && "Y".equalsIgnoreCase(nl.item(0).getTextContent()))
                	overWriteOption = "ALL";*/

                String documentNo = "";
                nl = dom.getElementsByTagName("documentNo");  
                if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent();
                else {
                                nl = dom.getElementsByTagName("policyCode");  
                                if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent();
                            } 
                String fileName = String.format("%s-%s-%06x.pdf", documentNo, relPdfFolder.replaceAll("\\\\", "-"), 1);
                        content.setContentName(fileName);
                        content.setContentType("pdf");        

                com.transglobe.cf.webservice.ArchiveResponse res = port.archiveFile(user, overWriteOption, repeatArchive, content, "Y", "SIPFMT");

                boolean hasErr = true;

                if (res != null) {
                    content = res.getContent();
                    if (content != null) {
                        System.out.println("guid="+content.getGuid());   
                        hasErr = false;
                    }
                } 
                if (hasErr) {
                    rc += 30;
                }

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
        }
        else {
            System.out.println("ArchFile <xml> <pdf>");
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

    static javax.xml.datatype.XMLGregorianCalendar toDate (String dStr) throws Exception {
        java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                            java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                            c.setTime(ca.getTime());
                            javax.xml.datatype.XMLGregorianCalendar date = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return date;                 
    }

    static void addIdx (com.transglobe.cf.webservice.Content content, String key, String value) throws Exception {
         Index idx = new Index();
                    idx.setKey(key);
                    boolean isDate = value.length()>=10 && value.charAt(4)=='-' && value.charAt(7)=='-';
                    if (isDate) idx.setValue(toDate(value));
                    else idx.setValue(value);
                    content.getIndexList().add(idx);
    }

}
