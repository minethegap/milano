

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

import java.sql.*;
import common.*;


public class ArchFileM {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 3) {

            String xmlFile = args[0];
            String pdfFolder = args[1];
            String relPdfFolder = args[2];
            String tblName = args.length > 3? args[3]:null;
            int subjobNo = 0;
            int done = 0;
            int errCnt = 0;

            String user = "SIP";
            String repeatArchive = "Y";

            String hostCode = "";
            String spoolName = "";
            Long jobNo = 0L;

            int pd1 = relPdfFolder.indexOf('\\');
            int pd2 = relPdfFolder.lastIndexOf('\\');
            hostCode = relPdfFolder.substring(0,pd1);
            spoolName = relPdfFolder.substring(pd1+1,pd2);
            jobNo = Long.parseLong(relPdfFolder.substring(pd2+1), 16);

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

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); 
                Document dom = db.parse(new File(xmlFile));

                Node root = dom.getDocumentElement();
                Node subjob = root.getFirstChild();

                while (subjob != null) {
                    if (subjob.getNodeType()==Node.ELEMENT_NODE) {
                        String reportId = "";
                        String printUnit = "";
                        String policyCode = "";
                        String overWriteOption = "ALL";
                        javax.xml.datatype.XMLGregorianCalendar dataDate = null;

                        com.transglobe.cf.webservice.Content content = new com.transglobe.cf.webservice.Content();

                        NodeList nl = ((Element)subjob).getElementsByTagName("index").item(0).getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++) {
                            Node n = nl.item(i);
                            if (n.getNodeType() == Node.ELEMENT_NODE) {
                                if ("reportId".equalsIgnoreCase(n.getNodeName())) {
                                    content.setReportId(reportId = n.getTextContent());
                                }
                                else if ("dataDate".equalsIgnoreCase(n.getNodeName())) {
                                    String dStr = n.getTextContent();
                                    java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                                    java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                                    c.setTime(ca.getTime());
                                    dataDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
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
                                        if ("printUnit".equalsIgnoreCase(key)) printUnit = val;
                                        else if ("policyCode".equalsIgnoreCase(key)) policyCode = val;
                                    }
                                }
                            }
                        }
                        nl = ((Element)subjob).getElementsByTagName("isOverriding");
                        if (nl != null && nl.getLength() > 0 && "Y".equalsIgnoreCase(nl.item(0).getTextContent())) {
                            overWriteOption = "ALL";
                            if ("#POLICY#".equalsIgnoreCase(reportId)) {
                                //nl = ((Element)subjob).getElementsByTagName("printMode");
                                //if (nl != null && nl.getLength() > 0 && "2".equalsIgnoreCase(nl.item(0).getTextContent())) {  //2.新契約重製
                                    cf_delete_pol(port, reportId, dataDate, printUnit, policyCode);
                                //}  
                            }
                        }

                        ++subjobNo;
                        String pdfFile = String.format("%s\\%s\\%06x.pdf", pdfFolder, relPdfFolder, subjobNo);
                        System.out.println(pdfFile);
                        content.setContentData(new DataHandler(new FileDataSource(pdfFile)));  
                        String documentNo = "";
                        nl = ((Element)subjob).getElementsByTagName("documentNo");  
                        if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent(); 
                        else {
                            nl = ((Element)subjob).getElementsByTagName("policyCode");  
                            if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent();
                        }  
                        String fileName = String.format("%s-%s-%06x.pdf", documentNo, relPdfFolder.replaceAll("\\\\", "-"), subjobNo);
                        content.setContentName(fileName);
                        content.setContentType("pdf");

                        com.transglobe.cf.webservice.ArchiveResponse res = port.archiveFile(user, overWriteOption, repeatArchive, content, "Y", "SIPFMT");

                        boolean hasErr = true;

                        if (res != null) {
                            content = res.getContent();
                            if (content != null) {
                                ++done;
                                if (!common.Utils.isEmpty(tblName)) {
                                    updateGUID(tblName, hostCode, spoolName, jobNo, subjobNo, content.getGuid());
                                }
                                System.out.println("guid="+content.getGuid());   
                                hasErr = false;
                            }
                        }
                        if (hasErr && res!=null) {
                            com.transglobe.cf.webservice.Response res1 = res;
                            updateErrMsg(tblName, hostCode, spoolName, jobNo, subjobNo, res1.getReturnCode()+" "+res1.getReturnMsg());
                        }

                        if (hasErr) ++errCnt; 
                        if (hasErr && rc == 0x00010000) {
                            rc += 30;
                        }
                    }
                    subjob = subjob.getNextSibling();
                }
            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }

            System.out.format("CF ok=%d, error=%d%n", done, errCnt);
        }
        else {
            System.out.println("ArchFileM <xml> <pdf_folder> <rel_pdf_folder> [t_fmt_document]");
            rc += 15;
        }

        System.exit(rc);
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

    static void updateGUID (String tblName, String hostCode, String spoolName, long jobNo, int subjobNo, String guid) throws Exception {
        if (tblName != null) {
            String connName = "jdbc_ODS";
            String sqlCmd = "update " + tblName + " set CFGUID=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = (new Sql(connName)).getConn();
                conn.setAutoCommit(true);
                stmt = conn.prepareStatement(sqlCmd);
                stmt.setString(1, guid);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (stmt != null) { stmt.close(); stmt = null; }
                if (conn != null) { conn.close(); conn = null; }
            }
        }
    }

    static void updateErrMsg (String tblName, String hostCode, String spoolName, long jobNo, int subjobNo, String errMsg) throws Exception {
        if (tblName != null) {
            String connName = "jdbc_ODS";
            String sqlCmd = "update " + tblName + " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = (new Sql(connName)).getConn();
                conn.setAutoCommit(true);
                stmt = conn.prepareStatement(sqlCmd);
                stmt.setString(1, errMsg.length()>2000? errMsg.substring(0,2000):errMsg);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (stmt != null) { stmt.close(); stmt = null; }
                if (conn != null) { conn.close(); conn = null; }
            }
        }
    }

    static void cf_delete_pol (CFService port, String reportId, javax.xml.datatype.XMLGregorianCalendar dataDate, String printUnit, String policyCode) {
        
        try {
            java.lang.String _user = "SIP";

            java.util.List<java.lang.String> _queryWithIndex_reportIdList = new java.util.ArrayList<java.lang.String>();
            _queryWithIndex_reportIdList.add(reportId);

            com.transglobe.cf.webservice.SqueezeDate _queryWithIndex_squeezeDate = new com.transglobe.cf.webservice.SqueezeDate();
            //java.util.GregorianCalendar c = new java.util.GregorianCalendar();
            //javax.xml.datatype.XMLGregorianCalendar endDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            _queryWithIndex_squeezeDate.setEndDate(dataDate);

            java.lang.String _queryWithIndex_onlyLastVersion = "Y";

            com.transglobe.cf.webservice.bean.request.Clause _queryWithIndex_queryIndex = new com.transglobe.cf.webservice.bean.request.Clause();
            _queryWithIndex_queryIndex.and("printUnit", printUnit, com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);
            _queryWithIndex_queryIndex.and("policyCode", policyCode, com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);

            com.transglobe.cf.webservice.Sort _queryWithIndex_sort = new com.transglobe.cf.webservice.Sort();
            _queryWithIndex_sort.setIndexName("printDate");
            _queryWithIndex_sort.setOperation("DESC");

            java.lang.Integer _queryWithIndex_page = 1;
            java.lang.Integer _queryWithIndex_pageSize = -1;

            com.transglobe.cf.webservice.QueryResponse _queryWithIndex__return = port.queryWithIndex(_user, _queryWithIndex_reportIdList,
                _queryWithIndex_squeezeDate, _queryWithIndex_onlyLastVersion, _queryWithIndex_queryIndex, _queryWithIndex_sort,
                _queryWithIndex_page, _queryWithIndex_pageSize, "Y", "SIPFMT");

            if (_queryWithIndex__return != null) {
                java.util.List<QueryResult> qrl =  _queryWithIndex__return.getQueryResultList();
                for (QueryResult qr:qrl) {
                    java.util.List<Content> cl = qr.getContentList();
                    for (Content c:cl) {
                        String guid = c.getGuid();
                        System.out.println("delete: " + guid);
                        DeleteFile df = new DeleteFile();
                        df.delete(port, guid);
                    }
                }
            }

        } catch (Exception e) {
                e.printStackTrace();
        }    
    }

}
