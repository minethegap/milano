//PolicyUrgentPrint.java for VP_O_POL_INT1297

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.util.*;
import java.sql.*;
import common.*;


public class PolicyUrgentPrint {
	static boolean sqlLogEnabled = true;

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 2) {
        	String resultStr = "F";
        	String failReason = "Unknown error";

            String rqXML = args[0];
            String rsXML = args[1];
            String tblName = args.length>2? args[2]:"T_FMT_POLICY";

            try {
            	failReason = "Invalid request XML";
            	//load the rq xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(rqXML));

				String jobId = dom.getElementsByTagName("jobId").item(0).getTextContent();

				String sqlStr = "SELECT f.HOSTCODE,f.SPOOLNAME,f.JOBNO,f.SUBJOBNO,f.STAGE FROM " + tblName + " f" +
					" WHERE f.JOB_ID=?";

				failReason = "Database trouble";
				Connection conn = (new Sql("@jdbc_TGL")).getConn();
				PreparedStatement stmt = sqlLogEnabled? new LoggableStatement(conn, sqlStr) : conn.prepareStatement(sqlStr);
				stmt.setString(1, jobId);
				if (sqlLogEnabled) {
		            	System.out.format("%s%n", ((LoggableStatement)stmt).getQueryString());
		            }
				ResultSet result = stmt.executeQuery();
				if (result.next()) {
					String hostCode = result.getString(1);
					String spoolName = result.getString(2);
					long jobNo = result.getLong(3);
					int subjobNo = result.getInt(4);
					int stage = result.getInt(5);
					result.close();

					if (stage != 8) {//not yet sent
						String sqlStr2 = "UPDATE " + tblName + " SET ISURGENT='Y'" +
							" WHERE HOSTCODE=? AND SPOOLNAME=? AND JOBNO=? AND SUBJOBNO=?";
						PreparedStatement stmt2 = sqlLogEnabled? new LoggableStatement(conn, sqlStr2) : conn.prepareStatement(sqlStr2);
						stmt2.setString(1, hostCode);
						stmt2.setString(2, spoolName);
						stmt2.setLong(3, jobNo);
						stmt2.setInt(4, subjobNo);
						stmt2.execute();
						//conn.commit();
						resultStr = "S";
        				failReason = "";
        			}
        			else {
        				failReason = "Already sent to print vender";
        			}
				}
				else {
					failReason = "No policy found";	
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			String rsXmlStr = String.format("<PolicyUrgentPrintRs><result>%s</result><failReason>%s</failReason></PolicyUrgentPrintRs>",
							resultStr, failReason);
			try {
				FileOutputStream fop = new FileOutputStream(rsXML);
				fop.write(rsXmlStr.getBytes("UTF-8"));
				fop.flush();
				fop.close();
			} catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}

		} else {
            System.out.println("PolicyUrgentPrint <rqXML> <rsXML> [tbl_name]");
            rc += 15;
        }

        System.exit(rc);
    }

}
