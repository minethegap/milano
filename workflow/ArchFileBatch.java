//ArchFileBatch.java


import java.io.*;
import java.nio.file.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;


/*
{ctrl_root_path}={cf_root} / {SysA}_cf / RQ / CTRL / archive /
{data_root_path}={cf_root} / {SysA}_cf / RQ / DATA / archive /
{JointName}= {SysA}_archive_{各系統自訂序號}_{yyyyMMddHHmmssSSS}

ctrl file: {ctrl_root_path}\rq_{JointName}.xml
data / pdf files: {data_root_path}\{JointName}\*.xml, *.pdf
*/


public class ArchFileBatch {

	String ctrlPath;
	String dataPath;
	String jointName;

	int seq = 0;

	String ctrlStr;

    String repeatArchive = "Y";

	public ArchFileBatch (String batchNo, String outPath) {
		java.util.Date now = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
        jointName = "sip_archive_" + batchNo + "_" + sdf.format(now);

        ctrlPath = outPath + "\\RQ\\CTRL\\archive";
        dataPath = outPath + "\\RQ\\DATA\\archive\\" + jointName;

        System.out.format("dataPath=%s%n", dataPath);

        new File(ctrlPath).mkdirs();
        new File(dataPath).mkdirs();   

        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        ctrlStr = String.format("<rqHeader><rqTimestamp>%s</rqTimestamp>", sdf.format(now)); 
	}

	public void addOne (String xmlFile, String pdfFile) throws Exception {
        String overWriteOption = "NON";
        
		++seq;
		System.out.format("%d,xml=%s,pdf=%s%n", seq, xmlFile, pdfFile);

		String fn = getFileName(pdfFile);
        String ext = getExtName(fn);
        fn = String.format("%08d_", seq) + fn;
        String fname = getFname(fn);

        String dataDate = "";
        String reportId = "";
        String idxStr = "";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document dom = db.parse(new File(xmlFile));

        NodeList nl = dom.getElementsByTagName("index").item(0).getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if ("reportId".equalsIgnoreCase(n.getNodeName())) {
                    reportId = n.getTextContent();
                }
                else if ("dataDate".equalsIgnoreCase(n.getNodeName())) {
                    dataDate = n.getTextContent();
                }
                else {
                    idxStr += String.format("<indexList><value xmlns:a=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:b=\"http://www.w3.org/2001/XMLSchema\" a:type=\"b:string\">%s</value><key>%s</key></indexList>",
                    		n.getTextContent(), n.getNodeName() );
                }
            }
        }

        nl = dom.getElementsByTagName("isOverriding");
        if (nl != null && nl.getLength() > 0 && "Y".equalsIgnoreCase(nl.item(0).getTextContent()))
                overWriteOption = "ALL";

        String content = String.format("<rqData><content>%s<reportId>%s</reportId><dataDate>%s</dataDate><overWriteOption>%s</overWriteOption><repeatArchive>%s</repeatArchive><contentName>%s</contentName><contentType>%s</contentType></content></rqData>",	    
					idxStr, reportId, dataDate, overWriteOption, repeatArchive, fn, ext);

        str2File(content, dataPath+"\\"+fname+".xml"); //generate data xml

		Files.copy((new File(pdfFile)).toPath(), (new File(dataPath+"\\"+fn)).toPath()); //copy pdf		

		ctrlStr +=  String.format("<rqFiles>%s.xml</rqFiles>", fname); //append to the ctrl file
	}

	public void closeBatch () throws Exception {
		ctrlStr += "</rqHeader>";
		String ctrlFile = ctrlPath+"\\rq_"+jointName+".xml";
		System.out.format("ctrlFile=%s%n", ctrlFile);
		str2File(ctrlStr, ctrlFile);
	}


	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 4) {

        	String outPath = args[0];       
        	String batchNo = args[1];
            String xmlFile = args[2];
            String pdfFile = args[3];
            
            try {   
            	ArchFileBatch afb = new ArchFileBatch(batchNo, outPath);
            	afb.addOne(xmlFile, pdfFile);
            	int i = 4;
            	while (args.length-2 >= i) {
            		xmlFile = args[i++];
            		pdfFile = args[i++];
            		afb.addOne(xmlFile, pdfFile);
            	}
				afb.closeBatch();

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
        }
        else {
            System.out.println("ArchFileBatch <outPath> <batchNo> <xml> <pdf>");
            rc += 15;
        }

        System.exit(rc);
    }

    String getFileName (String s) {
    	int idx = s.lastIndexOf('\\');
    	if (idx >= 0)
    		return s.substring(idx+1);
    	return s;	
    }

    String getExtName (String s) {
    	int idx = s.lastIndexOf('.');
    	if (idx >= 0)
    		return s.substring(idx+1);
    	return "";	
    }

    String getFname (String s) {
    	int idx = s.lastIndexOf('.');
    	if (idx >= 0)
    		return s.substring(0,idx);
    	return s;	
    }

    void str2File (String str, String fileName) throws Exception {
		FileOutputStream fop = new FileOutputStream(fileName);
		fop.write(str.getBytes("UTF-8"));
		fop.flush();
		fop.close();
	}

}
