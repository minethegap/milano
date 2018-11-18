
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.sql.*;
import org.xml.sax.InputSource;
import common.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;


class PolicyReprint {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

	public static void main (String[] args) {
		int rc = 0x00010000;
		if (args.length >= 2) {
			try {
				//String inXml = args[0];
                String policyCode = args[0];
				String outPdf = args[1];

				URL wsdlURL = CFServiceService.WSDL_LOCATION;
                File wsdlFile = new File("c:\\sip\\cfg\\CFService.xml");
                try {
                    if (wsdlFile.exists()) {
                        wsdlURL = wsdlFile.toURI().toURL();
                    } else {
                        //wsdlURL = new URL(args[0]);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
          
                CFServiceService ss = new CFServiceService(wsdlURL, SERVICE_NAME);
                CFService port = ss.getCFServicePort();  

                //enable MTOM
    			BindingProvider bp = (BindingProvider) port;
    			SOAPBinding binding = (SOAPBinding) bp.getBinding();
    			binding.setMTOMEnabled(true);

    			rc += (new PolicyReprint()).reprint(port, policyCode, outPdf);
				
			}
			catch (Exception e) {
				e.printStackTrace();
				rc += 98;
			}
		}
		else {
			//System.out.println("PolicyReprint <in_xml> <out_pdf>");
            System.out.println("PolicyReprint <policyCode> <out_pdf>");
			rc += 15;
		}
		System.exit(rc);
	}

    int reprint (CFService port, String policyCode, String outPdf) throws Exception {
        int rc = 30;

            java.lang.String _user = "SIP";

            java.util.List<java.lang.String> _queryWithIndex_reportIdList = new java.util.ArrayList<java.lang.String>();
            _queryWithIndex_reportIdList.add("#POLICY#");

            com.transglobe.cf.webservice.SqueezeDate _queryWithIndex_squeezeDate = new com.transglobe.cf.webservice.SqueezeDate();
            //java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
            //javax.xml.datatype.XMLGregorianCalendar endDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            //_queryWithIndex_squeezeDate.setEndDate(endDate);

            java.lang.String _queryWithIndex_onlyLastVersion = "Y";

            com.transglobe.cf.webservice.bean.request.Clause _queryWithIndex_queryIndex = new com.transglobe.cf.webservice.bean.request.Clause();
            _queryWithIndex_queryIndex.and("printUnit", "UNB", com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);
            _queryWithIndex_queryIndex.and("policyCode", policyCode, com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);

            com.transglobe.cf.webservice.Sort _queryWithIndex_sort = new com.transglobe.cf.webservice.Sort();
            _queryWithIndex_sort.setIndexName("printDate");
            _queryWithIndex_sort.setOperation("DESC");

            java.lang.Integer _queryWithIndex_page = 1;
            java.lang.Integer _queryWithIndex_pageSize = -1;

            com.transglobe.cf.webservice.QueryResponse _queryWithIndex__return = port.queryWithIndex(_user, _queryWithIndex_reportIdList,
                _queryWithIndex_squeezeDate, _queryWithIndex_onlyLastVersion, _queryWithIndex_queryIndex, _queryWithIndex_sort,
                _queryWithIndex_page, _queryWithIndex_pageSize, "Y", "SIPFMT");

            if (_queryWithIndex__return != null) {
                java.util.List<QueryResult> qrl =  _queryWithIndex__return.getQueryResultList();
                for (QueryResult qr:qrl) {
                    java.util.List<Content> cl = qr.getContentList();
                    for (Content c:cl) {
                        String guid = c.getGuid();
                        System.out.format("%s->%s%n", guid, outPdf);
                        boolean b = (new DownloadFile()).download(port, guid, outPdf);
                        if (b) rc = 0;
                        if (0==rc) break;
                    }
                    if (0==rc) break;
                }
            }
     
        return rc;
    }

/*
	int reprintOld (CFService port, String inXml, String outPdf) throws Exception {
		int rc = 30;

		//load the input xml
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document dom = db.parse(new File(inXml));

		NodeList nl = dom.getElementsByTagName("index");	
		//for (Node n:nl) {
		if (nl != null) {
			Node n = nl.item(0);
			String policyCode = Xml.getNodeText(n, "policyCode");

			java.lang.String _user = "SIP";

            java.util.List<java.lang.String> _queryWithIndex_reportIdList = new java.util.ArrayList<java.lang.String>();
            _queryWithIndex_reportIdList.add("#POLICY#");

            com.transglobe.cf.webservice.SqueezeDate _queryWithIndex_squeezeDate = new com.transglobe.cf.webservice.SqueezeDate();
            //java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
            //javax.xml.datatype.XMLGregorianCalendar endDate = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            //_queryWithIndex_squeezeDate.setEndDate(endDate);

            java.lang.String _queryWithIndex_onlyLastVersion = "Y";

            com.transglobe.cf.webservice.bean.request.Clause _queryWithIndex_queryIndex = new com.transglobe.cf.webservice.bean.request.Clause();
            _queryWithIndex_queryIndex.and("printUnit", "UNB", com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);
            _queryWithIndex_queryIndex.and("policyCode", policyCode, com.transglobe.cf.webservice.bean.request.ComparisonOperator.EQ);

            com.transglobe.cf.webservice.Sort _queryWithIndex_sort = new com.transglobe.cf.webservice.Sort();
            _queryWithIndex_sort.setIndexName("printDate");
            _queryWithIndex_sort.setOperation("DESC");

            java.lang.Integer _queryWithIndex_page = 1;
            java.lang.Integer _queryWithIndex_pageSize = -1;

            com.transglobe.cf.webservice.QueryResponse _queryWithIndex__return = port.queryWithIndex(_user, _queryWithIndex_reportIdList,
                _queryWithIndex_squeezeDate, _queryWithIndex_onlyLastVersion, _queryWithIndex_queryIndex, _queryWithIndex_sort,
                _queryWithIndex_page, _queryWithIndex_pageSize);

            if (_queryWithIndex__return != null) {
                java.util.List<QueryResult> qrl =  _queryWithIndex__return.getQueryResultList();
                for (QueryResult qr:qrl) {
                    java.util.List<Content> cl = qr.getContentList();
                    for (Content c:cl) {
                        String guid = c.getGuid();
                        System.out.format("%s->%s%n", guid, outPdf);
                        boolean b = (new DownloadFile()).download(port, guid, outPdf);
                        rc = 0;
                        if (0==rc) break;
                    }
                    if (0==rc) break;
                }
            }
		}

		return rc;
	}
*/
}
