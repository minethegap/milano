
package com.tgl.esp.ws.server.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>mailGroupMemberResponse complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="mailGroupMemberResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://service.server.ws.esp.tgl.com/}baseResponse">
 *       &lt;sequence>
 *         &lt;element name="memberList" type="{http://service.server.ws.esp.tgl.com/}mailGroupMember" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailGroupMemberResponse", propOrder = {
    "memberList"
})
public class MailGroupMemberResponse
    extends BaseResponse
{

    @XmlElement(nillable = true)
    protected List<MailGroupMember> memberList;

    /**
     * Gets the value of the memberList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the memberList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMemberList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MailGroupMember }
     * 
     * 
     */
    public List<MailGroupMember> getMemberList() {
        if (memberList == null) {
            memberList = new ArrayList<MailGroupMember>();
        }
        return this.memberList;
    }

}
