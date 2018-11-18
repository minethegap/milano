//PolicyCheckResult.java for VP_O_POL_INT1275

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.util.*;
import java.sql.*;
import common.*;


public class PolicyCheckResult {
	static boolean sqlLogEnabled = true;

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 2) {
        	String returnCode = "0098";
        	String returnMessage = "Unknown error";

            String rqXML = args[0];
            String rsXML = args[1];
            String tblName = args.length>2? args[2]:"T_FMT_POLICY";

            try {
            	returnCode = "0001";
            	returnMessage = "Invalid request XML";
            	//load the rq xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(rqXML));

				String jobId = dom.getElementsByTagName("jobId").item(0).getTextContent();
				String checkResult = dom.getElementsByTagName("checkResult").item(0).getTextContent();

				String sqlStr = "SELECT f.HOSTCODE,f.SPOOLNAME,f.JOBNO,f.SUBJOBNO,f.STAGE FROM " + tblName + " f" +
					" WHERE f.JOB_ID=?";

				returnCode = "0002";
				returnMessage = "Database trouble";
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

					if (stage == 1) {//has PDF
						if ("1".equals(checkResult)) {//ok
							String sqlStr2 = "UPDATE " + tblName + " SET STAGE=4" +
								" WHERE HOSTCODE=? AND SPOOLNAME=? AND JOBNO=? AND SUBJOBNO=?"; //stage==4 -> ready to go (CF,send to print)
							PreparedStatement stmt2 = sqlLogEnabled? new LoggableStatement(conn, sqlStr2) : conn.prepareStatement(sqlStr2);
							stmt2.setString(1, hostCode);
							stmt2.setString(2, spoolName);
							stmt2.setLong(3, jobNo);
							stmt2.setInt(4, subjobNo);
							stmt2.execute();
						}
						else {
							String sqlStr2 = "UPDATE " + tblName + " SET ERRMSG=?" +
								" WHERE HOSTCODE=? AND SPOOLNAME=? AND JOBNO=? AND SUBJOBNO=?";
							PreparedStatement stmt2 = sqlLogEnabled? new LoggableStatement(conn, sqlStr2) : conn.prepareStatement(sqlStr2);
							stmt2.setString(1, "PolicyCheckResult "+checkResult);
							stmt2.setString(2, hostCode);
							stmt2.setString(3, spoolName);
							stmt2.setLong(4, jobNo);
							stmt2.setInt(5, subjobNo);
							stmt2.execute();
						}
						//conn.commit();
						returnCode = "0000";
        				returnMessage = "";
        			}
        			else {
        				returnCode = stage==0? "0004":"0005";
        				returnMessage = stage==0? "No policy PDF":"Cannot set check result now";
        			}
				}
				else {
					returnCode = "0003";
					returnMessage = "No policy found";	
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			String rsXmlStr = String.format("<PolicyCheckResultRs><returnCode>%s</returnCode><returnMessage>%s</returnMessage></PolicyCheckResultRs>",
							returnCode, returnMessage);
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
            System.out.println("PolicyCheckResult <rqXML> <rsXML> [tbl_name]");
            rc += 15;
        }

        System.exit(rc);
    }

}
