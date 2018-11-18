//ToPrint.java
//
//ToPrint <TEST> <sql_name> <tbl_name> <pdf_folder>

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.zip.*;
import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.namespace.QName;
//import javax.mail.util.ByteArrayDataSource;
import javax.activation.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;
import com.tgl.esp.ws.server.service.*;
import sip.util.*;
import common.*;


public class ToPrint {
    static boolean sqlLogEnabled = true;
   
    public static void main (String args[]) {
        int rc = 0x00010000;

    	try {	    
	        	String connName = "@jdbc_TGL";

                SysVar sv = new SysVar();

                //boolean testMode = args.length >= 1 && "TEST".equals(args[0]) ? true:false;
                boolean testMode = false;
                String sqlName = args.length >= 1 && !Utils.isEmpty(args[0]) ? args[0]:"ToPrint.sql";
                boolean isPolicy = sqlName.toUpperCase().contains("POLICY");
                boolean isPolicyBatch = false;
                if (isPolicy && !sqlName.toUpperCase().contains("URGENT"))
                    isPolicyBatch = true;   
                boolean isAdhoc = sqlName.contains("_A");
	        	//String fmtTblName = args.length >= 2 && !Utils.isEmpty(args[1]) ? args[1]:isPolicy?sv.get("POLICY_TBL","stage.t_fmt_policy@ODSLINK"):sv.get("DOC_TBL","stage.t_fmt_document@ODSLINK");
				//String pdfFolder = args.length >= 3 && !Utils.isEmpty(args[2]) ? args[2]:sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");

                String fmtTblName = isPolicy?sv.get("POLICY_TBL","t_fmt_policy"):sv.get("DOC_TBL","t_fmt_document");
                String pdfFolder = sv.get("HA_FOLDER","\\\\tglcifs\\sip_ha");

                boolean isOnce = false;
                if (args.length >= 2 && !Utils.isEmpty(args[1]) && "ONCE".equals(args[1])) isOnce = true;
	        	
	        	//String sqlSelect = new String(Files.readAllBytes(Paths.get("c:\\sip\\cfg\\"+sqlName)));
	        	String sqlSelect = sqlName.charAt(0)=='@' ?  new String(Files.readAllBytes(Paths.get(sqlName.substring(1)))) :
	        		sv.getSql(sqlName);

	        	System.out.println(sqlName);
	        	System.out.println(sqlSelect);
	        	System.out.println(fmtTblName);
	        	System.out.println(pdfFolder);

	        	ToPrint toPrint = new ToPrint(isAdhoc, connName, sqlSelect, pdfFolder, fmtTblName, isPolicy, isPolicy?"VP_TO_YET":"VP_TO_BET");

                if (isOnce && isPolicy) toPrint.isPolicyFmtEnd();
        		
        		while (true) {
                    if (!isPolicyBatch || isOnce || toPrint.needPrint()) {
                        if (isPolicyBatch) {
                            toPrint.getBatch(isOnce);
                        }
            			boolean b = toPrint.start(testMode);
                        if (isOnce) { 
                            if (!b) rc += 30;
                            break;
                        }
                    }
        			Thread.sleep(60*60*1000);//1 hour
        		}

	    } catch (Exception e) {
				e.printStackTrace();
                rc += 98;
		}
        
        System.exit(rc); 
    }


    int _policyCount = 0;
    String _policyBatchId = null;
    int _total_dataFile = 0;
    int _total_pdf = 0;
    boolean _no_ctrl = false;


