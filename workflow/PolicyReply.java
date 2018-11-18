

import java.io.*;
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

import com.tgl.common.ws.*;

import java.sql.*;
import sip.util.*;
import common.*;


public class PolicyReply {
    static boolean sqlLogEnabled = true;

	private static final QName SERVICE_NAME = new QName("http://tgl.com/common/ws/", "CommonWSService");
    private static final String EBAO_WSDL = "c:\\sip\\cfg\\eBao.xml";

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length==1 && "ALL".equals(args[0])) {
            rc += (new PolicyReply()).replyAll();
        } else
        if (args.length >= 3) {

            String jobId = args[0];
            String jobPath = args[1];
            String ebaoJobId = args[2];
            boolean isError = false;

            if (args.length > 3 && "ERROR".equals(args[3])) isError = true;

            try {

                URL wsdlURL = CommonWSService.WSDL_LOCATION;
                File wsdlFile = new File(EBAO_WSDL);
                try {
                    if (wsdlFile.exists()) {
                        wsdlURL = wsdlFile.toURI().toURL();
                    } 
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
          
                CommonWSService ss = new CommonWSService(wsdlURL, SERVICE_NAME);
        		CommonWS port = ss.getCommonWSServicePort();  

        com.tgl.common.ws.RqHeader rqHeader = new com.tgl.common.ws.RqHeader();
        rqHeader.setRqUID(jobId);
        com.tgl.common.ws.StandardRequest standardRequest = new com.tgl.common.ws.StandardRequest();
        standardRequest.setRqHeader(rqHeader);
        standardRequest.setServiceName("wSPolicyPrintReplyService");
        standardRequest.setSystemCode("SIP");
        String errMsg = isError? getErrMsg(jobPath):"";
//        String rqBody = String.format("<PolicyPrintReplyRq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"PolicyPrintReplyRq.xsd\"> <jobId>%s</jobId> <printCompIndi>6</printCompIndi> <statusChangeDate>%s</statusChangeDate> <errorMessage>%s</errorMessage> </PolicyPrintReplyRq>",
        String rqBody = String.format("<PolicyPrintReplyRq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"PolicyPrintReplyRq.xsd\"><PolicyPrintReplyList><PolicyPrintReply> <jobId>%s</jobId> <printCompIndi>%d</printCompIndi> <statusChangeDate>%s</statusChangeDate> <errorMessage>%s</errorMessage> </PolicyPrintReply></PolicyPrintReplyList></PolicyPrintReplyRq>",
        	 ebaoJobId, common.Utils.isEmpty(errMsg) ? 6:4, (new java.text.SimpleDateFormat("yyyyMMdd")).format(new java.util.Date()),  errMsg );
        standardRequest.setRqBody(rqBody);
        com.tgl.common.ws.StandardResponse standardResponse = port.exchange(standardRequest);
                RsHeader rsHeader = standardResponse.getRsHeader();
                    String returnCode = rsHeader.getReturnCode(); 
                    String returnMsg = rsHeader.getReturnMsg(); 
                    System.out.format("%s %s%n", returnCode, returnMsg);

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
        }
        else {
            System.out.println("PolicyReply <jobId> <jobPath> <ebaoJobId> [ERROR]");
            rc += 15;
        }

        System.exit(rc);
    }

    static String getErrMsg (String jobPath) {
    	String str = "";
    	try {
    		String errMsg = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(jobPath+".err")));
    		str = "<![CDATA["+errMsg+"]]>";
    	} catch (Exception e) {
                
        }
    	return str;
    }


    int replyAll () {
        int rc = 97;

        String connName = "@jdbc_ODS";
        String sqlStrSelect = "select HOSTCODE,SPOOLNAME,JOBNO,SUBJOBNO,JOB_ID,STAGE,ERRMSG from T_FMT_POLICY where STAGE=11 or STAGE=13 order by JOB_ID";
        String sqlStrUpdate = "update T_FMT_POLICY set STAGE=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.format("%s %s%n%s%n%s%n", sdf.format(new java.util.Date()), connName, sqlStrSelect, sqlStrUpdate );

        Connection conn = null;
        PreparedStatement stmtSelect = null;
        ResultSet result = null;
        PreparedStatement stmtUpdate = null;

        while (true) {

            try {
                Thread.sleep(3*60*1000); //do it per 3 min

                URL wsdlURL = CommonWSService.WSDL_LOCATION;
                File wsdlFile = new File(EBAO_WSDL);
                try {
                    if (wsdlFile.exists()) {
                        wsdlURL = wsdlFile.toURI().toURL();
                    } 
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
          
                CommonWSService ss = new CommonWSService(wsdlURL, SERVICE_NAME);
                CommonWS port = ss.getCommonWSServicePort(); 


                conn = (new Sql(connName)).getConn();
                conn.setAutoCommit(true);
                stmtUpdate = sqlLogEnabled? new LoggableStatement(conn, sqlStrUpdate) : conn.prepareStatement(sqlStrUpdate);
                stmtSelect = sqlLogEnabled? new LoggableStatement(conn, sqlStrSelect) : conn.prepareStatement(sqlStrSelect);
                result = stmtSelect.executeQuery();
                while (result.next()) {
                    String hostCode = result.getString("HOSTCODE");
                    String spoolName = result.getString("SPOOLNAME");
                    long jobNo = result.getLong("JOBNO");
                    int subjobNo = result.getInt("SUBJOBNO");
                    String ebaoJobId = String.format("%d", result.getLong("JOB_ID"));
                    String jobId = String.format("%s-%s-%08x-%06x", hostCode, spoolName, jobNo, subjobNo);
                    int stage = result.getInt("STAGE");
                    boolean isError = stage==13? true:false;
                    String errMsg = "";
                    if (isError) {
                        errMsg = result.getString("ERRMSG");    
                        if (common.Utils.isEmpty(errMsg)) errMsg = "Unknown Error";
                    }
                    
                    System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, subjobNo=%d, policy JOB_ID=%s%n",  sdf.format(new java.util.Date()), 
                        hostCode, spoolName, jobNo, subjobNo, ebaoJobId);

                    
                    com.tgl.common.ws.RqHeader rqHeader = new com.tgl.common.ws.RqHeader();
                    rqHeader.setRqUID(jobId);
                    com.tgl.common.ws.StandardRequest standardRequest = new com.tgl.common.ws.StandardRequest();
                    standardRequest.setRqHeader(rqHeader);
                    standardRequest.setServiceName("wSPolicyPrintReplyService");
                    standardRequest.setSystemCode("SIP");
                    

                    String rqBody = String.format("<PolicyPrintReplyRq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"PolicyPrintReplyRq.xsd\"><PolicyPrintReplyList><PolicyPrintReply> <jobId>%s</jobId> <printCompIndi>%d</printCompIndi> <statusChangeDate>%s</statusChangeDate> <errorMessage>%s</errorMessage> </PolicyPrintReply></PolicyPrintReplyList></PolicyPrintReplyRq>",
                            ebaoJobId, common.Utils.isEmpty(errMsg) ? 6:4, (new java.text.SimpleDateFormat("yyyyMMdd")).format(new java.util.Date()),  errMsg );
                    standardRequest.setRqBody(rqBody); 

                    com.tgl.common.ws.StandardResponse standardResponse = port.exchange(standardRequest);
                    RsHeader rsHeader = standardResponse.getRsHeader();
                    String returnCode = rsHeader.getReturnCode(); 
                    String returnMsg = rsHeader.getReturnMsg(); 
                    System.out.format("%s %s%n", returnCode, returnMsg);


                    if ("4011".equals(returnCode)) {//eBao say "call me later"

                    } else {
                    stmtUpdate.setInt(1,isError? 14:"0000".equals(returnCode)?1:12);
                    stmtUpdate.setString(2, hostCode);
                    stmtUpdate.setString(3, spoolName);
                    stmtUpdate.setLong(4, jobNo);
                    stmtUpdate.setInt(5, subjobNo);
                    stmtUpdate.execute();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }  
            finally {
                if (result != null) { try{result.close();}catch (Exception e){} result = null; }
                if (stmtUpdate != null) { try{stmtUpdate.close();}catch (Exception e){} stmtUpdate = null; }
                if (stmtSelect != null) { try{stmtSelect.close();}catch (Exception e){} stmtSelect = null; }
                if (conn != null) { try{conn.close();}catch (Exception e){} conn = null; }
            }
   
        }

        //return rc;
    }

}
