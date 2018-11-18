
package sipservertype;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for attach complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="attach">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attFile" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attach", propOrder = {
    "attName",
    "attFile"
})
public class Attach {

    protected String attName;
    @XmlMimeType("application/octet-stream")
    protected DataHandler attFile;

    /**
     * Gets the value of the attName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttName() {
        return attName;
    }

    /**
     * Sets the value of the attName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttName(String value) {
        this.attName = value;
    }

    /**
     * Gets the value of the attFile property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getAttFile() {
        return attFile;
    }

    /**
     * Sets the value of the attFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setAttFile(DataHandler value) {
        this.attFile = value;
    }

}
