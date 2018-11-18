
package com.tgl.esp.ws.server.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>mailGroupMemberRequest complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="mailGroupMemberRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{http://service.server.ws.esp.tgl.com/}baseRequest">
 *       &lt;sequence>
 *         &lt;element name="mailGroupId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailGroupMemberRequest", propOrder = {
    "mailGroupId"
})
public class MailGroupMemberRequest
    extends BaseRequest
{

    protected String mailGroupId;

    /**
     * 取得 mailGroupId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailGroupId() {
        return mailGroupId;
    }

    /**
     * 設定 mailGroupId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailGroupId(String value) {
        this.mailGroupId = value;
    }

}
