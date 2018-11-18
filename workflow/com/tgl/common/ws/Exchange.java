
package com.tgl.common.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>exchange complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="exchange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://tgl.com/common/ws/}standardRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exchange", propOrder = {
    "arg0"
})
public class Exchange {

    protected StandardRequest arg0;

    /**
     * 取得 arg0 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link StandardRequest }
     *     
     */
    public StandardRequest getArg0() {
        return arg0;
    }

    /**
     * 設定 arg0 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardRequest }
     *     
     */
    public void setArg0(StandardRequest value) {
        this.arg0 = value;
    }

}
