
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

public class DeleteFile {
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

                
    			DeleteFile df = new DeleteFile();
    			df.delete(port, guid);

            } catch (Exception e) {
                e.printStackTrace();
                rc += 98;
            }
        }
        else {
            System.out.println("DeleteFile <guid>");
            rc += 15;
        }

        System.exit(rc);
    }


    public boolean delete (CFService port, String guid) {
    	boolean rc = false;
    	try {
	        com.transglobe.cf.webservice.DeleteResponse res = port.deleteWithGuid("SIP", guid, "Y", "SIPFMT");
	        if (res != null) {
	        	java.util.List<Content> list = res.getSuccessList();
	        	if (list != null) {
	        		rc = true;
	        	}
	    	}
    	} catch (Exception e) {
                e.printStackTrace();
        }
        return rc;
    }
    
}
