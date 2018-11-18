//ToCFHelper.java
//
//ToCFHelper  <req_file> 

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.zip.*;
import java.net.URL;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import javax.xml.namespace.QName;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;
import sipservertype.*;
import sip.util.*;
import common.*;


public class ToCFHelper {
    static boolean sqlLogEnabled = true;

	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

    public static void main (String args[]) {
    	int rc = 0x00010000;

    	try {
        		URL wsdlURL = null;
                File wsdlFile = new File("c:\\sip\\cfg\\CFService.xml");
                try {
                    wsdlURL = wsdlFile.toURI().toURL();      
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            	CFServiceService ss = new CFServiceService(wsdlURL, SERVICE_NAME);
                CFService port = ss.getCFServicePort();  

                //enable MTOM
    			BindingProvider bp = (BindingProvider) port;
    			SOAPBinding binding = (SOAPBinding) bp.getBinding();
    			binding.setMTOMEnabled(true);

    			SysVar sv = new SysVar();
    			    
	        	String connName = "@jdbc_TGL";
	        	String connName2 = "@jdbc_ODS";
                //String fmtTblName = "t_fmt_document";
       	        String fmtTblName = sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
				String pdfFolder = sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");

	        	ToCFHelper toCF = new ToCFHelper(port, connName, connName2, args[0], pdfFolder, fmtTblName);
        		
        		toCF.start();

        		if (toCF._err_update_cnt > 0)
        			rc += 33;
        		else if (toCF._err_job_cnt > 0)
                    rc += 10;
        		
	    } catch (Exception e) {
				e.printStackTrace();
				rc += 98;
		}

		System.exit(rc);       
    }


    CFService _port;
    String _connName;
    String _connName2;
    String _reqFile;
	String _pdfFolder;
	String _fmtTblName;

    boolean _isPolicy = false;
	PreparedStatement _stmtUpdateGUID = null;
	PreparedStatement _stmtUpdateERRMSG = null;

	String _lastErr;

    int _seq = 0;

    public int _err_update_cnt = 0;
    public int _err_job_cnt = 0; 

    public ToCFHelper (CFService port, String connName, String connName2, String reqFile, String pdfFolder, String fmtTblName) {
    	_port = port;
    	_connName = connName;
    	_connName2 = connName2;
        _reqFile = reqFile;
    	_pdfFolder = pdfFolder;
    	_fmtTblName = fmtTblName;
    }

    public void start () throws Exception {

    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 

    	Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		ResultSet result = null;

  		Connection conn2 = null;

		try {

            Sql sql = new Sql(_connName);
			conn = sql.getConn();

			conn2 = (new Sql(_connName2)).getConn();
			conn2.setAutoCommit(true);

			String sqlStr;

            sqlStr="update " + "t_fmt_document" + " set CFGUID=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			_stmtUpdateGUID = sqlLogEnabled? new LoggableStatement(conn2, sqlStr) : conn2.prepareStatement(sqlStr);
			if (0==_seq) System.out.println(sqlStr);

            sqlStr="update " + "t_fmt_document" + " set ERRMSG=?,CFGUID=NULL where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			_stmtUpdateERRMSG = sqlLogEnabled? new LoggableStatement(conn2, sqlStr) : conn2.prepareStatement(sqlStr);
			if (0==_seq) System.out.println(sqlStr);

			sqlStr="SELECT f.HOSTCODE,f.SPOOLNAME,f.JOBNO,f.SUBJOBNO,f.SHORT_NAME,f.LIST_ID,c.CONTENT FROM " + _fmtTblName + " f" + 
 " JOIN T_DOCUMENT d ON f.LIST_ID=d.LIST_ID" + 
 " JOIN T_DOCUMENT_DATA dd ON f.LIST_ID=dd.LIST_ID" + 
 " JOIN T_CLOB c ON dd.CLOB_ID=c.CLOB_ID" + 
 " WHERE HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, sqlStr) : conn.prepareStatement(sqlStr);

            au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader(_reqFile));
    
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String hostCode = nextLine[0];
                String spoolName = nextLine[1];
                long jobNo = Long.parseLong(nextLine[2]);
                int subjobNo = Integer.parseInt(nextLine[3]);

                if (0==_seq)
                    sql.showSql(sqlStr, hostCode, spoolName, jobNo, subjobNo);
                
                stmtSelect.setString(1, hostCode);
                stmtSelect.setString(2, spoolName);
                stmtSelect.setLong(3, jobNo);
                stmtSelect.setInt(4, subjobNo);
                result = stmtSelect.executeQuery();

                ++_seq;

                if (result.next()) {
                    String shortName = result.getString("SHORT_NAME");
                    long listId = result.getLong("LIST_ID");

                    System.out.format("%s %d hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",  sdf.format(new java.util.Date()), _seq,
                        hostCode, spoolName, jobNo, subjobNo, shortName, listId);

                    Clob rptData = result.getClob("CONTENT");
                    if (result != null) { result.close(); result = null; }
                    byte[] baData = rptData.getSubString(1, (int)rptData.length()).getBytes("UTF-8");

                    Document dom = db.parse(new ByteArrayInputStream(baData));

                    String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo);
                    String fileName = String.format("%s-%s-%08x-%06x.pdf", hostCode, spoolName, jobNo, subjobNo);

                    String guid = do_one(dom, srcPdf, fileName, null);
                    if (guid != null) {
                        updateGUID(_stmtUpdateGUID, hostCode, spoolName, jobNo, subjobNo, guid);
                        if ("FMT_POS_0190".equals(shortName)) {
                            srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.DOC.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo);
                            if ((new File(srcPdf)).exists()) {
                                fileName = String.format("%s-%s-%08x-%06x-DOC.pdf", hostCode, spoolName, jobNo, subjobNo);
                                System.out.format("%s,", srcPdf);
                                do_one(dom, srcPdf, fileName, "FMT_POS_0190_DOC");
                            }
                            srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.TIF.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo);
                            if ((new File(srcPdf)).exists()) {
                                fileName = String.format("%s-%s-%08x-%06x-TIF.pdf", hostCode, spoolName, jobNo, subjobNo);
                                System.out.format("%s,", srcPdf);
                                do_one(dom, srcPdf, fileName, "FMT_POS_0190_TIF");
                            }
                        }
                    }
                    else {
                    	_err_job_cnt++;

                        if (_lastErr==null)
                            _lastErr = "Error";

						System.out.println(_lastErr);
			
                        updateErrMsg(_stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, _lastErr);
                    }
                }
                if (result != null) { result.close(); result = null; }
            }//while ((nextLine = reader.readNext()) != null) {
    	}
		catch (Exception e) {
			handleException(e);
			_err_update_cnt++;
		}
		finally {
			if (result != null) { result.close(); result = null; }
			if (_stmtUpdateGUID != null) { _stmtUpdateGUID.close(); _stmtUpdateGUID = null; }
			if (_stmtUpdateERRMSG != null) { _stmtUpdateERRMSG.close(); _stmtUpdateERRMSG = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
			if (conn2 != null) { conn2.close(); conn2 = null; }
		}

    }

    void handleException (Exception e) {
		if (e instanceof SQLException) {
			System.err.println("SQLState=" + ((SQLException)e).getSQLState());
			System.err.println("ErrorCode=" + ((SQLException)e).getErrorCode());
		}
		e.printStackTrace();
	}

	String do_one (Document dom, String pdfFile, String fileName, String reportId) {
		String guid = null;

		_lastErr = null;

		try {
			String user = "SIP";
            String repeatArchive = "Y";

						//String reportId = "";
                        String printUnit = "";
                        String policyCode = "";
                        String overWriteOption = _isPolicy? "NON":"ALL";
                        javax.xml.datatype.XMLGregorianCalendar dataDate = null;

                        com.transglobe.cf.webservice.Content content = new com.transglobe.cf.webservice.Content();

                        NodeList nl = dom.getElementsByTagName("index").item(0).getChildNodes();
                        for (int i = 0; i < nl.getLength(); i++) {
                            Node n = nl.item(i);
                            if (n.getNodeType() == Node.ELEMENT_NODE) {
                                if ("reportId".equalsIgnoreCase(n.getNodeName())) {
                                    if (reportId==null) reportId = n.getTextContent();
                                    content.setReportId(reportId);
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
                                        if (_isPolicy) {
                                        	if ("printUnit".equalsIgnoreCase(key)) printUnit = val;
                                        	else if ("policyCode".equalsIgnoreCase(key)) policyCode = val;
                                        }
                                    }
                                }
                            }
                        }

                        nl = dom.getElementsByTagName("isOverriding");
                        if (nl != null && nl.getLength() > 0 && "Y".equalsIgnoreCase(nl.item(0).getTextContent())) {
                            overWriteOption = "ALL";
                            if (_isPolicy) {
                                //nl = ((Element)subjob).getElementsByTagName("printMode");
                                //if (nl != null && nl.getLength() > 0 && "2".equalsIgnoreCase(nl.item(0).getTextContent())) {  //2.新契約重製
                                    cf_delete_pol(_port, reportId, dataDate, printUnit, policyCode);
                                //}  
                            }
                        }

                        content.setContentData(new DataHandler(new FileDataSource(pdfFile)));
                        if (_isPolicy) {
                        	fileName = policyCode + "-" + fileName;

                        } else {
	                        String documentNo = "";
	                        nl = dom.getElementsByTagName("documentNo");  
	                        if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent(); 
	                        else {
	                            nl = dom.getElementsByTagName("policyCode");  
	                            if (nl != null && nl.getLength() > 0) documentNo = nl.item(0).getTextContent();
	                        } 
	                        fileName = documentNo + "-" + fileName; 
                        }
                        content.setContentName(fileName);
                        content.setContentType("pdf");

                        com.transglobe.cf.webservice.ArchiveResponse res = _port.archiveFile(user, overWriteOption, repeatArchive, content, "Y", "SIPFMT");

                        boolean hasErr = true;

                        if (res != null) {
                            content = res.getContent();
                            if (content != null) {
                              	guid = content.getGuid();
                                System.out.println("guid="+guid);   
                                hasErr = false;
                            }
                        }
                        if (hasErr && res!=null) {
                            com.transglobe.cf.webservice.Response res1 = res;
                            _lastErr = res1.getReturnCode()+" "+res1.getReturnMsg();
                        }        
        } catch (Exception e) {
			e.printStackTrace();
			_lastErr = e.toString();
		}

		return guid;
	} 

	javax.xml.datatype.XMLGregorianCalendar toDate (String dStr) throws Exception {
        java.util.Calendar ca = javax.xml.bind.DatatypeConverter.parseDateTime(dStr);
                            java.util.GregorianCalendar c = new java.util.GregorianCalendar();
                            c.setTime(ca.getTime());
                            javax.xml.datatype.XMLGregorianCalendar date = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return date;                 
    }

    void addIdx (com.transglobe.cf.webservice.Content content, String key, String value) throws Exception {
         Index idx = new Index();
                    idx.setKey(key);
                    boolean isDate = value.length()>=10 && value.charAt(4)=='-' && value.charAt(7)=='-';
                    if (isDate) idx.setValue(toDate(value));
                    else idx.setValue(value);
                    content.getIndexList().add(idx);
    }

    void updateGUID (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, String guid) throws Exception {
            try {
                stmt.setString(1, guid);
                stmt.setString(2, hostCode);
                stmt.setString(3, spoolName);
                stmt.setLong(4, jobNo);
                stmt.setInt(5, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
                _err_update_cnt++;
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
                _err_update_cnt++;
            }
    }

	void cf_delete_pol (CFService port, String reportId, javax.xml.datatype.XMLGregorianCalendar dataDate, String printUnit, String policyCode) {
        
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
