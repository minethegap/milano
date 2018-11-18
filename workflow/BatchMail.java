

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import common.Utils;
import common.Xml;

public class BatchMail {
	
	private static final Logger LOGGER = Logger.getLogger(BatchMail.class);

    public class ListFile {
        PrintWriter _pw;
        
        ListFile () throws Exception {
            String path = _batchFolder + "\\CONVERT\\LIST";
            new File(path).mkdirs();
            path += "\\list.txt" ;
            _pw = new PrintWriter(path, "UTF-8"); 
        }

        public void add (String str) {
            _pw.println(str);
        } 

        void close () {
            _pw.close();
        }      
    }

    String _batchName;
    String _batchFolder;
    ListFile _listFile;

    String _loginName;
    String _fromDisplay;
    String _approveName;

    String _batchFolderReal = null;
	
    public BatchMail (String batchName) throws Exception {
        java.util.Date now = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
        _batchName = batchName + "_" + sdf.format(now);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document dom = db.parse(new File("c:\\sip\\cfg\\omicard.xml"));
        String requestFolder = Xml.getAttrText(dom.getDocumentElement(), "Folder");

        _batchFolderReal = requestFolder + "\\" + _batchName;
        _batchFolder = "D:\\toOmicard" + "\\" + _batchName;
        System.out.format("batchName=%s, batchFolder=%s%n", batchName, _batchFolder);
        new File(_batchFolder).mkdirs();
    }

    public void setLoginName (String loginName) { _loginName = loginName;}
    public void setFromDisplay  (String fromDisplay ) { _fromDisplay  = fromDisplay;}
    public void setApproveName (String approveName) { _approveName = approveName;}

    public void setSubject (String subject) throws Exception {
        String path = _batchFolder + "\\SUBJECT\\OLD";
        new File(path).mkdirs();
        path += "\\subject.txt" ;
        str2File(subject, path);
    }

    public void setSubject (String subject, String cpStr) throws Exception {
        String path = _batchFolder + "\\SUBJECT\\OLD";
        new File(path).mkdirs();
        path += "\\subject.txt" ;
        str2File(subject, cpStr, path);
    }

    public void setBody (String body) throws Exception {
        String path = _batchFolder + "\\CONVERT\\HTML";
        new File(path).mkdirs();
        path += "\\body.html" ;
        str2File(body, path);
    }

    public boolean setAttach (String attFileName) {
        return setAttach(attFileName, getFileName(attFileName));    
    }

    public boolean setAttach (String attFileName, String attDispName)  {
    	boolean rc = false;
    	try {
        String path = _batchFolder + "\\ATTACHMENT\\LIST";
        new File(path).mkdirs();

        String dst;
        Files.copy((new File(attFileName)).toPath(), (new File(dst=path+"\\"+attDispName)).toPath()); 
        System.out.format("%s->%s%n", attFileName, dst);
        rc = true;
    	}  catch (IOException e) {
            e.printStackTrace();
        }
        return rc;
    }

    public String getAttachPath () {
    	String path = _batchFolder + "\\ATTACHMENT\\LIST";
        new File(path).mkdirs();
        return path;
    }

    public ListFile getListFile () throws Exception {
        if (null==_listFile) _listFile = new ListFile();
        return _listFile;
    }

    public void commit () throws Exception {
        getListFile().close();

        String launchTxt = _batchFolder + "\\Launch.txt";
        Files.copy((new File("c:\\sip\\cfg\\Launch.txt")).toPath(), (new File(launchTxt)).toPath());

        try (//FileWriter fw = new FileWriter(launchTxt, true);
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(launchTxt, true), "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw))
        {
            pw.println(String.format("JobName=%s", _batchName));
            if (!Utils.isEmpty(_loginName)) pw.println(String.format("UserLoginName=%s", _loginName));
            else pw.println("UserID=1");
            if (!Utils.isEmpty(_fromDisplay)) pw.println(String.format("FromDisplay=%s", _fromDisplay));
            if (!Utils.isEmpty(_approveName)) {
            	LOGGER.debug("Set Approve Name as : " + String.format("ApproveID=0%nApproveName=%s", _approveName));
            	pw.println(String.format("ApproveID=0%nApproveName=%s", _approveName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_batchFolderReal != null) {
      		try {
        		copyDirectory(new File(_batchFolder), new File(_batchFolderReal) );
        		String path = _batchFolderReal + ".ok";
	        	new File(path).createNewFile();
                path = _batchFolder + ".ok";
                new File(path).createNewFile();
        	} catch (IOException e) {
            	e.printStackTrace();
        	}
        } else {
	        String path = _batchFolder + ".ok";
	        new File(path).createNewFile();
    	}
    }

  

    void copy (File sourceLocation, File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        copyDirectory(sourceLocation, targetLocation);
	    } else {
	        copyFile(sourceLocation, targetLocation);
	    }
	}

	void copyDirectory (File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }
	    for (String f : source.list()) {
	        copy(new File(source, f), new File(target, f));
	    }
	}

	void copyFile (File source, File target) throws IOException {        
         Files.copy(source.toPath(),  target.toPath(),  StandardCopyOption.REPLACE_EXISTING);
	}

    public void delete () {
		if (_listFile!=null) _listFile.close();
		Utils.deleteDir(_batchFolder);
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

    String getFileName (String s) {
        int idx = s.lastIndexOf('\\');
        if (idx >= 0)
            return s.substring(idx+1);
        return s;   
    }
}
