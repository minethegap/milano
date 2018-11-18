
package com.tgl.common.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>rqHeader complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="rqHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="custID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prevReturnCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prevReturnMsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prevRqUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rqTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="rqUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rqHeader", propOrder = {
    "custID",
    "prevReturnCode",
    "prevReturnMsg",
    "prevRqUID",
    "rqTimestamp",
    "rqUID"
})
public class RqHeader {

    protected String custID;
    protected String prevReturnCode;
    protected String prevReturnMsg;
    protected String prevRqUID;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar rqTimestamp;
    protected String rqUID;

    /**
     * 取得 custID 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustID() {
        return custID;
    }

    /**
     * 設定 custID 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustID(String value) {
        this.custID = value;
    }

    /**
     * 取得 prevReturnCode 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrevReturnCode() {
        return prevReturnCode;
    }

    /**
     * 設定 prevReturnCode 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrevReturnCode(String value) {
        this.prevReturnCode = value;
    }

    /**
     * 取得 prevReturnMsg 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrevReturnMsg() {
        return prevReturnMsg;
    }

    /**
     * 設定 prevReturnMsg 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrevReturnMsg(String value) {
        this.prevReturnMsg = value;
    }

    /**
     * 取得 prevRqUID 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrevRqUID() {
        return prevRqUID;
    }

    /**
     * 設定 prevRqUID 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrevRqUID(String value) {
        this.prevRqUID = value;
    }

    /**
     * 取得 rqTimestamp 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRqTimestamp() {
        return rqTimestamp;
    }

    /**
     * 設定 rqTimestamp 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRqTimestamp(XMLGregorianCalendar value) {
        this.rqTimestamp = value;
    }

    /**
     * 取得 rqUID 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRqUID() {
        return rqUID;
    }

    /**
     * 設定 rqUID 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRqUID(String value) {
        this.rqUID = value;
    }

}
