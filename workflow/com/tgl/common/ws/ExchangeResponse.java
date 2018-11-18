
package com.tgl.common.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>exchangeResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="exchangeResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://tgl.com/common/ws/}standardResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exchangeResponse", propOrder = {
    "_return"
})
public class ExchangeResponse {

    @XmlElement(name = "return")
    protected StandardResponse _return;

    /**
     * 取得 return 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link StandardResponse }
     *     
     */
    public StandardResponse getReturn() {
        return _return;
    }

    /**
     * 設定 return 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardResponse }
     *     
     */
    public void setReturn(StandardResponse value) {
        this._return = value;
    }

}
