
package common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.activation.*;
//import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

//import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
//import org.apache.cxf.interceptor.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import com.transglobe.cf.webservice.*;
import com.transglobe.cf.webservice.bean.*;

public class DownloadFile {
	private static final QName SERVICE_NAME = new QName("http://webservice.cf.transglobe.com/", "CFServiceService");

	public static void main (String args[]) {
        int rc = 0x00010000;

        if (args.length >= 2) {

            String guid = args[0];
            String pdfFile = args[1];

		    try {

                URL wsdlURL = CFServiceService.WSDL_LOCATION;
                File wsdlFile = new File("c:\\sip\\cfg\\CFService.xml");
                try {
                    if (wsdlFile.exists()) {
                        wsdlURL = wsdlFile.toURI().toURL();
                    } else {
                        wsdlURL = new URL(args[0]);
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

                
    			DownloadFile df = new DownloadFile();
    			df.download(port, guid, pdfFile);

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
        }
        else {
            System.out.println("DownloadFile <guid> <pdf>");
            rc += 15;
        }

        System.exit(rc);
    }


    public boolean download (CFService port, String guid, String pdfFile) {
    	boolean rc = false;
    	try {
	    	java.lang.String _downloadWithGuid_user = "SIP";
	        java.util.List<java.lang.String> _downloadWithGuid_guidList = new java.util.ArrayList<java.lang.String>();
	        _downloadWithGuid_guidList.add(guid);
	        java.lang.String _downloadWithGuid_merge = "N";
	        java.lang.String _downloadWithGuid_zip = "N";
	        java.lang.String _downloadWithGuid_zipPassword = "";
	        java.lang.String _downloadWithGuid_onlyBookmarkSection = "N";
	        com.transglobe.cf.webservice.DownloadResponse _downloadWithGuid__return = port.downloadWithGuid(_downloadWithGuid_user, _downloadWithGuid_guidList, _downloadWithGuid_merge, _downloadWithGuid_zip, _downloadWithGuid_zipPassword, _downloadWithGuid_onlyBookmarkSection, "SIPFMT");
	        java.util.List<Content> listContent = _downloadWithGuid__return.getContentList();
	        if (listContent != null && listContent.size()>0) {
	        	Content content = listContent.get(0);
	        	if (content != null) {
	        		DataHandler dh = content.getContentData();
	        		dh.writeTo(new java.io.FileOutputStream(pdfFile));
	        		rc = true;
	        	}
	    	}
    	} catch (Exception e) {
                e.printStackTrace();
        }
        return rc;
    }
    
}
