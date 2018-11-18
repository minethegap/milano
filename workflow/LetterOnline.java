//LetterOnline.java - for VP_O_LTR_FMT, VP_O_LTR_FMT_CF_SND

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.sql.*;
import common.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;
import sipservertype.*;


class LetterOnline {
	static final javax.xml.namespace.QName SERVICE_NAME = new javax.xml.namespace.QName("urn:sipserverType", "sipserver");

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 2) {
			try {
				String inXml = args[0];
				String outXml = args[1];
				String tblName = args.length>2? args[2]:"T_FMT_TPL_CFG";
				String jobPath = outXml.substring(0, outXml.lastIndexOf('.'));
				rc += (new LetterOnline()).do_it(inXml, outXml, jobPath, tblName);
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			System.out.println("LetterOnline <in_xml> <out_xml> [tbl_name]");
			rc += 15;
		}
		System.exit(rc);
	}

	Connection _conn;
  	SipserverPortType _port;
  	String _sqlStr;

  	LetterOnline () {
		_conn = (new Sql("@jdbc_TGL")).getConn();

		//Sipserver ss = new Sipserver();
		File wsdlFile = new File("c:\\sip\\cfg\\sipserver2.wsdl");
		java.net.URL wsdlURL = null;
		try {
			wsdlURL = wsdlFile.toURI().toURL();
		} catch (java.net.MalformedURLException e) {
            e.printStackTrace();
        }
		Sipserver ss = new Sipserver(wsdlURL, SERVICE_NAME);
		_port = ss.getSipserver();

		//enable MTOM
		BindingProvider bp = (BindingProvider) _port;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
	}

	int do_it (String inXml, String outXml, String jobPath, String tblName)  throws Exception {
		int rc = 0;

		_sqlStr = "SELECT ISJDT FROM " + tblName + 
					" WHERE SHORT_NAME=?";
				
		java.util.ArrayList<SendJobThread> thList = new java.util.ArrayList<SendJobThread>();  

		//load the input xml
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document dom = db.parse(new File(inXml));
		Document domOut = null;
		Element jobElement = null;

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//process each unit
		Element root = dom.getDocumentElement();
		Node unitList = Xml.getNode(root, "unitList");
		NodeList units = unitList.getChildNodes();
		for (int i=0; i<units.getLength(); ++i) {
				Node unit = units.item(i);
				if (unit.getNodeType()==Node.ELEMENT_NODE && "unit".equals(unit.getNodeName())) {
						String rptType = Xml.getNodeText(unit, "shortName");
						String rptNo = Xml.getNodeText(unit, "documentId");
						System.out.format("rptType=%s, rptNo=%s\n", rptType, rptNo);

						//create Node unit as a Document
						Document newDom = db.newDocument();
						Node importedNode = newDom.importNode(unit, true);
						newDom.renameNode(importedNode, null, "WSLetterUnit");
						newDom.appendChild(importedNode);

						//process it
					//	Object converter = Class.forName("rpt."+rptType).newInstance();
					//	Document newDomOut = ((rpt.ConvertInterface)converter).convert(newDom);
						Document newDomOut = newDom;

						if (null==domOut) {
							domOut = db.newDocument();
							jobElement = domOut.createElement("JOB");
							domOut.appendChild(jobElement);
						}
						Element subjobElement = domOut.createElement("DOC");
						jobElement.appendChild(subjobElement);
						Node dataElement = domOut.importNode(newDomOut.getDocumentElement(), true);
						subjobElement.appendChild(dataElement);

						Document domSend = db.newDocument();
						domSend.appendChild(domSend.createElement("JOB"));
						domSend.getDocumentElement().appendChild(domSend.importNode(subjobElement, true));
						String xmlStr = dom2String(domSend);

						String jobName = rptType+"-"+rptNo;
						System.out.format("%s unit%d, %s%n", sdf.format(new java.util.Date()), i, jobName);
						SendJobThread th = new SendJobThread();
						th.setPara(xmlStr, jobName, rptType);
						th.start();
						thList.add(th);
/*
						DataHandler reqFile = new DataHandler(new ByteArrayDataSource(xmlStr.getBytes("UTF-8"), "application/octet-stream"));

		 				Holder<String> jobId = new Holder<String>();
						Holder<String> error = new Holder<String>();
						Holder<DataHandler> resFile = new Holder<DataHandler>();

						port.sendJobAndGetJobFile("local", "LetterOnline", "VP_LTR_FMT", rptType+"-"+rptNo, reqFile, 0, "", null,
							jobId, error, resFile);

						System.out.format("jobId=%s, error=%s\n", jobId.value, error.value);

						if (!Utils.isEmpty(error.value) || Utils.isEmpty(jobId.value))
							rc |= 32;
						else {
							sipjt += String.format("<Job>%s</Job>", jobId.value);
						} 
						*/
				}
		}

		//save the output xml
		TransformerFactory trf = TransformerFactory.newInstance();
	 	Transformer tr = trf.newTransformer();
	  	DOMSource ds = new DOMSource(domOut);
	  	StreamResult sr = new StreamResult(new FileOutputStream(new File(outXml)));
	  	tr.transform(ds, sr);

	  	String sipjt = String.format("<SIPJT><Convert FileName='%s.mta'>", jobPath);

	  	for (SendJobThread t : thList) {
	  		t.join();
	  		String jobId = t.getJobId();
	  		if (Utils.isEmpty(jobId))
				rc |= 32;
			else {
				int idx = jobId.indexOf('*');
				if (-1!=idx) jobId=jobId.substring(idx+1);
				sipjt += String.format("<Job SubjobDesc=\"%s\">%s</Job>", t.getJobName(), jobId);
			} 
	  	}

		sipjt += "<POST>1</POST></Convert></SIPJT>";

		str2File(sipjt, jobPath+".sipjt");

	  	return rc;			

	}

	public String dom2String (Document dom) throws TransformerException {
	    DOMSource ds = new DOMSource(dom);
	    StringWriter writer = new StringWriter();
	    StreamResult sr = new StreamResult(writer);
	    TransformerFactory trf = TransformerFactory.newInstance();
	    Transformer tr = trf.newTransformer();
	    tr.transform(ds, sr);
	    return writer.toString();
	}

	public void str2File (String str, String fileName) throws Exception {
		FileOutputStream fop = new FileOutputStream(fileName);
		fop.write(str.getBytes("UTF-8"));
		fop.flush();
		fop.close();
	}


	class SendJobThread extends Thread {
		String _jobData;
		String _jobName;
		String _rptType;
		
		String _jobId;

  		public void run() {
  			try {
    					DataHandler reqFile = new DataHandler(new ByteArrayDataSource(_jobData.getBytes("UTF-8"), "application/octet-stream"));

		 				Holder<String> jobId = new Holder<String>();
						Holder<String> error = new Holder<String>();
						Holder<DataHandler> resFile = new Holder<DataHandler>();

						_port.sendJobAndGetJobFile("local", "LetterOnline", isJdt()? "VP_LTR_FMT_JDT":"VP_LTR_FMT", _jobName, reqFile, 0, "", null,
							jobId, error, resFile);

						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						System.out.format("%s %s, jobId=%s, error=%s%n", sdf.format(new java.util.Date()), _jobName, jobId.value, error.value);

						if (!Utils.isEmpty(error.value) || Utils.isEmpty(jobId.value))
							;
						else {
							_jobId = jobId.value;
						} 
			} catch (Exception e) {
				e.printStackTrace();
			}			
			
    	}

    	public void setPara (String xmlStr, String jobName, String rptType) {
     		_jobData = xmlStr;
    		_jobName = jobName;
    		_rptType = rptType;
    	}

    	public String getJobId () {
    		return _jobId;
    	}

    	public String getJobName () {
    		return _jobName;
    	}

    	boolean isJdt () throws Exception {
    		boolean rc = false;
    		PreparedStatement stmt = _conn.prepareStatement(_sqlStr);
    		stmt.setString(1, _rptType);
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				rc = "Y".equals(result.getString(1));
			}
			result.close();
			return rc;	
    	}
  	}

}
