//OnlineLetterPostProc.java - for  VP_O_LTR_FMT_CF_SND
//
// copy PDF to NAS and insert one record into T_FMT_DOCUMENT

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import common.*;
import sip.util.*;


class OnlineLetterPostProc {
	
	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 3) {
			try {
				String xml = args[0];
				String pdf = args[1];
				String job = args[2];
				String[] jobs = job.split("-");
				String hostCode = jobs[0];
				String spoolName = jobs[1];
				long jobNo = Long.parseLong(jobs[2], 16);;
				rc += (new OnlineLetterPostProc()).do_it(xml, pdf, hostCode, spoolName, jobNo);
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("OnlineLetterPostProc <xml> <pdf> <job_id>");
			rc += 15;
		}
		System.exit(rc);
	}


	int do_it (String xml, String pdf, String hostCode, String spoolName, long jobNo)  throws Exception {
		int rc = 0;

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//load the input xml
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document dom = db.parse(new File(xml));

		String shortName = Xml.getNodeText(dom, "/JOB/DOC/WSLetterUnit/shortName");
		String documentId = Xml.getNodeText(dom, "/JOB/DOC/WSLetterUnit/documentId");
		long listId = Long.parseLong(documentId);
		String sendingMethodStr = Xml.getNodeText(dom, "/JOB/DOC/WSLetterUnit/sendingMethod");
		int sendingMethod = 0;
		try {
			int sm = Integer.parseInt(sendingMethodStr);
			if (sm>=1 && sm<=3) sendingMethod = sm;
		} catch (Exception e2) {
            e2.printStackTrace();
        }

		System.out.format("%s hostCode=%s, spoolName=%s, jobNo=%d, shortName=%s, listId=%d, sendingMethod=%s%n",  sdf.format(new java.util.Date()), 
	 				hostCode, spoolName, jobNo, shortName, listId, sendingMethodStr);

		Connection conn = null;
		PreparedStatement stmtInsert = null;

		try {
			SysVar sv = new SysVar();
			String pdfPath = sv.get("HA_FOLDER");

			pdfPath = String.format("%s\\%s", pdfPath, hostCode);
			new File(pdfPath).mkdirs();

			pdfPath = String.format("%s\\%s", pdfPath, spoolName);
			new File(pdfPath).mkdirs();

			pdfPath = String.format("%s\\%08x", pdfPath, jobNo);
			new File(pdfPath).mkdirs();

			pdfPath = String.format("%s\\%06x.pdf", pdfPath, 1);

			System.out.format("%s->%s%n", pdf, pdfPath);
			Files.copy((new File(pdf)).toPath(), (new File(pdfPath)).toPath());

			conn = (new Sql("@jdbc_ODS")).getConn();
			stmtInsert = conn.prepareStatement("INSERT INTO T_FMT_DOCUMENT (HOSTCODE,SPOOLNAME,JOBNO,SUBJOBNO,SHORT_NAME,LIST_ID,STAGE,SENDING_METHOD,MERGED) VALUES (?,?,?,1,?,?,1,?,1)");
			stmtInsert.setString(1, hostCode);
			stmtInsert.setString(2, spoolName);
			stmtInsert.setLong(3, jobNo);
			stmtInsert.setString(4, shortName);
			stmtInsert.setLong(5, listId);
			stmtInsert.setInt(6, sendingMethod);
			stmtInsert.executeUpdate();
		} catch (Exception e) {
                e.printStackTrace();
                rc += 97;
        }
        finally {
                if (stmtInsert != null) { stmtInsert.close(); stmtInsert = null; }
                if (conn != null) { conn.close(); conn = null; }
        }

		return rc;
	}

}
