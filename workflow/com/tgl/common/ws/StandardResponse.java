
package com.tgl.common.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>standardResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="standardResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileList" type="{http://tgl.com/common/ws/}fileObject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="rsBody" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rsHeader" type="{http://tgl.com/common/ws/}rsHeader" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "standardResponse", propOrder = {
    "fileList",
    "rsBody",
    "rsHeader"
})
public class StandardResponse {

    @XmlElement(nillable = true)
    protected List<FileObject> fileList;
    protected String rsBody;
    protected RsHeader rsHeader;

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
     * 取得 rsBody 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsBody() {
        return rsBody;
    }

    /**
     * 設定 rsBody 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsBody(String value) {
        this.rsBody = value;
    }

    /**
     * 取得 rsHeader 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link RsHeader }
     *     
     */
    public RsHeader getRsHeader() {
        return rsHeader;
    }

    /**
     * 設定 rsHeader 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link RsHeader }
     *     
     */
    public void setRsHeader(RsHeader value) {
        this.rsHeader = value;
    }

}
