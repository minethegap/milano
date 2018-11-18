package com.transglobe.cf.webservice.bean.request;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="request.bean.webservice.cf.transglobe.com")
public class ConditionContract {

	public ConditionContract(){}

	public ConditionContract(String classType, Clause clause, Condition condition){
		this.classType = classType;
		this.clause = clause;
		this.condition = condition;
	}

	private String classType;

	private Clause clause;

	private Condition condition;

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public Clause getClause() {
		return clause;
	}

	public void setClause(Clause clause) {
		this.clause = clause;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
}
