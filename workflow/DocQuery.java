//DocQuery.java for VP_O_POL_PDF

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.nio.file.*;
import common.*;


class DocQuery {
	static boolean sqlLogEnabled = true;
	
	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 3) {
			String tblName = args.length>3? args[3]:"T_FMT_POLICY";
			HashMap<String, String> sqlMap = new HashMap<String, String>();
			sqlMap.put("POLICY_CODE", "SELECT f.HOSTCODE,f.SPOOLNAME,f.JOBNO,f.SUBJOBNO FROM " + tblName + " f" +
					" JOIN T_POLICY_PRINT_JOB p ON p.JOB_ID=f.JOB_ID JOIN T_CONTRACT_MASTER c ON c.POLICY_ID=p.POLICY_ID WHERE c.POLICY_CODE=? ORDER BY f.JOB_ID DESC");
			sqlMap.put("JOB_ID", "SELECT f.HOSTCODE,f.SPOOLNAME,f.JOBNO,f.SUBJOBNO FROM " + tblName + " f" +
					" WHERE f.JOB_ID=?");
			try {
				String inXml = args[0];
				String outPdf = args[1];
				String sharedFolder = args[2];

				//load the input xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(inXml));

				Node fstElement = null;
				Element root = dom.getDocumentElement();
				NodeList nl = root.getChildNodes();
				for (int i=0; i<nl.getLength(); ++i) {
					Node n = nl.item(i);
					if (n.getNodeType()==Node.ELEMENT_NODE) {
						fstElement = n;
						break;
					}
				}

				int err = 30;

				do {
					if (null == fstElement) {
						System.err.println("No query criteria");
						break;
					}
					String sqlStr = sqlMap.get(fstElement.getNodeName());
					if (null == sqlStr) {
						System.err.println("Unknown query criteria");
						break;
					}
					System.out.println(sqlStr);
					Connection conn = (new Sql("@jdbc_TGL")).getConn();
					PreparedStatement stmt = sqlLogEnabled? new LoggableStatement(conn, sqlStr) : conn.prepareStatement(sqlStr);
					//stmt.setLong(1, Long.parseLong(fstElement.getTextContent());
					stmt.setString(1, fstElement.getTextContent());
					if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)stmt).getQueryString());
		            }
					ResultSet result = stmt.executeQuery();
					if (result.next()) {
						String hostCode = result.getString(1);
						String spoolName = result.getString(2);
						long jobNo = result.getLong(3);
						int subjobNo = result.getInt(4);
						String srcPdf = String.format("%s\\%s\\%s\\%08x\\%06x.pdf", sharedFolder, hostCode, spoolName, jobNo, subjobNo);
						if (new File(srcPdf).isFile())
							;
						else
							srcPdf = String.format("%s\\%s\\%s\\%08x_%05d.pdf", sharedFolder, hostCode, spoolName, jobNo, subjobNo);
						System.out.println("srcPdf="+srcPdf);
						Files.copy((new File(srcPdf)).toPath(), (new File(outPdf)).toPath());

						err = 0;
					}
					else {
						System.err.println("No policy found");
					}

				} while (false);

				rc += err;
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("DocQuery <in_xml> <out_pdf> <shared_folder> [tbl_name]");
			rc += 15;
		}
		System.exit(rc);
	}
}
