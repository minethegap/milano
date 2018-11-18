package com.transglobe.cf.webservice.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="bean.webservice.cf.transglobe.com")
public class Index implements Serializable{
	private static final long serialVersionUID = -1324490547636762791L;

	private String key;

	private Object value;

	public Index(){}

	public Index(String key, Object value){
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
