
package com.tgl.esp.ws.server.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tgl.esp.ws.server.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetTransferedTemplateByIDResponse_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "getTransferedTemplateByIDResponse");
    private final static QName _RetrieveStaticMailGroupMemberByID_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "retrieveStaticMailGroupMemberByID");
    private final static QName _GetTransferedTemplateByID_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "getTransferedTemplateByID");
    private final static QName _RetrieveStaticMailGroupMemberByIDResponse_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "retrieveStaticMailGroupMemberByIDResponse");
    private final static QName _GetRawTemplateByID_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "getRawTemplateByID");
    private final static QName _GetRawTemplateByIDResponse_QNAME = new QName("http://service.server.ws.esp.tgl.com/", "getRawTemplateByIDResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tgl.esp.ws.server.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TemplateContentRequest }
     * 
     */
    public TemplateContentRequest createTemplateContentRequest() {
        return new TemplateContentRequest();
    }

    /**
     * Create an instance of {@link TemplateContentRequest.ParameterMap }
     * 
     */
    public TemplateContentRequest.ParameterMap createTemplateContentRequestParameterMap() {
        return new TemplateContentRequest.ParameterMap();
    }

    /**
     * Create an instance of {@link GetTransferedTemplateByIDResponse }
     * 
     */
    public GetTransferedTemplateByIDResponse createGetTransferedTemplateByIDResponse() {
        return new GetTransferedTemplateByIDResponse();
    }

    /**
     * Create an instance of {@link RetrieveStaticMailGroupMemberByID }
     * 
     */
    public RetrieveStaticMailGroupMemberByID createRetrieveStaticMailGroupMemberByID() {
        return new RetrieveStaticMailGroupMemberByID();
    }

    /**
     * Create an instance of {@link GetTransferedTemplateByID }
     * 
     */
    public GetTransferedTemplateByID createGetTransferedTemplateByID() {
        return new GetTransferedTemplateByID();
    }

    /**
     * Create an instance of {@link RetrieveStaticMailGroupMemberByIDResponse }
     * 
     */
    public RetrieveStaticMailGroupMemberByIDResponse createRetrieveStaticMailGroupMemberByIDResponse() {
        return new RetrieveStaticMailGroupMemberByIDResponse();
    }

    /**
     * Create an instance of {@link GetRawTemplateByIDResponse }
     * 
     */
    public GetRawTemplateByIDResponse createGetRawTemplateByIDResponse() {
        return new GetRawTemplateByIDResponse();
    }

    /**
     * Create an instance of {@link GetRawTemplateByID }
     * 
     */
    public GetRawTemplateByID createGetRawTemplateByID() {
        return new GetRawTemplateByID();
    }

    /**
     * Create an instance of {@link BaseResponse }
     * 
     */
    public BaseResponse createBaseResponse() {
        return new BaseResponse();
    }

    /**
     * Create an instance of {@link RawTemplateContentRequest }
     * 
     */
    public RawTemplateContentRequest createRawTemplateContentRequest() {
        return new RawTemplateContentRequest();
    }

    /**
     * Create an instance of {@link MailGroupMemberRequest }
     * 
     */
    public MailGroupMemberRequest createMailGroupMemberRequest() {
        return new MailGroupMemberRequest();
    }

    /**
     * Create an instance of {@link ResponseHeader }
     * 
     */
    public ResponseHeader createResponseHeader() {
        return new ResponseHeader();
    }

    /**
     * Create an instance of {@link MailGroupMemberResponse }
     * 
     */
    public MailGroupMemberResponse createMailGroupMemberResponse() {
        return new MailGroupMemberResponse();
    }

    /**
     * Create an instance of {@link BaseRequest }
     * 
     */
    public BaseRequest createBaseRequest() {
        return new BaseRequest();
    }

    /**
     * Create an instance of {@link MailGroupMember }
     * 
     */
    public MailGroupMember createMailGroupMember() {
        return new MailGroupMember();
    }

    /**
     * Create an instance of {@link TemplateContent }
     * 
     */
    public TemplateContent createTemplateContent() {
        return new TemplateContent();
    }

    /**
     * Create an instance of {@link RequestHeader }
     * 
     */
    public RequestHeader createRequestHeader() {
        return new RequestHeader();
    }

    /**
     * Create an instance of {@link TemplateContentResponse }
     * 
     */
    public TemplateContentResponse createTemplateContentResponse() {
        return new TemplateContentResponse();
    }

    /**
     * Create an instance of {@link TemplateContentRequest.ParameterMap.Entry }
     * 
     */
    public TemplateContentRequest.ParameterMap.Entry createTemplateContentRequestParameterMapEntry() {
        return new TemplateContentRequest.ParameterMap.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTransferedTemplateByIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "getTransferedTemplateByIDResponse")
    public JAXBElement<GetTransferedTemplateByIDResponse> createGetTransferedTemplateByIDResponse(GetTransferedTemplateByIDResponse value) {
        return new JAXBElement<GetTransferedTemplateByIDResponse>(_GetTransferedTemplateByIDResponse_QNAME, GetTransferedTemplateByIDResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveStaticMailGroupMemberByID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "retrieveStaticMailGroupMemberByID")
    public JAXBElement<RetrieveStaticMailGroupMemberByID> createRetrieveStaticMailGroupMemberByID(RetrieveStaticMailGroupMemberByID value) {
        return new JAXBElement<RetrieveStaticMailGroupMemberByID>(_RetrieveStaticMailGroupMemberByID_QNAME, RetrieveStaticMailGroupMemberByID.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTransferedTemplateByID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "getTransferedTemplateByID")
    public JAXBElement<GetTransferedTemplateByID> createGetTransferedTemplateByID(GetTransferedTemplateByID value) {
        return new JAXBElement<GetTransferedTemplateByID>(_GetTransferedTemplateByID_QNAME, GetTransferedTemplateByID.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveStaticMailGroupMemberByIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "retrieveStaticMailGroupMemberByIDResponse")
    public JAXBElement<RetrieveStaticMailGroupMemberByIDResponse> createRetrieveStaticMailGroupMemberByIDResponse(RetrieveStaticMailGroupMemberByIDResponse value) {
        return new JAXBElement<RetrieveStaticMailGroupMemberByIDResponse>(_RetrieveStaticMailGroupMemberByIDResponse_QNAME, RetrieveStaticMailGroupMemberByIDResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRawTemplateByID }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "getRawTemplateByID")
    public JAXBElement<GetRawTemplateByID> createGetRawTemplateByID(GetRawTemplateByID value) {
        return new JAXBElement<GetRawTemplateByID>(_GetRawTemplateByID_QNAME, GetRawTemplateByID.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRawTemplateByIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.server.ws.esp.tgl.com/", name = "getRawTemplateByIDResponse")
    public JAXBElement<GetRawTemplateByIDResponse> createGetRawTemplateByIDResponse(GetRawTemplateByIDResponse value) {
        return new JAXBElement<GetRawTemplateByIDResponse>(_GetRawTemplateByIDResponse_QNAME, GetRawTemplateByIDResponse.class, null, value);
    }

}
