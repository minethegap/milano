
package sipservertype;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clientIP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reqFile" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="stage" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="fileType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attachList" type="{urn:sipserverType}attachList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "clientIP",
    "userName",
    "vp",
    "jobName",
    "reqFile",
    "stage",
    "fileType",
    "attachList"
})
@XmlRootElement(name = "sendJobAndGetJobFile")
public class SendJobAndGetJobFile {

    protected String clientIP;
    protected String userName;
    protected String vp;
    protected String jobName;
    @XmlMimeType("application/octet-stream")
    protected DataHandler reqFile;
    protected int stage;
    protected String fileType;
    @XmlElement(required = true)
    protected AttachList attachList;

    /**
     * Gets the value of the clientIP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientIP() {
        return clientIP;
    }

    /**
     * Sets the value of the clientIP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientIP(String value) {
        this.clientIP = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the vp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVp() {
        return vp;
    }

    /**
     * Sets the value of the vp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVp(String value) {
        this.vp = value;
    }

    /**
     * Gets the value of the jobName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the value of the jobName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobName(String value) {
        this.jobName = value;
    }

    /**
     * Gets the value of the reqFile property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getReqFile() {
        return reqFile;
    }

    /**
     * Sets the value of the reqFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setReqFile(DataHandler value) {
        this.reqFile = value;
    }

    /**
     * Gets the value of the stage property.
     * 
     */
    public int getStage() {
        return stage;
    }

    /**
     * Sets the value of the stage property.
     * 
     */
    public void setStage(int value) {
        this.stage = value;
    }

    /**
     * Gets the value of the fileType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Sets the value of the fileType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileType(String value) {
        this.fileType = value;
    }

    /**
     * Gets the value of the attachList property.
     * 
     * @return
     *     possible object is
     *     {@link AttachList }
     *     
     */
    public AttachList getAttachList() {
        return attachList;
    }

    /**
     * Sets the value of the attachList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttachList }
     *     
     */
    public void setAttachList(AttachList value) {
        this.attachList = value;
    }

}
