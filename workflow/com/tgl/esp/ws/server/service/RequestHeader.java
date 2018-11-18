
package com.tgl.esp.ws.server.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>requestHeader complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="requestHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requesterPwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requesterSystem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requestHeader", propOrder = {
    "requesterID",
    "requesterPwd",
    "requesterSystem"
})
public class RequestHeader {

    protected String requesterID;
    protected String requesterPwd;
    protected String requesterSystem;

    /**
     * 取得 requesterID 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterID() {
        return requesterID;
    }

    /**
     * 設定 requesterID 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterID(String value) {
        this.requesterID = value;
    }

    /**
     * 取得 requesterPwd 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterPwd() {
        return requesterPwd;
    }

    /**
     * 設定 requesterPwd 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterPwd(String value) {
        this.requesterPwd = value;
    }

    /**
     * 取得 requesterSystem 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterSystem() {
        return requesterSystem;
    }

    /**
     * 設定 requesterSystem 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterSystem(String value) {
        this.requesterSystem = value;
    }

}
