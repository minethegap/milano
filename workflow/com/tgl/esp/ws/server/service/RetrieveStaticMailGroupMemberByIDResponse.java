
package com.tgl.esp.ws.server.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>retrieveStaticMailGroupMemberByIDResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="retrieveStaticMailGroupMemberByIDResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://service.server.ws.esp.tgl.com/}mailGroupMemberResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "retrieveStaticMailGroupMemberByIDResponse", propOrder = {
    "_return"
})
public class RetrieveStaticMailGroupMemberByIDResponse {

    @XmlElement(name = "return")
    protected MailGroupMemberResponse _return;

    /**
     * 取得 return 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link MailGroupMemberResponse }
     *     
     */
    public MailGroupMemberResponse getReturn() {
        return _return;
    }

    /**
     * 設定 return 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link MailGroupMemberResponse }
     *     
     */
    public void setReturn(MailGroupMemberResponse value) {
        this._return = value;
    }

}
