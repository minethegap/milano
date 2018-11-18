package com.transglobe.cf.webservice.bean.request;

import javax.xml.bind.annotation.XmlType;

import com.transglobe.cf.webservice.bean.Index;

@XmlType(namespace="request.bean.webservice.cf.transglobe.com")
public class Condition extends Index{
	private static final long serialVersionUID = -118946089044634835L;

	private LogicalOperator logicalOperator;

	private ComparisonOperator comparisonOperator;

	public Condition(){}

	public Condition(String key, Object value, LogicalOperator logicalOperator, ComparisonOperator comparisonOperator){
		setKey(key);
		setValue(value);
		this.logicalOperator = logicalOperator;
		this.comparisonOperator = comparisonOperator;
	}

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public ComparisonOperator getComparisonOperator() {
		return comparisonOperator;
	}

	public void setComparisonOperator(ComparisonOperator comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}
}
