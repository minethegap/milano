
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.transglobe.cf.webservice.bean.Index;


/**
 * <p>content complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reportId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="indexList" type="{bean.webservice.cf.transglobe.com}index" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="propertyList" type="{bean.webservice.cf.transglobe.com}index" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "content", propOrder = {
    "guid",
    "contentData",
    "contentType",
    "contentName",
    "reportId",
    "dataDate",
    "version",
    "indexList",
    "propertyList"
})
public class Content {

    protected String guid;
    @XmlMimeType("application/octet-stream")
    protected DataHandler contentData;
    protected String contentType;
    protected String contentName;
    protected String reportId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dataDate;
    protected String version;
    @XmlElement(nillable = true)
    protected List<Index> indexList;
    @XmlElement(nillable = true)
    protected List<Index> propertyList;

    /**
     * 取得 guid 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * 設定 guid 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * 取得 contentData 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getContentData() {
        return contentData;
    }

    /**
     * 設定 contentData 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setContentData(DataHandler value) {
        this.contentData = value;
    }

    /**
     * 取得 contentType 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 設定 contentType 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * 取得 contentName 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentName() {
        return contentName;
    }

    /**
     * 設定 contentName 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentName(String value) {
        this.contentName = value;
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
     * 取得 dataDate 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataDate() {
        return dataDate;
    }

    /**
     * 設定 dataDate 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataDate(XMLGregorianCalendar value) {
        this.dataDate = value;
    }

    /**
     * 取得 version 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * 設定 version 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the indexList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the indexList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIndexList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Index }
     * 
     * 
     */
    public List<Index> getIndexList() {
        if (indexList == null) {
            indexList = new ArrayList<Index>();
        }
        return this.indexList;
    }

    /**
     * Gets the value of the propertyList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Index }
     * 
     * 
     */
    public List<Index> getPropertyList() {
        if (propertyList == null) {
            propertyList = new ArrayList<Index>();
        }
        return this.propertyList;
    }

}
