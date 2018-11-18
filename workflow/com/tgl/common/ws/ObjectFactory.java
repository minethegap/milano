
package com.tgl.common.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tgl.common.ws package. 
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

    private final static QName _ExchangeResponse_QNAME = new QName("http://tgl.com/common/ws/", "exchangeResponse");
    private final static QName _StandardResponse_QNAME = new QName("http://tgl.com/common/ws/", "standardResponse");
    private final static QName _StandardRequest_QNAME = new QName("http://tgl.com/common/ws/", "standardRequest");
    private final static QName _Exchange_QNAME = new QName("http://tgl.com/common/ws/", "exchange");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tgl.common.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StandardResponse }
     * 
     */
    public StandardResponse createStandardResponse() {
        return new StandardResponse();
    }

    /**
     * Create an instance of {@link ExchangeResponse }
     * 
     */
    public ExchangeResponse createExchangeResponse() {
        return new ExchangeResponse();
    }

    /**
     * Create an instance of {@link Exchange }
     * 
     */
    public Exchange createExchange() {
        return new Exchange();
    }

    /**
     * Create an instance of {@link StandardRequest }
     * 
     */
    public StandardRequest createStandardRequest() {
        return new StandardRequest();
    }

    /**
     * Create an instance of {@link RqHeader }
     * 
     */
    public RqHeader createRqHeader() {
        return new RqHeader();
    }

    /**
     * Create an instance of {@link FileObject }
     * 
     */
    public FileObject createFileObject() {
        return new FileObject();
    }

    /**
     * Create an instance of {@link RsHeader }
     * 
     */
    public RsHeader createRsHeader() {
        return new RsHeader();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExchangeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tgl.com/common/ws/", name = "exchangeResponse")
    public JAXBElement<ExchangeResponse> createExchangeResponse(ExchangeResponse value) {
        return new JAXBElement<ExchangeResponse>(_ExchangeResponse_QNAME, ExchangeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StandardResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tgl.com/common/ws/", name = "standardResponse")
    public JAXBElement<StandardResponse> createStandardResponse(StandardResponse value) {
        return new JAXBElement<StandardResponse>(_StandardResponse_QNAME, StandardResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StandardRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tgl.com/common/ws/", name = "standardRequest")
    public JAXBElement<StandardRequest> createStandardRequest(StandardRequest value) {
        return new JAXBElement<StandardRequest>(_StandardRequest_QNAME, StandardRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exchange }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tgl.com/common/ws/", name = "exchange")
    public JAXBElement<Exchange> createExchange(Exchange value) {
        return new JAXBElement<Exchange>(_Exchange_QNAME, Exchange.class, null, value);
    }

}
