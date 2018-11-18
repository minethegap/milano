
package com.transglobe.cf.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>archiveFile complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="archiveFile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="overWriteOption" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="repeatArchive" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="archiveContent" type="{http://webservice.cf.transglobe.com/}content" minOccurs="0"/>
 *         &lt;element name="isRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="projectCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archiveFile", propOrder = {
    "user",
    "overWriteOption",
    "repeatArchive",
    "archiveContent",
    "isRelease",
    "projectCode"
})
public class ArchiveFile {

    protected String user;
    protected String overWriteOption;
    protected String repeatArchive;
    protected Content archiveContent;
    protected String isRelease;
    protected String projectCode;

    /**
     * 取得 user 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * 設定 user 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * 取得 overWriteOption 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverWriteOption() {
        return overWriteOption;
    }

    /**
     * 設定 overWriteOption 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverWriteOption(String value) {
        this.overWriteOption = value;
    }

    /**
     * 取得 repeatArchive 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepeatArchive() {
        return repeatArchive;
    }

    /**
     * 設定 repeatArchive 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepeatArchive(String value) {
        this.repeatArchive = value;
    }

    /**
     * 取得 archiveContent 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Content }
     *     
     */
    public Content getArchiveContent() {
        return archiveContent;
    }

    /**
     * 設定 archiveContent 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Content }
     *     
     */
    public void setArchiveContent(Content value) {
        this.archiveContent = value;
    }

    /**
     * 取得 isRelease 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsRelease() {
        return isRelease;
    }

    /**
     * 設定 isRelease 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsRelease(String value) {
        this.isRelease = value;
    }

    /**
     * 取得 projectCode 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectCode() {
        return projectCode;
    }

    /**
     * 設定 projectCode 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectCode(String value) {
        this.projectCode = value;
    }

}
