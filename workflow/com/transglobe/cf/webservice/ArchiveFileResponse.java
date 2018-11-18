
package com.transglobe.cf.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>archiveFileResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="archiveFileResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://webservice.cf.transglobe.com/}archiveResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archiveFileResponse", propOrder = {
    "_return"
})
public class ArchiveFileResponse {

    @XmlElement(name = "return")
    protected ArchiveResponse _return;

    /**
     * 取得 return 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link ArchiveResponse }
     *     
     */
    public ArchiveResponse getReturn() {
        return _return;
    }

    /**
     * 設定 return 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link ArchiveResponse }
     *     
     */
    public void setReturn(ArchiveResponse value) {
        this._return = value;
    }

}
