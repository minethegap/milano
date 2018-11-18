package sipservertype;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.0.3
 * 2016-06-29T14:20:58.617+08:00
 * Generated source version: 3.0.3
 * 
 */
@WebService(targetNamespace = "urn:sipserverType", name = "sipserverPortType")
@XmlSeeAlso({ObjectFactory.class})
public interface SipserverPortType {

    @RequestWrapper(localName = "sendJobAndGetJobFile", targetNamespace = "urn:sipserverType", className = "sipservertype.SendJobAndGetJobFile")
    @WebMethod
    @ResponseWrapper(localName = "sendJobAndGetJobFileResponse", targetNamespace = "urn:sipserverType", className = "sipservertype.SendJobAndGetJobFileResponse")
    public void sendJobAndGetJobFile(
        @WebParam(name = "clientIP", targetNamespace = "")
        java.lang.String clientIP,
        @WebParam(name = "userName", targetNamespace = "")
        java.lang.String userName,
        @WebParam(name = "vp", targetNamespace = "")
        java.lang.String vp,
        @WebParam(name = "jobName", targetNamespace = "")
        java.lang.String jobName,
        @WebParam(name = "reqFile", targetNamespace = "")
        javax.activation.DataHandler reqFile,
        @WebParam(name = "stage", targetNamespace = "")
        int stage,
        @WebParam(name = "fileType", targetNamespace = "")
        java.lang.String fileType,
        @WebParam(name = "attachList", targetNamespace = "")
        sipservertype.AttachList attachList,
        @WebParam(mode = WebParam.Mode.OUT, name = "jobId", targetNamespace = "")
        javax.xml.ws.Holder<java.lang.String> jobId,
        @WebParam(mode = WebParam.Mode.OUT, name = "error", targetNamespace = "")
        javax.xml.ws.Holder<java.lang.String> error,
        @WebParam(mode = WebParam.Mode.OUT, name = "resFile", targetNamespace = "")
        javax.xml.ws.Holder<javax.activation.DataHandler> resFile
    );
}