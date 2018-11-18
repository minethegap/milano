package common;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import common.*;
import java.sql.*;

public class MakeXml {
	static String _outPath;

	public static String getOutPath () {

		return _outPath;
	}

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 3) {
			Connection conn = null;
			PreparedStatement stmt = null;
			PreparedStatement stmtUpdateERRMSG = null;
				
			try {
				String inXml = args[0];
				String outXml = args[1];
				String rptType = args[2];

				String hostCode = null;
				String spoolName = null;
				long jobNo = 0;
				int subjobNo = 1;
				boolean needUpdate = false;
				boolean isPolicy = false;

				if (args.length >= 6) {
					needUpdate = true;
					hostCode = args[3];	
					spoolName = args[4];
					jobNo = Long.parseLong(args[5]);	
					if ("POLICY".equals(rptType)) isPolicy = true;

					conn = (new Sql("@jdbc_ODS")).getConn();
					conn.setAutoCommit(true);
					stmt = conn.prepareStatement(isPolicy? "update t_fmt_policy set ISCHECK=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?":
						"update t_fmt_document set SENDING_METHOD=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?");

					stmtUpdateERRMSG = conn.prepareStatement(isPolicy? "update t_fmt_policy set set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?":
						"update t_fmt_document set ERRMSG=? where HOSTCODE=? and SPOOLNAME=? and JOBNO=? and SUBJOBNO=?");
				}

				_outPath = outXml.substring(0, outXml.lastIndexOf('.'));

				//load the input xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(inXml));

				//process it
				Object converter = Class.forName("rpt."+rptType).newInstance();
				Document domOut = null; 
				Node root = dom.getDocumentElement();
				if ("JOB".equals(root.getNodeName())) {
					domOut = dom;
					Node subjob = root.getFirstChild();
					while (subjob != null) {
						if (subjob.getNodeType()==Node.ELEMENT_NODE) {
							Node data = subjob.getFirstChild();
							while (data != null) {
								if (data.getNodeType()==Node.ELEMENT_NODE) break;
								data = data.getNextSibling();
							}
							Document dom2 = db.newDocument();
							dom2.appendChild(dom2.importNode(data, true));
							if (needUpdate) {
								String attr2 = null;
								if (isPolicy) {
									attr2 = Xml.getNodeText(dom2, "/printRoot/printInfo/printIndi");
									attr2 = "1".equals(attr2) ? "Y":"N";  
								}
								else {
									attr2 = Xml.getNodeText(dom2, "/WSLetterUnit/sendingMethod");
								}
								stmt.setString(1, attr2);
								stmt.setString(2, hostCode);
								stmt.setString(3, spoolName);
								stmt.setLong(4, jobNo);
								stmt.setInt(5, subjobNo);
								stmt.executeUpdate();
								//System.out.format("attr2=%s %s %s %d %d %n", attr2, hostCode, spoolName, jobNo, subjobNo);
							}
							Document domOut2 = dom2;
							try {
								domOut2 = ((rpt.ConvertInterface)converter).convert(dom2);
							} catch (Exception exml) {
								exml.printStackTrace();
								if (needUpdate) {
									updateErrMsg(stmtUpdateERRMSG, hostCode, spoolName, jobNo, subjobNo, exml.toString());
								}
								if (rc == 0x00010000) rc += 10;
							}
							(new MakeXml()).do_imageList(domOut2);
							subjob.removeChild(data);
							subjob.appendChild(domOut.importNode(domOut2.getDocumentElement(), true));
							++subjobNo;
						}
						subjob = subjob.getNextSibling();
					}
				} else { 
					if (needUpdate) {
								String attr2 = null;
								if (isPolicy) {
									attr2 = Xml.getNodeText(dom, "/printRoot/printInfo/printIndi");
									attr2 = "1".equals(attr2) ? "Y":"N";  
								}
								else {
									attr2 = Xml.getNodeText(dom, "/WSLetterUnit/sendingMethod");
								}
								stmt.setString(1, attr2);
								stmt.setString(2, hostCode);
								stmt.setString(3, spoolName);
								stmt.setLong(4, jobNo);
								stmt.setInt(5, subjobNo);
								stmt.executeUpdate();
								//System.out.format("attr2=%s %s %s %d %d %n", attr2, hostCode, spoolName, jobNo, subjobNo);
							}
					domOut = ((rpt.ConvertInterface)converter).convert(dom);
					(new MakeXml()).do_imageList(domOut);
					if (domOut != null) {
						//add /job/doc
						Element jobElement = domOut.createElement("JOB");
						Element subjobElement = domOut.createElement("DOC");
						jobElement.appendChild(subjobElement);
						Element dataElement = domOut.getDocumentElement();
						domOut.replaceChild(jobElement, dataElement); //replace dataElement with jobElement
						subjobElement.appendChild(dataElement); 
					}
				}
				if (domOut != null) {
					//save the output xml
					TransformerFactory trf = TransformerFactory.newInstance();
	 				Transformer tr = trf.newTransformer();
	  				DOMSource ds = new DOMSource(domOut);
	  				StreamResult sr = new StreamResult(new FileOutputStream(new File(outXml)));
	  				tr.transform(ds, sr); 
  				}
  				else {
  					rc += 31;
  				}
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("MakeXml <in_xml> <out_xml> <rpt_type>");
			rc += 15;
		}
		System.exit(rc);
	}

	static void updateErrMsg (PreparedStatement stmt, String hostCode, String spoolName, long jobNo, int subjobNo, String errMsg) throws Exception {
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

	String getImgRoot () throws Exception {
		au.com.bytecode.opencsv.CSVReader reader = new au.com.bytecode.opencsv.CSVReader(new FileReader("c:\\sip\\cfg\\URL.ini"), ',', '"', '|');
    
        String[] nextLine;
      	while ((nextLine = reader.readNext()) != null) {
      		if ("IMG".equals(nextLine[0]))
      			return nextLine[1];
        }

		return "";
	}

	void do_imageList (Document dom) throws Exception {
		if (dom != null) {
			Element ele = dom.getDocumentElement();
			NodeList nl = ele.getElementsByTagName("imageList");
			if (nl != null && nl.getLength() > 0) {
				NodeList nl2 = nl.item(0).getChildNodes();
				if (nl2 != null && nl2.getLength() > 0) {
					String imgRoot = getImgRoot();
					for (int i=0; i<nl2.getLength(); i++) {
						Node n = nl2.item(i);
						if (n.getNodeType()==Node.ELEMENT_NODE && "image".equals(n.getNodeName())) {
							Node n2 = Xml.getNode(n, "archivePath");
							if (n2 != null) {
								String imageName = Xml.getNodeText(n, "imageName");	
								String archivePath = Xml.getNodeText(n, "archivePath");
								
								dom.renameNode(n2, null, "archivePathOrg");

								Xml.addNode(n, "archivePath", imgRoot + archivePath.replaceAll("/", "\\\\") + "\\" + imageName);
							}
						}
					}
				}
			}
		}
	}

}
