
package com.tgl.esp.ws.server.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>templateContentResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="templateContentResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://service.server.ws.esp.tgl.com/}baseResponse">
 *       &lt;sequence>
 *         &lt;element name="templateContent" type="{http://service.server.ws.esp.tgl.com/}templateContent" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "templateContentResponse", propOrder = {
    "templateContent"
})
public class TemplateContentResponse
    extends BaseResponse
{

    protected TemplateContent templateContent;

    /**
     * 取得 templateContent 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link TemplateContent }
     *     
     */
    public TemplateContent getTemplateContent() {
        return templateContent;
    }

    /**
     * 設定 templateContent 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link TemplateContent }
     *     
     */
    public void setTemplateContent(TemplateContent value) {
        this.templateContent = value;
    }

}
