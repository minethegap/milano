
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.sql.*;
import org.xml.sax.InputSource;
import common.*;


class PolicyOnline {

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

				Node printJob = dom.getDocumentElement();
				Node content = Xml.getNode(printJob, "content");
				Node printRoot = Xml.getNode(content, "printRoot");

				//create "printRoot" as a DOM
				Document dom2 = db.newDocument();
				dom2.appendChild(dom2.importNode(printRoot, true));
				//Document domOut2 = dom2;
				//process it
				Object converter = Class.forName("rpt."+"POLICY").newInstance();
				Document domOut2 = ((rpt.ConvertInterface)converter).convert(dom2);
				//Document domOut2 = dom2;

				//create the output xml root
				Document domOut = db.newDocument();
				Node jobElement = domOut.createElement("JOB");
				domOut.appendChild(jobElement);

				Node subjobElement = domOut.createElement("DOC");
				jobElement.appendChild(subjobElement);

				//append "printJob"
				printJob.removeChild(content);
				subjobElement.appendChild(domOut.importNode(dom.getDocumentElement(), true));

				//append "printRoot"
				subjobElement.appendChild(domOut.importNode(domOut2.getDocumentElement(), true));

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
			System.out.println("PolicyOnline <in_xml> <out_xml>");
			rc += 15;
		}
		System.exit(rc);
	}

	static public String dom2str (Document dom) {
		return null;
	}

}
