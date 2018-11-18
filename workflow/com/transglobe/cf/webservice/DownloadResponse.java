
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>downloadResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="downloadResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webservice.cf.transglobe.com/}response">
 *       &lt;sequence>
 *         &lt;element name="combinedContent" type="{http://webservice.cf.transglobe.com/}content" minOccurs="0"/>
 *         &lt;element name="contentList" type="{http://webservice.cf.transglobe.com/}content" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "downloadResponse", propOrder = {
    "combinedContent",
    "contentList"
})
public class DownloadResponse
    extends Response
{

    protected Content combinedContent;
    @XmlElement(nillable = true)
    protected List<Content> contentList;

    /**
     * 取得 combinedContent 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Content }
     *     
     */
    public Content getCombinedContent() {
        return combinedContent;
    }

    /**
     * 設定 combinedContent 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Content }
     *     
     */
    public void setCombinedContent(Content value) {
        this.combinedContent = value;
    }

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

}
