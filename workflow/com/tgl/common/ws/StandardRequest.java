
package com.tgl.common.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>standardRequest complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="standardRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileList" type="{http://tgl.com/common/ws/}fileObject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rqBody" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rqHeader" type="{http://tgl.com/common/ws/}rqHeader" minOccurs="0"/>
 *         &lt;element name="serviceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="systemCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "standardRequest", propOrder = {
    "fileList",
    "rqBody",
    "rqHeader",
    "serviceName",
    "systemCode"
})
public class StandardRequest {

    @XmlElement(nillable = true)
    protected List<FileObject> fileList;
    protected String rqBody;
    protected RqHeader rqHeader;
    protected String serviceName;
    protected String systemCode;

    /**
     * Gets the value of the fileList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileObject }
     * 
     * 
     */
    public List<FileObject> getFileList() {
        if (fileList == null) {
            fileList = new ArrayList<FileObject>();
        }
        return this.fileList;
    }

    /**
     * 取得 rqBody 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRqBody() {
        return rqBody;
    }

    /**
     * 設定 rqBody 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRqBody(String value) {
        this.rqBody = value;
    }

    /**
     * 取得 rqHeader 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link RqHeader }
     *     
     */
    public RqHeader getRqHeader() {
        return rqHeader;
    }

    /**
     * 設定 rqHeader 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link RqHeader }
     *     
     */
    public void setRqHeader(RqHeader value) {
        this.rqHeader = value;
    }

    /**
     * 取得 serviceName 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 設定 serviceName 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceName(String value) {
        this.serviceName = value;
    }

    /**
     * 取得 systemCode 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemCode() {
        return systemCode;
    }

    /**
     * 設定 systemCode 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemCode(String value) {
        this.systemCode = value;
    }

}
