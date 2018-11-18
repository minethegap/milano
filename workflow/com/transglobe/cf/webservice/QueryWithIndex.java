
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.transglobe.cf.webservice.bean.request.Clause;


/**
 * <p>queryWithIndex complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="queryWithIndex">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reportIdList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="squeezeDate" type="{http://webservice.cf.transglobe.com/}squeezeDate" minOccurs="0"/>
 *         &lt;element name="onlyLastVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryIndex" type="{request.bean.webservice.cf.transglobe.com}clause" minOccurs="0"/>
 *         &lt;element name="sort" type="{http://webservice.cf.transglobe.com/}sort" minOccurs="0"/>
 *         &lt;element name="page" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="pageSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
@XmlType(name = "queryWithIndex", propOrder = {
    "user",
    "reportIdList",
    "squeezeDate",
    "onlyLastVersion",
    "queryIndex",
    "sort",
    "page",
    "pageSize",
    "isRelease",
    "projectCode"
})
public class QueryWithIndex {

    protected String user;
    protected List<String> reportIdList;
    protected SqueezeDate squeezeDate;
    protected String onlyLastVersion;
    protected Clause queryIndex;
    protected Sort sort;
    protected Integer page;
    protected Integer pageSize;
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
     * Gets the value of the reportIdList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportIdList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportIdList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReportIdList() {
        if (reportIdList == null) {
            reportIdList = new ArrayList<String>();
        }
        return this.reportIdList;
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
     * 取得 sort 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Sort }
     *     
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * 設定 sort 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Sort }
     *     
     */
    public void setSort(Sort value) {
        this.sort = value;
    }

    /**
     * 取得 page 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPage() {
        return page;
    }

    /**
     * 設定 page 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPage(Integer value) {
        this.page = value;
    }

    /**
     * 取得 pageSize 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 設定 pageSize 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPageSize(Integer value) {
        this.pageSize = value;
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