    void getBatch (boolean last) {
        _no_ctrl = !last;

        _policyBatchId = null;
        _total_dataFile = 
        _total_pdf = 0;    

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet result = null;  

        try {
                conn = (new Sql(_connName)).getConn();
                stmt = conn.prepareStatement("select * from T_FMT_POLICY_BATCH where BATCH_END is null order by BATCHID desc");
                result = stmt.executeQuery();
                if (result.next()) {
                    _policyBatchId = result.getString("BATCHID");
                    _total_dataFile = result.getInt("TOTAL_DATAFILE");
                    _total_pdf = result.getInt("TOTAL_PDF");
                }
                result.close(); result = null;
                stmt.close(); stmt = null;
                if (_policyBatchId==null) {
                    java.util.Date curDt = new java.util.Date();
                    String batchTimeStr = (new java.text.SimpleDateFormat("yyyyMMddHHmmss")).format(curDt);
       
                    _policyBatchId = "SIP-PM-" + "POLICY" + "-" + batchTimeStr;

                    stmt = conn.prepareStatement("insert into T_FMT_POLICY_BATCH (BATCHID) VALUES (?)");
                    stmt.setString(1,_policyBatchId);
                    stmt.execute();
                }
                System.out.format("%s %s prev_dataFile=%d prev_pdf=%d%n", sdf.format(new java.util.Date()), _policyBatchId, _total_dataFile, _total_pdf);
        } catch (Exception e) {
                    e.printStackTrace();
             }
        finally {
                    try {
                        if (result != null) { result.close(); result = null; }
                        if (stmt != null) { stmt.close(); stmt = null; }
                        if (conn != null) { conn.close(); conn = null; }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }        
             }
    }

    boolean needPrint () {
        boolean rc = false;

        if (!(new File("c:\\sip\\spool\\dobatch.active")).exists()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str;
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                conn = (new Sql(_connName)).getConn();
                stmt = conn.prepareStatement(str=(new SysVar()).getSql("ToPrint_Policy_Count.sql"));
                result = stmt.executeQuery();
                int cnt = result.next()? result.getInt(1):-1;
                if (cnt >= 50)  {
                    rc = true;
                    _policyCount = cnt/50 * 50;
                }
                System.out.format("%s %s%ncount=%d%n", sdf.format(new java.util.Date()), str, cnt);
             } catch (Exception e) {
                    e.printStackTrace();
             }
             finally {
                    try {
                        if (result != null) { result.close(); result = null; }
                        if (stmt != null) { stmt.close(); stmt = null; }
                        if (conn != null) { conn.close(); conn = null; }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }        
             }
        }

