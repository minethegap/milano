
package com.tgl.common.ws;

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
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.0.3
 * 2017-04-19T18:47:06.916+08:00
 * Generated source version: 3.0.3
 * 
 */
public final class CommonWS_CommonWSServicePort_Client {

    private static final QName SERVICE_NAME = new QName("http://tgl.com/common/ws/", "CommonWSService");

    private CommonWS_CommonWSServicePort_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = CommonWSService.WSDL_LOCATION;
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
      
        CommonWSService ss = new CommonWSService(wsdlURL, SERVICE_NAME);
        CommonWS port = ss.getCommonWSServicePort();  
        
        {
        System.out.println("Invoking exchange...");
        com.tgl.common.ws.StandardRequest _exchange_arg0 = null;
        com.tgl.common.ws.StandardResponse _exchange__return = port.exchange(_exchange_arg0);
        System.out.println("exchange.result=" + _exchange__return);


        }

        System.exit(0);
    }

}