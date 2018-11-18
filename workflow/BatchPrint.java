

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import common.*;
import java.util.ArrayList;
import java.sql.*;

public class BatchPrint {

    final static private String _pwdHead = "70817744";
    String _fileType = "7z";
    boolean _outPages = true;

/*    final static private String _infoHead = "batchID,seqNo,dataDate,regNumber,mailSubject," +
        "policyNo,priContractNo,policyOwnerID,policyOwnerName,insuredID,insuredName,salesNo,salesName,vipIndi,noticeOfInterestIndi,reproductionIndi,reprintIndi,urgentIndi," +
        "senderName,senderAddressZipCode,senderAddress," +
        "receiverID,receiverName,receiverAddressZipCode,receiverAddress," +
        "channelTypeCode,channelCode,channelName,channelReceiverName,collectorOrgCode,collectorOrgName,collectorOrgAddress,collectorName,brbdCompanyCode,brbdCompanyName,brbdBranchCode,brbdBranchName," + 
        "postWay,filePath\r\n"; */

    final static private String _infoHead = "\"batchID\",\"seqNo\",\"dataDate\",\"regNumber\",\"mailSubject\"," +
        "\"policyNo\",\"priContractNo\",\"policyOwnerID\",\"policyOwnerName\",\"insuredID\",\"insuredName\",\"salesNo\",\"salesName\",\"vipIndi\",\"noticeOfInterestIndi\",\"reproductionIndi\",\"reprintIndi\",\"urgentIndi\",\"pendingDelivery\"," +
        "\"senderName\",\"senderAddressZipCode\",\"senderAddress\",\"senderPhone\"," +
        "\"receiverID\",\"receiverName\",\"receiverAddressZipCode\",\"receiverAddress\"," +
        "\"channelTypeCode\",\"channelCode\",\"channelName\",\"channelReceiverName\",\"collectorOrgCode\",\"collectorOrgName\",\"collectorOrgAddress\",\"collectorName\",\"brbdCompanyCode\",\"brbdCompanyName\",\"brbdBranchCode\",\"brbdBranchName\"," + 
//        "\"postWay\",\"filePath\"\r\n"; 
        "\"postWay\",\"currency\",\"isOIU\",\"ackDueDate\",\"pages\",\"filePath\"\r\n";    

    String _reportId; 
    String _duplexCode;
    String _deliverMethod;
    boolean _isPolicy;
    boolean _isPos190;
    String _vpName;

    String _batchDate;
    String _batchId; 
    String _batchFolder;

    String _sender = null;

    boolean _needEra = false;

    StringBuilder _sbInfo;
    String _zipFolder;
    int _seq;   //current seq of data/zip file 
    int _pdfs;  //# of pdf in this zip
    int _total; //total pdfs of this batch

    int _prev_seq;
    int _prev_total;
    boolean _no_ctrl;

    ArrayList<Process> _alZip;

    Connection _connESP = null;
    PreparedStatement _stmtData = null;
    PreparedStatement _stmtBatch = null;

    static boolean fstInst = true;

    public int getTotalDataFile () {
        return _seq;
    }

    public int getTotalPdf () {
        return _total;
    }

    public void setNeedEra (boolean needEra) {
        _needEra = needEra;
    }

    public void setSender (String sender) {
        _sender = sender;
    }

    public BatchPrint (boolean isAdhoc, String shortName, String duplexCode, String deliverMethod, boolean isPolicy, String vpName) throws Exception {
        init(isAdhoc, shortName, duplexCode, deliverMethod, isPolicy, vpName, 
        	null, 0, 0, false);
    }

    public BatchPrint (boolean isAdhoc, String shortName, String duplexCode, String deliverMethod, boolean isPolicy, String vpName,
    	String batchId, int total_dataFile, int total_pdf, boolean no_ctrlFile) throws Exception {
        init(isAdhoc, shortName, duplexCode, deliverMethod, isPolicy, vpName, 
        	batchId, total_dataFile, total_pdf, no_ctrlFile);
    }