        return rc;
    }

    boolean isPolicyFmtEnd () {
        boolean rc = false;

        int prevCnt = -1;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strSql;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            conn = (new Sql(_connName)).getConn();
            stmt = conn.prepareStatement(strSql = "SELECT count(JOB_ID) FROM " + _fmtTblName +
                                            " WHERE STAGE=0 AND ERRMSG IS NULL");
            System.out.format("%s %s%n", sdf.format(new java.util.Date()), strSql);

            while (true) {
                result = stmt.executeQuery();
                int cnt = result.next()? result.getInt(1):-1;
                result.close(); result = null;
                if (cnt != prevCnt) {
                    prevCnt = cnt;
                    System.out.format("%s count=%d%n", sdf.format(new java.util.Date()), cnt);
                }
                if (cnt==0) {
                    rc = true;
                    break;
                }
            
                Thread.sleep(1000*60);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (result != null) { result.close(); result = null; }
                if (stmt != null) { stmt.close(); stmt = null; }
                if (conn != null) { conn.close(); conn = null; }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return rc;
    }


    boolean _isAdhoc;
    String _connName;
	String _sqlSelect;
	String _pdfFolder;
	String _fmtTblName;
	boolean _isPolicy;
    String _vpName;
    String _sender;

	PreparedStatement _stmtUpdatePRINTED = null;
	PreparedStatement _stmtUpdateERRMSG = null;

    PreparedStatement _stmtUpdatePolicyBatch = null;    

	String _lastErr;

    final String EMPTY_STR = "\"\"";


/*  暫不郵寄件(01)
    單位件(02)
    平信(03)
    單掛(04)
    雙掛(05)
    國際快捷(06)

    國際航空函件(07)
    國際航空函件掛號(08)
    限時專送(09)
*/    
    final String[] _sendModeStr = {"01","04","05","02","06","07","08","03"};

/* postWay:
0. 暫不郵寄
1. 一般件(單褂)  
2. 雙掛件  
3. 單位件
4. 國際快遞
5. 國際航空函件
6. 國際航空函件掛號
7. 平信
*/
    String postWayToSendMode (String postWay) {
         int postWayInt = 3;
                        try {
                            postWayInt = Integer.parseInt(postWay);
                            if (postWayInt>=0 && postWayInt<=7)
                                ;
                            else postWayInt = 3;
                        } catch (Exception ex) {};
        return _sendModeStr[postWayInt];
    }

    public ToPrint (boolean isAdhoc, String connName, String sqlSelect, String pdfFolder, String fmtTblName, boolean isPolicy, String vpName) {
        _isAdhoc = isAdhoc;
    	_connName = connName;
    	_sqlSelect = sqlSelect;
    	_pdfFolder = pdfFolder;
    	_fmtTblName = fmtTblName;
    	_isPolicy = isPolicy;
        _vpName = vpName;
        _sender = null;
    }

    public ToPrint (boolean isAdhoc, String connName, String sqlSelect, String pdfFolder, String fmtTblName, boolean isPolicy, String vpName, String sender) {
        _isAdhoc = isAdhoc;
        _connName = connName;
        _sqlSelect = sqlSelect;
        _pdfFolder = pdfFolder;
        _fmtTblName = fmtTblName;
        _isPolicy = isPolicy;
        _vpName = vpName;
        _sender = sender;
    }

    public boolean start () throws Exception {
    	return start(false);
    }

    public boolean start (boolean testMode) throws Exception {
        boolean rc = true;

    	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 

    	Connection conn = null;
  		PreparedStatement stmtSelect = null;
  		ResultSet result = null;

  		Connection connESP = null;
        PreparedStatement stmtInsert = null;

  		String prev_shortName = null;
        String deptId = null;
  		BatchPrint bp = null;
  		
        int jobs = 0;
        int done = 0;

        String needEraFlag = null; 

        boolean isPos190 = false;

		try {
			conn = (new Sql(_connName)).getConn();
			conn.setAutoCommit(false);

			connESP = (new Sql("@jdbc_ESP")).getConn();
			connESP.setAutoCommit(false);

            stmtInsert = connESP.prepareStatement("INSERT INTO esp.CUST_CONTACT_LOG (CONTACT_SYS_ID,CONTACT_SYS_OPERATOR_ID," + 
"FUNCTION_CATEGORY,REPORT_ID,POLICY_NO,PROPOSER_ID,CONTACT_METHOD,CONTACT_END_POINT_VAL,CONTACT_DATE_TIME,CONTACT_SUBJECT,CONTACT_BODY)" +
"VALUES ('FMT','SIP',?,?,?,?,'POSTMAIL',?,?,?,?)");

            String sqlPRINTED = _isPolicy? "update " + _fmtTblName + " set STAGE=?,PRINTID=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?" :
                "update " + _fmtTblName + " set PRINTED=?,PRINTID=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
            String sqlERRMSG = "update " + _fmtTblName + " set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
			_stmtUpdatePRINTED = sqlLogEnabled? new LoggableStatement(conn, sqlPRINTED) : conn.prepareStatement(sqlPRINTED);
			_stmtUpdateERRMSG = sqlLogEnabled? new LoggableStatement(conn, sqlERRMSG) : conn.prepareStatement(sqlERRMSG);

            String sqlUpdatePolicyBatch = "update T_FMT_POLICY_BATCH set TOTAL_DATAFILE=?,TOTAL_PDF=?,BATCH_END=?,UPATETIME=sysdate where BATCHID=?" ; 
			if (_policyBatchId != null)
                _stmtUpdatePolicyBatch = sqlLogEnabled? new LoggableStatement(conn, sqlUpdatePolicyBatch) : conn.prepareStatement(sqlUpdatePolicyBatch);

			stmtSelect = sqlLogEnabled? new LoggableStatement(conn, _sqlSelect) : conn.prepareStatement(_sqlSelect);

			result = stmtSelect.executeQuery();
			while (result.next()) {
				String printId = "";

				String hostCode = result.getString("HOSTCODE");
				String spoolName = result.getString("SPOOLNAME");
				long jobNo = result.getLong("JOBNO");
				int subjobNo = result.getInt("SUBJOBNO");
				String shortName = result.getString("SHORT_NAME");
                long listId = result.getLong("LIST_ID");
                String duplex = "1";
                String deliverMethod = "3";
                try {
						duplex = result.getString("duplex");
					} catch (SQLException e) {
					}
				try {
						deliverMethod = result.getString("deliverMethod");
					} catch (SQLException e) {
					}

				String policyCode = "";

				String isReprint = null;
				String isUrgent = null;	
				if (_isPolicy) {
					try {
						isReprint = result.getString("isReprint");
					} catch (SQLException e) {
					}
					try {
						isUrgent = result.getString("isUrgent");
					} catch (SQLException e) {
					}

					if ("Y".equals(isReprint))
						try {
							policyCode = result.getString("POLICY_CODE");
						} catch (SQLException e) {
						}
				}

                

				System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, shortName=%s, listId=%d%n",  sdf.format(new java.util.Date()), 
	 				hostCode, spoolName, jobNo, subjobNo, shortName, listId);

				Clob rptData = result.getClob("CONTENT");
	 			byte[] baData = rptData==null?null:rptData.getSubString(1, (int)rptData.length()).getBytes("UTF-8");

                ++jobs;

                boolean printed = false;

                if (!shortName.equals(prev_shortName)) {
                        if (_isPolicy) needEraFlag = "N";
                        else if (_isAdhoc) needEraFlag = "Y";
                        else  {//batch letter
                            needEraFlag = "N";
                            try {
                                if ("Y".equals(result.getString("ISESUB"))) {

                                } else {
                                    needEraFlag = "Y";
                                }
                            } catch (SQLException e) {
                                System.out.print("ISESUB:");
                                e.printStackTrace();
                            }
                        }

                        isPos190 = "FMT_POS_0190".equals(shortName);

                		prev_shortName = shortName;

                        deptId = get_dept(shortName);
                     
                		if (bp != null) {
                            bp.commit();
                            if (conn != null) conn.commit();    
                            if (connESP != null) connESP.commit();    
                        }

                		bp = new BatchPrint(_isAdhoc, shortName, duplex, deliverMethod, _isPolicy, isPos190?"VP_TO_YET":_vpName,
                              _policyBatchId, _total_dataFile, _total_pdf, _no_ctrl);
                        bp.setSender(_sender);
                        bp.setNeedEra("Y".equals(needEraFlag));
                	}

                String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", _pdfFolder, hostCode, spoolName, jobNo, subjobNo);
                String uid = String.format("%s-%s-%08x-%06x", hostCode, spoolName, jobNo, subjobNo);
                String pdfName = String.format("%s.pdf", uid);

                try {

                	if (!(new File(srcPdf)).exists())
                		throw new Exception("File not found " + srcPdf);

                	Document dom = null;
                	if (!"Y".equals(isReprint)) dom=db.parse(new ByteArrayInputStream(baData));

                    
                    String policyHolderId = "";
                    String postWay = null;
                    String sendMode = "03";//平信
                    String id = "";
                    String name = "";
                    String zip = "";
                    String addr = "";

                    String dataDateStr = ""; 
                    
                    //collect info from xml
                    String infoStr = "";

                    if (_isPolicy) {
		shortName = "#POLICY#";
		if ("Y".equals(isReprint)) {
						infoStr = String.format(",%s,%s,%s",
                    		EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                    		);
						infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    		forCSV(policyCode),
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
                    		forCSV(isReprint),
                    		forCSV(isUrgent),
                    		EMPTY_STR
                    		);
						infoStr += String.format(",%s,%s,%s,%s", 
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR //senderPhone
                            );
						infoStr += String.format(",%s,%s,%s,%s",
                            EMPTY_STR,
                    		EMPTY_STR,
                    		EMPTY_STR,
                    		EMPTY_STR 		
                    		);
						infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR, //???
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR, //brbdBranchCode
                            EMPTY_STR, //brbdBranchName
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                            );
                        sendMode = "01";//暫不郵寄

		} else {
                        Node basicInfo = Xml.getNode(dom, "/printRoot/policyInfo/basicInfo");
                        //Node agent = Xml.getFstNode(dom.getDocumentElement(), "agent"); 
                        NodeList agents = dom.getElementsByTagName("agent");
                    	Node printInfo = Xml.getNode(dom, "/printRoot/printInfo");
                        String printIndi = Xml.getNodeText(printInfo, "printIndi");
                    	Node postRule = Xml.getNode(printInfo, "postRule");
                    	Node idxNode = Xml.getNode(dom, "/printRoot/index");
                    	infoStr = String.format(",%s,%s,%s",
                    		forCSV(dataDateStr=getDateStr(Xml.getNodeText(idxNode, "dataDate"))),
                            EMPTY_STR,
                            EMPTY_STR
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    		forCSV(policyCode=getTagValue(idxNode, dom, "policyCode")),
                            forCSV(getMasterPlanCode(dom)),
                    		forCSV(policyHolderId=getTagValue(idxNode, dom, "policyHolderId")),
                            forCSV(getTagValue(idxNode, dom, "holderName")),
                    		forCSV(getTagValue(idxNode, dom, "insuredId")),
                    		forCSV(getTagValue(idxNode, dom, "insuredName")),
                            //forCSV(getTagValue(agent, null, "registerCode")),
                            //forCSV(getTagValue(agent, null, "name")),
                            forCSV(nodeListInfo(agents, "registerCode")),
                            forCSV(nodeListInfo(agents, "name")),   
                            forCSV(getTagValue(printInfo, dom, "vipHolderIndi")), //vipIndi
                    		forCSV(getTagValue(printInfo, dom, "printECNoticeIndi")),
                    		forCSV(getTagValue(printInfo, dom, "remakeIndi")),
                    		forCSV(isReprint),
                    		forCSV(isUrgent),
                    		forCSV("2".equals(printIndi)? "Y":"N")
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s", 
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR //senderPhone
                            );
                    	infoStr += String.format(",%s,%s,%s,%s",
                            EMPTY_STR,
                    		forCSV(name=getTagValue(postRule, null, "receiverName")),
                    		forCSV(zip=getTagValue(postRule, null, "zipCode")),
                    		forCSV(addr=getTagValue(postRule, null, "postAddress")) 		
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            forCSV(getTagValue(basicInfo, null, "channelType")),
                            forCSV(getTagValue(basicInfo/*agents.item(0)*/, null, "orgCode")),
                            forCSV(getTagValue(basicInfo/*agents.item(0)*/, null, "orgName")),
                            forCSV(getTagValue(postRule, null, "receiverName")), //???
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            forCSV(getTagValue(postRule, null, "organCode")),
                            forCSV(getTagValue(postRule, null, "organName")),
                            EMPTY_STR, //brbdBranchCode
                            EMPTY_STR, //brbdBranchName
                            forCSV(postWay=getTagValue(postRule, null, "postWay")),   
                            EMPTY_STR,
                            EMPTY_STR,
                            forCSV(getTagValue(basicInfo, null, "ackDueDate"))
                            );
                        id = policyHolderId; //???
                        sendMode = postWayToSendMode(postWay);
            }//else of if ("Y".equals(isReprint)) {
                    }
                    else {
                        Node basicInfo = Xml.getNode(dom, "/WSLetterUnit/basicInfo");
                        Node extension = Xml.getNode(dom, "/WSLetterUnit/extension");
                    	Node address = Xml.getNode(dom, "/WSLetterUnit/addressList/address");
                    	Node idxNode = Xml.getNode(dom, "/WSLetterUnit/index");
                    	infoStr = String.format(",%s,%s,%s",
                      		forCSV(dataDateStr=getDateStr(Xml.getNodeText(idxNode, "dataDate"))),
                            EMPTY_STR,
                            EMPTY_STR
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    	    forCSV(policyCode=getTagValue(idxNode, dom, "policyCode")),
                            "FMT_PA_0860".equals(shortName)? forCSV(getTagValue(extension, null, "rcptCode")):EMPTY_STR,
                    		forCSV(policyHolderId=getTagValue(idxNode, dom, "policyHolderId")),
                            isPos190?forCSV(getTagValue(idxNode, dom, "holderName")):EMPTY_STR,
                            EMPTY_STR,
                            isPos190?forCSV(getTagValue(idxNode, dom, "insuredName")):EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s", 
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR
                            );
                    	infoStr += String.format(",%s,%s,%s,%s",
                    		forCSV(id=getTagValue(address, null, "receiverId")),
                    		forCSV(name=getTagValue(address, null, "receiverName")),
                    		forCSV(zip=getTagValue(address, null, "addressCode")),
                    		forCSV(addr=getTagValue(address, null, "address"))
                    		);
                    	infoStr += String.format(",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            EMPTY_STR,
                            forCSV(getTagValue(extension, null, "collectorOrgCode")),
                            forCSV(getTagValue(extension, null, "collectorOrgName")),
                            forCSV(getTagValue(extension, null, "collectorOrgAddress")),
                            forCSV(getTagValue(extension, null, "collectorName")),
                            forCSV(getTagValue(basicInfo, null, "brbdCompanyCode")),
                            forCSV(getTagValue(basicInfo, null, "brbdCompanyName")),
                            forCSV(getTagValue(basicInfo, null, "brbdBranchCode")),
                            forCSV(getTagValue(basicInfo, null, "brbdBranchName")),
                            forCSV(postWay=getTagValue(extension, null, "postWay")), //postWay
                            forCSV(getTagValue(extension, dom, "currency")),
                            forCSV(getTagValue(extension, dom, "isOIU")),
                            EMPTY_STR  //ackDueDate
                            );

                        if (Utils.isEmpty(postWay))
                            sendMode = Utils.shortNameToSendMode(shortName);
                        else
                            sendMode = postWayToSendMode(postWay);
                    } 

                    printId = bp.add(infoStr, srcPdf, pdfName, shortName, sendMode, id, name, zip, addr, String.format("%d_%s", listId, uid), dataDateStr, policyCode );

                    if (!testMode && !_isPolicy) {
                        try {
                                    stmtInsert.setString(1,deptId);
                                    stmtInsert.setString(2,shortName);
                                    stmtInsert.setString(3,policyCode);
                                    stmtInsert.setString(4,policyHolderId);
                                    stmtInsert.setString(5,getStr(addr));
                                    stmtInsert.setDate(6, new java.sql.Date(System.currentTimeMillis()) );
                                    stmtInsert.setString(7,shortName);
                                    stmtInsert.setString(8,shortName);

                                    stmtInsert.executeUpdate();
                        } catch (Exception e2) { e2.printStackTrace(); } 
                    }
                    ++done; 
                    printed = true;
                } catch (Exception e1) {
                	e1.printStackTrace();
                	updateErrMsg(_stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, e1.toString());
                }

                if (_isPolicy) {
                	//if (printed)
                	//	updateStage(_stmtUpdatePRINTED, hostCode, spoolName, jobNo, subjobNo, 8, printId);
                    updateStage(_stmtUpdatePRINTED, hostCode, spoolName, jobNo, subjobNo, printed? 8:10, printId);
                }
                else {
                	updatePrinted(_stmtUpdatePRINTED, hostCode, spoolName, jobNo, subjobNo, printed? "Y":"N", printId);
                }

                if (_policyCount > 0 && done>=_policyCount) break;
			}//while (result.next()) {

            if (bp==null && _policyBatchId!=null && !_no_ctrl && _total_dataFile>0)  {
                bp = new BatchPrint(false, "POLICY", "1", "3", true,  "VP_TO_YET",
                              _policyBatchId, _total_dataFile, _total_pdf, _no_ctrl);
            }

            if (bp != null) {
                            bp.commit();

                            if (_policyBatchId != null) {
                                updatePolicyBatch(_stmtUpdatePolicyBatch, _policyBatchId, bp.getTotalDataFile(), bp.getTotalPdf(), _no_ctrl?null:"Y");
                            }

                            if (conn != null) conn.commit();    
                            if (connESP != null) try {connESP.commit(); } catch (Exception e2) { e2.printStackTrace(); }     
                        }

    	}
		catch (Exception e) {
            rc = false;
			handleException(e);
            if (bp != null) {
                            bp.rollback();
                            if (conn != null) try {conn.rollback(); } catch (Exception e1) { e1.printStackTrace(); }     
                            if (connESP != null) try {connESP.rollback(); } catch (Exception e2) { e2.printStackTrace(); }  
                        }
		}
		finally {
			if (result != null) { result.close(); result = null; }
            if (_stmtUpdatePolicyBatch != null) { _stmtUpdatePolicyBatch.close(); _stmtUpdatePolicyBatch = null; }
			if (_stmtUpdatePRINTED != null) { _stmtUpdatePRINTED.close(); _stmtUpdatePRINTED = null; }
			if (_stmtUpdateERRMSG != null) { _stmtUpdateERRMSG.close(); _stmtUpdateERRMSG = null; }
			if (stmtSelect != null) { stmtSelect.close(); stmtSelect = null; }
			if (conn != null) { conn.close(); conn = null; }
			if (connESP != null) { connESP.close(); connESP = null; }
		}

        System.out.format("%s jobs=%d, done=%d%n", sdf.format(new java.util.Date()), jobs, done);

        return rc;
    }

    void handleException (Exception e) {
		if (e instanceof SQLException) {
			System.err.println("SQLState=" + ((SQLException)e).getSQLState());
			System.err.println("ErrorCode=" + ((SQLException)e).getErrorCode());
		}
		e.printStackTrace();
	}

    void updatePolicyBatch (PreparedStatement stmt, String batchId, int totalDataFile, int totalPdf, String batchEnd) throws Exception {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.format("%s %s dataFile=%d pdf=%d, batchEnd=%s%n", sdf.format(new java.util.Date()), batchId, totalDataFile, totalPdf, Utils.isEmpty(batchEnd)? "null":"Y");
                stmt.setInt(1, totalDataFile);
                stmt.setInt(2, totalPdf);
                stmt.setString(3, batchEnd);
                stmt.setString(4, batchId);
                stmt.execute();
    }

	void updateStage (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, int stage, String printId) throws Exception {
            try {
                stmt.setInt(1, stage);
                stmt.setString(2, printId);
                stmt.setString(3, hostCode);
                stmt.setString(4, spoolName);
                stmt.setLong(5, jobNo);
                stmt.setInt(6, subjobNo);
                stmt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

	void updatePrinted (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, String printed, String printId) throws Exception {
            try {
                stmt.setString(1, printed);
                stmt.setString(2, printId);
                stmt.setString(3, hostCode);
                stmt.setString(4, spoolName);
                stmt.setLong(5, jobNo);
                stmt.setInt(6, subjobNo);
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
        String dept = "UNB";
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

    String getDateStr (String s) {
    	if (s.length() >= 10 && s.charAt(4)=='-') {
    		return s.substring(0,4) +
				s.substring(5,7) +
				s.substring(8,10);
    	}
    	return "";
    }

    String getTagValue (Node parent, Document dom, String tag) {
    	String v = null==parent? null:Xml.getNodeText(parent, tag);
    	if (Utils.isEmpty(v) && dom != null) {
    		NodeList nl = dom.getElementsByTagName(tag);
    		if (nl != null && nl.getLength() > 0) v = nl.item(0).getTextContent();
    	}
    	return v;
    }

    String forCSV_ (String s) {
    	if (!Utils.isEmpty(s)) {
    		if (s.contains(",")) {
    			return String.format("\"%s\"", s); 
    		}
    		return s;
    	}
    	return "";
    }

    String forCSV (String s) {
        if (!Utils.isEmpty(s)) {
            return String.format("\"%s\"", s.replace("\"", "\"\"")); 
        }
        return "\"\"";
    }

    String getMasterPlanCode (Document dom) {
        String planCode = "";
        NodeList nl_productItem = dom.getElementsByTagName("productItem");
        Node nodeMaster = null; //node for master productItem
        if (nl_productItem != null)
          for (int i=0; nodeMaster==null && i<nl_productItem.getLength(); ++i) {
            Node node = nl_productItem.item(i);
            if ("Y".equals(Xml.getNodeText(node, "masterIndi"))) {
                nodeMaster = node;
                planCode = Xml.getNodeText(node, "planCode");
            }
        }
        return planCode;
    }

    String nodeListInfo (NodeList nl, String tag) {
    	String str = "";
		if (nl != null && nl.getLength() > 0) {
			for (int i=0; i<nl.getLength(); ++i) {
				Node node = nl.item(i);
				String s = Xml.getNodeText(node, tag);
				if (i==0) str = s;
				else str += ","+s;
			}
		}
		return str;
    }

    String getStr (String str) {
        if (Utils.isEmpty(str)) return "null";
        return str;
    }

}
