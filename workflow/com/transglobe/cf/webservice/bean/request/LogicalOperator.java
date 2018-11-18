
package com.transglobe.cf.webservice.bean.request;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>logicalOperator 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * <p>
 * <pre>
 * &lt;simpleType name="logicalOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AND"/>
 *     &lt;enumeration value="OR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "logicalOperator")
@XmlEnum
public enum LogicalOperator {

    AND,
    OR;

    public String value() {
        return name();
    }

    public static LogicalOperator fromValue(String v) {
        return valueOf(v);
    }

}