    private void init (boolean isAdhoc, String shortName, String duplexCode, String deliverMethod, boolean isPolicy, String vpName,
    	String batchId, int total_dataFile, int total_pdf, boolean no_ctrlFile) throws Exception {
    	_reportId = shortName;
        _duplexCode = duplexCode;
        _deliverMethod = deliverMethod;
        _isPolicy =  isPolicy;
        _isPos190 = "FMT_POS_0190".equals(shortName);
        _vpName = vpName;

        if (Utils.isEmpty(batchId)) {

        java.util.Date curDt = new java.util.Date();
        String batchTimeStr = (new java.text.SimpleDateFormat("yyyyMMddHHmmss")).format(curDt);
        _batchDate = (new java.text.SimpleDateFormat("yyyyMMdd")).format(curDt);

        _batchId = (isAdhoc? "SIPA-PM-":"SIP-PM-") + shortName + "-" + batchTimeStr;

    	} else {
    		int idx = batchId.lastIndexOf('-') + 1;
    		_batchDate = batchId.substring(idx,idx+8);
    		_batchId = batchId;
    	}
     
        sip.util.SysVar sv = new sip.util.SysVar();
        _batchFolder = sv.get("TMP_FOLDER") + "\\" + _batchId;

        System.out.format("batchId=%s, batchFolder=%s%n", _batchId, _batchFolder);

        new File(_batchFolder).mkdirs();

        _sbInfo = null;
        _seq = total_dataFile;
        _pdfs = 0;
        _total = total_pdf;


        _connESP = (new Sql("@jdbc_ESP")).getConn();
        _connESP.setAutoCommit(false);

        String strData, strBatch;

        _stmtData = _connESP.prepareStatement(strData="INSERT INTO esp.POSTMAIL_DATA_REQ (REQ_SYS_ID,BATCH_NO,SEQ_NO,POSTMAIL_CATEGORY,OUTSOURCING_POST," + 
            "MAIL_SEND_MODE,RECEIVER_ID_NO,RECEIVER_TYPE,RECEIVER_NAME,RECEIVER_ZIPCODE,RECEIVER_ADDRESS,REQ_SYS_REF_ID,CREATE_DATA_DATETIME,POLICY_NUM,DOC_TYPE,DOC_NO)" + 
            "VALUES ('SIP',?,?,?,'Y', ?,?,'01',?,?,?,?,?,?,'01',?)");

        //_stmtBatch = _connESP.prepareStatement(strBtach="INSERT INTO esp.POSTMAIL_DATA_BATCH_REQ (REQ_SYS_ID,BATCH_NO,BATCH_COUNT,IMPORT_ONLY_INDI,CREATE_DATETIME)"+
        //    " VALUES ('SIP',?,?,'Y',sysdate)");
        _stmtBatch = _connESP.prepareStatement(strBatch="INSERT INTO esp.POSTMAIL_DATA_BATCH_REQ (REQ_SYS_ID,BATCH_NO,BATCH_COUNT,CREATE_DATETIME)"+
            " VALUES ('SIP',?,?,sysdate)");

        if (fstInst) {
            fstInst = false;
            System.out.println(strData);
            System.out.println(strBatch);
        }

        _prev_seq = total_dataFile;
    	_prev_total = total_pdf;
    	_no_ctrl = no_ctrlFile;
    }


