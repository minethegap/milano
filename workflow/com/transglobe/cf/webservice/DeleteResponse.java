
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>deleteResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="deleteResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webservice.cf.transglobe.com/}response">
 *       &lt;sequence>
 *         &lt;element name="errorList" type="{http://webservice.cf.transglobe.com/}content" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="successList" type="{http://webservice.cf.transglobe.com/}content" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteResponse", propOrder = {
    "errorList",
    "successList"
})
public class DeleteResponse
    extends Response
{

    @XmlElement(nillable = true)
    protected List<Content> errorList;
    @XmlElement(nillable = true)
    protected List<Content> successList;

    /**
     * Gets the value of the errorList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Content }
     * 
     * 
     */
    public List<Content> getErrorList() {
        if (errorList == null) {
            errorList = new ArrayList<Content>();
        }
        return this.errorList;
    }

    /**
     * Gets the value of the successList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the successList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuccessList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Content }
     * 
     * 
     */
    public List<Content> getSuccessList() {
        if (successList == null) {
            successList = new ArrayList<Content>();
        }
        return this.successList;
    }

}
