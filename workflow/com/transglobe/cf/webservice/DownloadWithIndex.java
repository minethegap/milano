
package com.transglobe.cf.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.transglobe.cf.webservice.bean.request.Clause;


/**
 * <p>downloadWithIndex complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="downloadWithIndex">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reportId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="squeezeDate" type="{http://webservice.cf.transglobe.com/}squeezeDate" minOccurs="0"/>
 *         &lt;element name="onlyLastVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryIndex" type="{request.bean.webservice.cf.transglobe.com}clause" minOccurs="0"/>
 *         &lt;element name="onlyBookmarkSection" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "downloadWithIndex", propOrder = {
    "user",
    "reportId",
    "squeezeDate",
    "onlyLastVersion",
    "queryIndex",
    "onlyBookmarkSection",
    "isRelease",
    "projectCode"
})
public class DownloadWithIndex {

    protected String user;
    protected String reportId;
    protected SqueezeDate squeezeDate;
    protected String onlyLastVersion;
    protected Clause queryIndex;
    protected String onlyBookmarkSection;
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
     * 取得 reportId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportId() {
        return reportId;
    }

    /**
     * 設定 reportId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportId(String value) {
        this.reportId = value;
    }

    /**
     * 取得 squeezeDate 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link SqueezeDate }
     *     
     */
    public SqueezeDate getSqueezeDate() {
        return squeezeDate;
    }

    /**
     * 設定 squeezeDate 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link SqueezeDate }
     *     
     */
    public void setSqueezeDate(SqueezeDate value) {
        this.squeezeDate = value;
    }

    /**
     * 取得 onlyLastVersion 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnlyLastVersion() {
        return onlyLastVersion;
    }

    /**
     * 設定 onlyLastVersion 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnlyLastVersion(String value) {
        this.onlyLastVersion = value;
    }

    /**
     * 取得 queryIndex 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Clause }
     *     
     */
    public Clause getQueryIndex() {
        return queryIndex;
    }

    /**
     * 設定 queryIndex 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Clause }
     *     
     */
    public void setQueryIndex(Clause value) {
        this.queryIndex = value;
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