    public String add (String infoStr, String srcPdf, String pdfName,
        String reportId, String sendMode, String receiverId, String receiverName, String receiverZip, String receiverAddr,
        String uid, String dataDateStr, String policyCode) throws Exception {
        if (0==_pdfs) {
            ++_seq;
            _zipFolder = String.format("%s\\rq_%s-%d", _batchFolder, _batchId, _seq);
            new File(_zipFolder).mkdirs();
            _sbInfo = new StringBuilder(_infoHead);
        }

        String dstPdf;

        Files.copy((new File(srcPdf)).toPath(), (new File(dstPdf=_zipFolder+"\\"+pdfName)).toPath()); 

        ++_total;

        //batchID,seqNo
        _sbInfo.append(String.format("%s,%s", forCSV(_batchId), forCSV(Integer.toString(_total))));
        _sbInfo.append(infoStr);
        if (_outPages) {
            _sbInfo.append(String.format(",%s,%s%n", forCSV(Integer.toString(getPages(dstPdf))), forCSV(pdfName)));
        } else
        //filePath
        _sbInfo.append(String.format(",%s%n", forCSV(pdfName)));

        ++_pdfs;
        if (_pdfs == (_isPolicy? 50:_isPos190?500:10000)) {
            commitZip();
        }

        try {
		String pn;
            _stmtData.setString(1,_batchId);
            _stmtData.setInt(2,_total);
            _stmtData.setString(3,reportId);
            _stmtData.setString(4,getStr(sendMode));
            _stmtData.setString(5,getStr(receiverId));
            _stmtData.setString(6,getStr(receiverName));
            _stmtData.setString(7,getStr(receiverZip));
            _stmtData.setString(8,getStr(receiverAddr));
            _stmtData.setString(9,uid);
            _stmtData.setDate(10,toDate(dataDateStr));
            _stmtData.setString(11,pn=getStr(policyCode));
            _stmtData.setString(12,pn);

            _stmtData.executeUpdate(); 
        } catch (Exception e1) {
                e1.printStackTrace();
            }

        return String.format("%s,%d", _batchId, _total);
    }

    
    void commitZip () throws Exception {       
        if (_pdfs > 0) {
            String zipFile = String.format("rq_%s-%d.dat", _batchId, _seq);
            System.out.format("zip=%s, pdfs=%d%n", zipFile, _pdfs);
            str2File(_sbInfo.toString(), _zipFolder+"\\"+zipFile);

            zipFile = String.format("%s\\rq_%s-%d.%s", _batchFolder, _batchId, _seq, _fileType);

            if (_seq==1+_prev_seq) _alZip = new ArrayList<Process>();
            
            ProcessBuilder pb = new ProcessBuilder("7za", "a", "-t"+_fileType, "-p"+_pwdHead+_batchDate, zipFile, _zipFolder+"\\*");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            _alZip.add(pb.start());

            //pb = new ProcessBuilder("sendjob", "/pri:50", "local", _vpName, zipFile);
            //run_proc(pb);

            _pdfs = 0;
        }
    }

