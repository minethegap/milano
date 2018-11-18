
package sipservertype;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.0.3
 * 2016-06-29T14:20:58.538+08:00
 * Generated source version: 3.0.3
 * 
 */
public final class SipserverPortType_Sipserver_Client {

    private static final QName SERVICE_NAME = new QName("urn:sipserverType", "sipserver");

    private SipserverPortType_Sipserver_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = Sipserver.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        Sipserver ss = new Sipserver(wsdlURL, SERVICE_NAME);
        SipserverPortType port = ss.getSipserver();  
        
        {
        System.out.println("Invoking sendJobAndGetJobFile...");
        java.lang.String _sendJobAndGetJobFile_clientIP = "";
        java.lang.String _sendJobAndGetJobFile_userName = "";
        java.lang.String _sendJobAndGetJobFile_vp = "";
        java.lang.String _sendJobAndGetJobFile_jobName = "";
        javax.activation.DataHandler _sendJobAndGetJobFile_reqFile = null;
        int _sendJobAndGetJobFile_stage = 0;
        java.lang.String _sendJobAndGetJobFile_fileType = "";
        sipservertype.AttachList _sendJobAndGetJobFile_attachList = null;
        javax.xml.ws.Holder<java.lang.String> _sendJobAndGetJobFile_jobId = new javax.xml.ws.Holder<java.lang.String>();
        javax.xml.ws.Holder<java.lang.String> _sendJobAndGetJobFile_error = new javax.xml.ws.Holder<java.lang.String>();
        javax.xml.ws.Holder<javax.activation.DataHandler> _sendJobAndGetJobFile_resFile = new javax.xml.ws.Holder<javax.activation.DataHandler>();
        port.sendJobAndGetJobFile(_sendJobAndGetJobFile_clientIP, _sendJobAndGetJobFile_userName, _sendJobAndGetJobFile_vp, _sendJobAndGetJobFile_jobName, _sendJobAndGetJobFile_reqFile, _sendJobAndGetJobFile_stage, _sendJobAndGetJobFile_fileType, _sendJobAndGetJobFile_attachList, _sendJobAndGetJobFile_jobId, _sendJobAndGetJobFile_error, _sendJobAndGetJobFile_resFile);

        System.out.println("sendJobAndGetJobFile._sendJobAndGetJobFile_jobId=" + _sendJobAndGetJobFile_jobId.value);
        System.out.println("sendJobAndGetJobFile._sendJobAndGetJobFile_error=" + _sendJobAndGetJobFile_error.value);
        System.out.println("sendJobAndGetJobFile._sendJobAndGetJobFile_resFile=" + _sendJobAndGetJobFile_resFile.value);

        }

        System.exit(0);
    }

}
