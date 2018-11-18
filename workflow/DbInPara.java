//DbInPara.java
//


import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.net.*;
import javax.xml.namespace.QName;
import javax.jws.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.activation.*;
import sipservertype.*;
import sip.util.*;
import common.*;

class DbInPara {

	public String dbinName;

	public String connName;
	public String sqlSelect;
	public String sqlInsert;
	public String sipURL;
	public String vp;
	public int scanInt = 30; //secs
	public int maxJobs = 0;
	public int merge = 0;
	public int mergeSizeMax = 200; //MB

	public DbInPara (String dbinName) {

			this.dbinName = dbinName;
	
			try {
				
				System.out.println("Working directory=" + System.getProperty("user.dir"));

				//read the dbin.xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File("..\\cfg\\dbin.xml"));

				Node dbinNode = null;
				NodeList nl = dom.getElementsByTagName("dbin");
				for (int i=0; i<nl.getLength(); ++i) {
					Node n = nl.item(i);
					//System.out.println(Xml.getAttrText(n, "name"));
					if (dbinName.equals(Xml.getAttrText(n, "name"))) {
						dbinNode = n;
						break;
					}
				}

				if (dbinNode != null) {
					connName = Xml.getNodeText(dbinNode, "connName");
					sqlSelect = Xml.getNodeText(dbinNode, "sqlSelect");
					sqlInsert = Xml.getNodeText(dbinNode, "sqlInsert");
					sipURL = Xml.getNodeText(dbinNode, "sipURL");
					vp = Xml.getNodeText(dbinNode, "vp");
					String scanIntStr = Xml.getNodeText(dbinNode, "scanInt");
					String maxJobsStr = Xml.getNodeText(dbinNode, "maxJobs");
					String mergeStr = Xml.getNodeText(dbinNode, "merge");
					String mergeSizeMaxStr = Xml.getNodeText(dbinNode, "mergeSizeMax");
					
					if (!Utils.isEmpty(scanIntStr))
						scanInt = Integer.parseInt(scanIntStr);
					if (!Utils.isEmpty(maxJobsStr))
						maxJobs = Integer.parseInt(maxJobsStr);
					if (!Utils.isEmpty(mergeStr))
						merge = Integer.parseInt(mergeStr);
					if (!Utils.isEmpty(mergeSizeMaxStr))
						mergeSizeMax = Integer.parseInt(mergeSizeMaxStr);

					if (Utils.isEmpty(sipURL))
						sipURL = "http://127.0.0.1:5999";
					if (Utils.isEmpty(vp))
						vp = "VP";

					if (Utils.isEmpty(connName))
						System.err.println("connName not defined");
					if (Utils.isEmpty(sqlSelect))
						System.err.println("sqlSelect not defined");

					System.out.format("dbinName=%s, connName=%s,%nsqlSelect=%s,%nsqlInsert=%s,%nsipURL=%s, vp=%s, scanInt=%d, maxJobs=%d, merge=%d, mergeSizeMax=%d%n",
						dbinName, connName, sqlSelect, sqlInsert, sipURL, vp, scanInt, maxJobs, merge, mergeSizeMax);

				}
				else {
					System.err.println(dbinName + " not defined in dbin.xml");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	
	}
	
}