    public void rollback () {
        if (_connESP != null) {
            try {
                _connESP.rollback();
                _connESP.close();
                _connESP = null;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    void sendEUDC () {
        String eudcFile = String.format("%s\\rq_%s.eudc.%s", _batchFolder, _batchId, _fileType);
        
        try {
            ProcessBuilder pb = new ProcessBuilder("7za", "a", "-t"+_fileType, eudcFile, "C:\\Gaiji\\User\\Dynafont\\細明體.tte", "C:\\Gaiji\\User\\Dynafont\\標楷體.tte");
            run_proc(pb);  

            pb = new ProcessBuilder("sendjob",  "/pri:60", "local", _vpName, eudcFile);
            run_proc(pb); 
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void commit () throws Exception {  
        commitZip();  

        if (!_no_ctrl)
        	if (_isPolicy) sendEUDC();

        ProcessBuilder pb; 


        if (_alZip != null) {

            int seq = _prev_seq;
            for (Process p:_alZip) {
                String zipFile = String.format("%s\\rq_%s-%d.%s", _batchFolder, _batchId, ++seq, _fileType);
            	p.waitFor();
                if (0!=p.exitValue()) {
                    throw new Exception("7z error " + zipFile);
                }
            	pb = Utils.isEmpty(_sender)? new ProcessBuilder("sendjob", "/pri:50", "local", _vpName, zipFile) : new ProcessBuilder("sendjob", "/pri:50", "local", _vpName, zipFile, zipFile.substring(zipFile.lastIndexOf('\\')+1), _sender);
                if (0x00030000!=run_proc(pb)) {
                    throw new Exception("sendjob error " + zipFile);
                }
            }   

        }

        if (!_no_ctrl) {
        
        String ctlStr = String.format(
            "batchID:%s%n" +
            "batchDate:%s%n" +
            "totalCount:%d%n" +
            "reportID:%s%n" +
            "printPageCode:%s%n" +
            "deliverMethodCode:%s%n" +
            "dataFileList:%n",
            _batchId, _batchDate, _total, _reportId, _duplexCode, _deliverMethod);

        for (int i=1; i<=_seq; ++i)
            ctlStr += String.format("rq_%s-%d.%s%n", _batchId, i, _fileType);

        String ctlFile = String.format("%s\\rq_%s.ctl", _batchFolder, _batchId);
        System.out.format("ctl=%s, total=%d%n", ctlFile, _total);    
        str2File(ctlStr, ctlFile);

        pb = Utils.isEmpty(_sender)? new ProcessBuilder("sendjob", "local", _vpName, ctlFile) : new ProcessBuilder("sendjob", "local", _vpName, ctlFile, ctlFile.substring(ctlFile.lastIndexOf('\\')+1), _sender);
        if (0x00030000!=run_proc(pb)) {
                throw new Exception("sendjob error " + ctlFile);    
            }

        if (_needEra) {
            String era = String.format("rq_%s.era", _batchId);
            pb = Utils.isEmpty(_sender)? new ProcessBuilder("sendjob", "local", _vpName, "c:\\sip\\cfg\\empty.spl", era) : new ProcessBuilder("sendjob", "local", _vpName, "c:\\sip\\cfg\\empty.spl", era, _sender);
            if (0x00030000!=run_proc(pb)) {
                throw new Exception("sendjob error " + era);    
            }
        }    

    	}

        if (_connESP != null) {
            try {
              if (!_no_ctrl) {	
                _stmtBatch.setString(1,_batchId);
                _stmtBatch.setInt(2,_total);
                _stmtBatch.executeUpdate(); 
              }

                _connESP.commit();
                _connESP.close();
                _connESP = null;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }    
    }

    void str2File (String str, String fileName) throws Exception {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] ba = str.getBytes("UTF-8");
        fos.write(ba);

        fos.close();
    }

    void str2File (String str, String cpStr, String fileName) throws Exception {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] ba = str.getBytes(cpStr);
        fos.write(ba);

        fos.close();
    }

    int run_proc (ProcessBuilder pb) throws Exception {
        int rc = -1;
        pb.redirectErrorStream(true);
        Process p = pb.start();
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String lineRead;
        while ((lineRead = br.readLine()) != null) {
            System.out.println(lineRead);
        }
        p.waitFor();
        rc = p.exitValue();
        return rc;
    }

    String forCSV (String s) {
        if (!Utils.isEmpty(s)) {
            return String.format("\"%s\"", s.replace("\"", "\"\"")); 
        }
        return "\"\"";
    }

    int getPages (String pdfName) {
        int pg = 0;
        try {
            com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(
                new com.itextpdf.text.pdf.RandomAccessFileOrArray(pdfName), null);
            pg = reader.getNumberOfPages();
            reader.close();
        } catch (Exception e1) {
                    e1.printStackTrace();
                }
        return pg;
    }

    String getStr (String str) {
        if (Utils.isEmpty(str)) return "null";
        return str;
    }


    java.sql.Date toDate (String str) {
        java.util.Date uDate = null;
        if (!Utils.isEmpty(str) && str.length()==8) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            try {
                uDate = sdf.parse(str);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        
        if (uDate==null)
            uDate = new java.util.Date();
        
        return new java.sql.Date(uDate.getTime());
    }
}
