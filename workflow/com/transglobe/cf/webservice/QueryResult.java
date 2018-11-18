
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>queryResult complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="queryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contentList" type="{http://webservice.cf.transglobe.com/}content" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reportId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryResult", propOrder = {
    "contentList",
    "reportId"
})
public class QueryResult {

    @XmlElement(nillable = true)
    protected List<Content> contentList;
    protected String reportId;

    /**
     * Gets the value of the contentList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contentList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContentList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Content }
     * 
     * 
     */
    public List<Content> getContentList() {
        if (contentList == null) {
            contentList = new ArrayList<Content>();
        }
        return this.contentList;
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

}
