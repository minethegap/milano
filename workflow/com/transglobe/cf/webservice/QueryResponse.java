
package com.transglobe.cf.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>queryResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="queryResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://webservice.cf.transglobe.com/}response">
 *       &lt;sequence>
 *         &lt;element name="queryResultList" type="{http://webservice.cf.transglobe.com/}queryResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryResponse", propOrder = {
    "queryResultList"
})
public class QueryResponse
    extends Response
{

    @XmlElement(nillable = true)
    protected List<QueryResult> queryResultList;

    /**
     * Gets the value of the queryResultList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the queryResultList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQueryResultList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryResult }
     * 
     * 
     */
    public List<QueryResult> getQueryResultList() {
        if (queryResultList == null) {
            queryResultList = new ArrayList<QueryResult>();
        }
        return this.queryResultList;
    }

}
