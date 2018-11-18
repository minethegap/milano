
package com.tgl.esp.ws.server.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>retrieveStaticMailGroupMemberByID complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="retrieveStaticMailGroupMemberByID">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://service.server.ws.esp.tgl.com/}mailGroupMemberRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "retrieveStaticMailGroupMemberByID", propOrder = {
    "arg0"
})
public class RetrieveStaticMailGroupMemberByID {

    protected MailGroupMemberRequest arg0;

    /**
     * 取得 arg0 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link MailGroupMemberRequest }
     *     
     */
    public MailGroupMemberRequest getArg0() {
        return arg0;
    }

    /**
     * 設定 arg0 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link MailGroupMemberRequest }
     *     
     */
    public void setArg0(MailGroupMemberRequest value) {
        this.arg0 = value;
    }

}
