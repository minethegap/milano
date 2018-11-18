
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.sql.*;
import org.xml.sax.InputSource;
import common.*;


class CheckPrint {

	static final String chequeContentTag = "chequePaymentBatchDetail";

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 2) {
			try {
				String inXml = args[0];
				String outXml = args[1];

				//load the input xml
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				Document dom = db.parse(new File(inXml));

				//create the output xml root
				Document domOut = db.newDocument();
				Node jobElement = domOut.createElement("JOB");
				domOut.appendChild(jobElement);

				CheckPrint cp = new CheckPrint();

				//process each check
				Element root = dom.getDocumentElement();
				Node check = Xml.getNode(root, chequeContentTag);
				while (check != null) {
					if (check.getNodeType()==Node.ELEMENT_NODE && chequeContentTag.equals(check.getNodeName())) {

						Node subjobElement = domOut.createElement("DOC");
						jobElement.appendChild(subjobElement);

						String attachmentIndi = Xml.getNodeText(Xml.getNode(check, "chequeBody"), "attachmentIndi");
						System.out.format("attachmentIndi=%s%n", attachmentIndi);
						if (!"3".equals(attachmentIndi)) {

							Node rpt = Xml.getNode(check, "paymentContent");
							String shortName = Xml.getNodeText(rpt, "formNo");
							System.out.format("shortName=%s%n", shortName);
							
							//String listId = Xml.getNodeText(rpt, "docNo");
							//System.out.format("shortName=%s, listId=%s%n", shortName, listId);

							//Document attDom = cp.getRpt(shortName, listId);

							String docStr = Xml.getNodeText(rpt, "doc");
							
							Document attDom = cp.getRpt2(shortName, docStr);
							Node dataElement = domOut.importNode(attDom.getDocumentElement(), true);
							subjobElement.appendChild(dataElement);
						}

						subjobElement.appendChild(domOut.importNode(check, true)); //copy chequePaymentBatchDetail
						Node requestRoot = Xml.addElement(subjobElement, "requestRoot"); //copy root
							NodeList nl = root.getChildNodes();
							for (int i=0; i<nl.getLength(); ++i) {
								Node n = nl.item(i);
								if (n.getNodeType()==Node.ELEMENT_NODE && !chequeContentTag.equals(n.getNodeName())) {
									requestRoot.appendChild(domOut.importNode(n, true));
								}
							}
						
					}
					check = check.getNextSibling();	
				}

				//save the output xml
					TransformerFactory trf = TransformerFactory.newInstance();
	 				Transformer tr = trf.newTransformer();
	  				DOMSource ds = new DOMSource(domOut);
	  				StreamResult sr = new StreamResult(new FileOutputStream(new File(outXml)));
	  				tr.transform(ds, sr); 
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("CheckPrint <in_xml> <out_xml>");
			rc += 15;
		}
		System.exit(rc);
	}

	Connection conn;
	static final String sqlStr = "SELECT t.SHORT_NAME, d.LIST_ID, c.CONTENT " +
"FROM T_TEMPLATE t " +
"JOIN T_DOCUMENT d ON t.TEMPLATE_ID=d.TEMPLATE_ID " +
"JOIN T_DOCUMENT_DATA dd ON d.LIST_ID=dd.LIST_ID " +
"JOIN T_CLOB c ON dd.CLOB_ID=c.CLOB_ID " +
"WHERE d.LIST_ID=?";

	CheckPrint () {
		 conn = (new Sql("@jdbc_TGL")).getConn();
	}

	Document getRpt (String shortName, String listId) {
		Document dom = null;
		try {
			PreparedStatement stmt = conn.prepareStatement(sqlStr);
			stmt.setLong(1, Long.parseLong(listId));
			ResultSet result = stmt.executeQuery();
						if (result.next()) {
							Clob rptData = result.getClob(3);
		 					String rptStr = rptData.getSubString(1, (int)rptData.length());
		 					InputSource is = new InputSource(new StringReader(rptStr));
		 					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder(); 
							dom = db.parse(is);
							//process it
							Object converter = Class.forName("rpt."+shortName).newInstance();
							dom = ((rpt.ConvertInterface)converter).convert(dom);
						}
						else {
							System.err.println("No report found for " + listId);
						}
		}
		catch (Exception e) {
			e.printStackTrace();	
		}

		return dom;			
	}

	Document getRpt2 (String shortName, String rptStr) {
		Document dom = null;
		try {
			InputSource is = new InputSource(new StringReader(rptStr));
		 					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							DocumentBuilder db = dbf.newDocumentBuilder(); 
							dom = db.parse(is);
							//process it
							Object converter = Class.forName("rpt."+shortName).newInstance();
							dom = ((rpt.ConvertInterface)converter).convert(dom);
		}
		catch (Exception e) {
			e.printStackTrace();	
		}					
		return dom;			
	}
}
