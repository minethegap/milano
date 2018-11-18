
package com.transglobe.cf.webservice.bean.request;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>comparisonOperator 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * <p>
 * <pre>
 * &lt;simpleType name="comparisonOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EQ"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "comparisonOperator")
@XmlEnum
public enum ComparisonOperator {

    EQ,
    GT,
    GE,
    LT,
    LE;

    public String value() {
        return name();
    }

    public static ComparisonOperator fromValue(String v) {
        return valueOf(v);
    }

}
