
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>downloadWithGuid complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="downloadWithGuid">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="guidList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="merge" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="zip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="zipPassword" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="onlyBookmarkSection" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "downloadWithGuid", propOrder = {
    "user",
    "guidList",
    "merge",
    "zip",
    "zipPassword",
    "onlyBookmarkSection",
    "projectCode"
})
public class DownloadWithGuid {

    protected String user;
    protected List<String> guidList;
    protected String merge;
    protected String zip;
    protected String zipPassword;
    protected String onlyBookmarkSection;
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
     * Gets the value of the guidList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the guidList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGuidList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getGuidList() {
        if (guidList == null) {
            guidList = new ArrayList<String>();
        }
        return this.guidList;
    }

    /**
     * 取得 merge 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMerge() {
        return merge;
    }

    /**
     * 設定 merge 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMerge(String value) {
        this.merge = value;
    }

    /**
     * 取得 zip 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZip() {
        return zip;
    }

    /**
     * 設定 zip 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZip(String value) {
        this.zip = value;
    }

    /**
     * 取得 zipPassword 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZipPassword() {
        return zipPassword;
    }

    /**
     * 設定 zipPassword 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZipPassword(String value) {
        this.zipPassword = value;
    }

    /**
     * 取得 onlyBookmarkSection 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnlyBookmarkSection() {
        return onlyBookmarkSection;
    }

    /**
     * 設定 onlyBookmarkSection 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnlyBookmarkSection(String value) {
        this.onlyBookmarkSection = value;
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
