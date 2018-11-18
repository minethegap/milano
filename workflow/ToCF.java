//ToCF.java
//
//ToCF  <sql_name> <tbl_name> <pdf_folder>

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.transglobe.cf.webservice.CFService;
import com.transglobe.cf.webservice.CFServiceService;
import com.transglobe.cf.webservice.Content;
import com.transglobe.cf.webservice.QueryResult;
import com.transglobe.cf.webservice.bean.Index;

import common.DeleteFile;
import common.LoggableStatement;
import common.Utils;
import sip.util.DbConnStr;
import sip.util.SysVar;
import sipservertype.Sipserver;
import sipservertype.SipserverPortType;


public class ToCF {
	
	private final static Logger logger = Logger.getLogger(ToCF.class);
	
	
    static boolean sqlLogEnabled = true;

	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

    public static void main (String args[]) {

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

	        	String sqlName = args.length >= 1 && !Utils.isEmpty(get_arg(args,0)) ? get_arg(args,0):"ToCF.sql";
	        	boolean isPolicy = sqlName.toUpperCase().contains("POLICY");
	        	String vpName = args.length >= 2 && !Utils.isEmpty(get_arg(args,1)) ? get_arg(args,1):null;
	        	String fmtTblName = args.length >= 3 && !Utils.isEmpty(get_arg(args,2)) ? get_arg(args,2):isPolicy?sv.get("POLICY_TBL","stage.t_fmt_policy@ODSLINK"):sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
				String pdfFolder = args.length >= 4 && !Utils.isEmpty(get_arg(args,3)) ? get_arg(args,3):sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");
	        	
	        	String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\"+sqlName)));

	        	System.out.println(fmtTblName);
	        	System.out.println(sqlSelect);
                System.out.format("vpName=%s%n", !Utils.isEmpty(vpName)? vpName:"");

                int sleepSec = 5*60;

                try {
                    sleepSec= Integer.parseInt(get_arg_opt(args,'s'));
                } catch (Exception en) {
                    //en.printStackTrace();
                }

               logger.trace(String.format("sleepSec=%d%n", sleepSec)); 

	        	ToCF toCF = new ToCF(port, connName, sqlSelect, pdfFolder, fmtTblName, vpName);
        		
        		while (true) {
        			toCF.start();
        			Thread.sleep(sleepSec*1000);
        		}

	    } catch (Exception e) {
				e.printStackTrace();
		}
        
    }

    static String get_arg (String args[], int index) {
        int seq = 0;
        for (int i=0; i<args.length; ++i)
            if (args[i].charAt(0)=='-') {

            }
            else {
                if (seq==index) return args[i];
                ++seq;
            }
        return null;
    }

    static String get_arg_opt (String args[], char c) {
        int seq = 0;
        for (int i=0; i<args.length; ++i)
            if (args[i].charAt(0)=='-') {
                if (args[i].charAt(1)==c) return args[i].substring(2);
                ++seq;
            }
            else {
                
            }
        return null;
    }


    CFService _port;
    String _connName;
	String _sqlSelect;
	String _pdfFolder;
	String _fmtTblName;
	String _vpName;

	boolean _isPolicy;
	PreparedStatement _stmtUpdateGUID = null;
	PreparedStatement _stmtUpdateERRMSG = null;

	String _lastErr;

	int _seq = 0;
	int _jobs = 0;
	StringBuilder _sb;
	int _sta_seq;

	Connection conn = null;

    public ToCF (CFService port, String connName, String sqlSelect, String pdfFolder, String fmtTblName, String vpName) {
    	_port = port;
    	_connName = connName;
    	_sqlSelect = sqlSelect;
    	_pdfFolder = pdfFolder;
    	_fmtTblName = fmtTblName;
    	_vpName = vpName;

    	_isPolicy = fmtTblName.contains("POLICY");
    }

    public void start () throws Exception {
    	
    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Start Process Arching Request!!!");
    	}

    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 

    	
  		PreparedStatement stmtSelect = null;
  		
  		ResultSet result = null;

		try {
			DbConnStr dc = new DbConnStr();
			String strDB = dc.getConnStr(_connName);
			String strDrv = dc.getJdbcDriver(_connName);

			Class.forName(strDrv).newInstance();

			conn = DriverManager.getConnection(strDB);
			conn.setAutoCommit(null != _vpName? false:true);

            String sqlGUID = "update " + _fmtTblName + " set CFGUID=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
            String sqlERRMSG = "update " + _fmtTblName + " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			_stmtUpdateGUID = sqlLogEnabled? new LoggableStatement(conn, sqlGUID) : conn.prepareStatement(sqlGUID);
			_stmtUpdateERRMSG = sqlLogEnabled? new LoggableStatement(conn, sqlERRMSG) : conn.prepareStatement(sqlERRMSG);
			
			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);

			result = stmtSelect.executeQuery();
			while (result.next()) {
				String hostCode = result.getString("HOSTCODE");
				String spoolName = result.getString("SPOOLNAME");
				long jobNo = result.getLong("JOBNO");
				int subjobNo = result.getInt("SUBJOBNO");

                String shortName = result.getString("SHORT_NAME");
				long listId = result.getLong("LIST_ID");

				System.out.format("%s %d hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",  sdf.format(new java.util.Date()), ++_seq,
	 				hostCode, spoolName, jobNo, subjobNo, shortName, listId);

				if (null != _vpName) {

					add_one(hostCode, spoolName, jobNo, subjobNo);
					updateGUID(_stmtUpdateGUID, hostCode, spoolName, jobNo, subjobNo, "C");

				} else {

					Clob rptData = result.getClob("CONTENT");
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
		 				if (_lastErr==null)
		 					_lastErr = "Error";
		 				updateErrMsg(_stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, _lastErr);
		 			}
	 			}
			}

    	}
		catch (Exception e) {
			handleException(e);
		}
		finally {
			if (result != null) { result.close(); result = null; }
			if (_stmtUpdateGUID != null) { _stmtUpdateGUID.close(); _stmtUpdateGUID = null; }
			if (_stmtUpdateERRMSG != null) { _stmtUpdateERRMSG.close(); _stmtUpdateERRMSG = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { if (null != _vpName) conn.commit(); conn.close(); conn = null; }
		}

		commit();

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


    void commit () {
    	if (_vpName != null && _jobs > 0) {
    		try {	
				Sipserver ss = new Sipserver((new File("c:\\sip\\cfg\\sipserver2.wsdl")).toURI().toURL(), new javax.xml.namespace.QName("urn:sipserverType", "sipserver"));
				SipserverPortType port = ss.getSipserver();

				//enable MTOM
				BindingProvider bp = (BindingProvider) port;
				SOAPBinding binding = (SOAPBinding) bp.getBinding();
				binding.setMTOMEnabled(true);

				DataHandler reqFile = new DataHandler(new ByteArrayDataSource(_sb.toString().getBytes("UTF-8"), "application/octet-stream"));

		 				Holder<String> jobId = new Holder<String>();
						Holder<String> error = new Holder<String>();
						Holder<DataHandler> resFile = new Holder<DataHandler>();

						String jobName = String.format("ToCFHelper %d-%d", _sta_seq, _jobs);

						port.sendJobAndGetJobFile("local", "TO_CF", _vpName, jobName, reqFile, -2, "", null,
							jobId, error, resFile);

						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						System.out.format("%s %s, jobId=%s, error=%s%n", sdf.format(new java.util.Date()), jobName, jobId.value, error.value);

				if (conn != null) conn.commit();	

	    		_jobs = 0;	
	    	} catch (Exception e) {
            	e.printStackTrace();
        	}    
    	}
    }

    void add_one (String hostCode, String spoolName, long jobNo, int subjobNo) {
    	if (_vpName != null) {
    		if (0==_jobs) {
    			_sb = new StringBuilder();
    			_sta_seq = _seq;
    		}
    		_sb.append(String.format("%s,%s,%d,%d%n", hostCode, spoolName, jobNo, subjobNo) );
    		if (++_jobs >= 5000)
    			commit();
    	}
    }

}
