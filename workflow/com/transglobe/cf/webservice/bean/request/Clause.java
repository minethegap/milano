package com.transglobe.cf.webservice.bean.request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.transglobe.cf.webservice.bean.Index;

@XmlType(namespace="request.bean.webservice.cf.transglobe.com")
public class Clause{

	private final String conditionClassType = "Condition";

	private final String clauseClassType = "Clause";

	public final static SimpleDateFormat trimToDay = new SimpleDateFormat("yyyyMMdd");

	public final static SimpleDateFormat cfDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

	static{
		cfDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private List<ConditionContract> conditionList = new ArrayList<ConditionContract>();

	private LogicalOperator logicalOperator;

	public List<ConditionContract> getConditionList() {
		return conditionList;
	}

	public void setConditionList(List<ConditionContract> conditionList) {
		this.conditionList = conditionList;
	}

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public Clause and(String key, Object value, ComparisonOperator comparisonOperator){
		this.conditionList.add(new ConditionContract(conditionClassType, null, new Condition(key, value, LogicalOperator.AND, comparisonOperator)));

		return this;
	}

	public Clause and(Index index, ComparisonOperator comparisonOperator){
		this.conditionList.add(
			new ConditionContract(conditionClassType, null, new Condition(index.getKey(), index.getValue(), LogicalOperator.AND, comparisonOperator))
		);

		return this;
	}

	public Clause and(Clause clause){
		clause.setLogicalOperator(LogicalOperator.AND);
		this.conditionList.add(new ConditionContract(clauseClassType, clause, null));

		return this;
	}

	public Clause or(String key, Object value, ComparisonOperator comparisonOperator){
		this.conditionList.add(new ConditionContract(conditionClassType, null, new Condition(key, value, LogicalOperator.OR, comparisonOperator)));

		return this;
	}

	public Clause or(Index index, ComparisonOperator comparisonOperator){
		this.conditionList.add(
			new ConditionContract(conditionClassType, null, new Condition(index.getKey(), index.getValue(), LogicalOperator.OR, comparisonOperator))
		);

		return this;
	}

	public Clause or(Clause clause){
		clause.setLogicalOperator(LogicalOperator.OR);
		conditionList.add(new ConditionContract(clauseClassType, clause, null));

		return this;
	}

	public String toSQL(){
		StringBuffer sbr = new StringBuffer();
		if(conditionList != null){
			for(int i = 0 ; i < this.conditionList.size() ; i++){
				ConditionContract conditionContract = conditionList.get(i);

				if(conditionContract.getClassType().equals(conditionClassType)){

					Condition condition = conditionContract.getCondition();

					if(i != 0){
						sbr.append(" ")
						   .append(condition.getLogicalOperator().name())
						   .append(" ");
					}

					Object value = condition.getValue();
					if( value instanceof Date ||
						value instanceof Calendar ||
						value instanceof XMLGregorianCalendar
					){
						Date trimDay = null;
						try{
							if(value instanceof Date){
								trimDay = trimToDay.parse(trimToDay.format(value));
							}else if(value instanceof Calendar){
								trimDay = trimToDay.parse(trimToDay.format(((Calendar)value).getTime()));
							}else{
								trimDay = trimToDay.parse(trimToDay.format(((XMLGregorianCalendar)value).toGregorianCalendar().getTime()));
							}
						}catch (Exception e) {

						}

						if(ComparisonOperator.EQ.equals(condition.getComparisonOperator())){
							Calendar start = Calendar.getInstance();
							Calendar end = Calendar.getInstance();

							start.setTime(trimDay);
							end.setTime(trimDay);
							end.add(Calendar.DATE, 1);
							end.add(Calendar.SECOND, -1);

							sbr.append("(")
							   .append(condition.getKey()).append(" >= ").append(cfDateFormat.format(start.getTime()))
							   .append(" AND ")
							   .append(condition.getKey()).append(" <= ").append(cfDateFormat.format(end.getTime()))
							   .append(")");
						}else{
							sbr.append(condition.getKey()).append(" ").append(convertComparisonOperator(condition.getComparisonOperator())).append(" ")
							   .append(cfDateFormat.format(trimDay));
						}
					}else{
						sbr.append(condition.getKey()).append(" ").append(convertComparisonOperator(condition.getComparisonOperator())).append(" ");

						if( value instanceof String ||
							value instanceof Character
						){
							sbr.append("'").append(value.toString()).append("' ");
						}else if(value instanceof Integer ||
								 value instanceof Double ||
								 value instanceof Float	||
								 value instanceof Boolean
						){
							sbr.append(value.toString());
						}else{
							throw new RuntimeException("CF沒設定type:" + value.getClass().getSimpleName() + "的轉換方式");
						}
					}
				}else if(conditionContract.getClassType().equals(clauseClassType)){
					Clause clause = conditionContract.getClause();

					if(i != 0){
						sbr.append(" ")
						   .append(clause.getLogicalOperator().name())
						   .append(" ");
					}

					sbr.append("( ")
					   .append(clause.toSQL())
					   .append(" )");

				}else{
					throw new RuntimeException("錯誤");
				}
			}
		}

		return sbr.toString();
	}

	private String convertComparisonOperator(ComparisonOperator comparisonOperator){
		switch(comparisonOperator){
			case EQ:
				return "=";
			case GE:
				return ">=";
			case GT:
				return ">";
			case LE:
				return "<=";
			case LT:
				return "<";
			default:
				throw new RuntimeException("未定義ComparisonOperator." + comparisonOperator.name() + "的轉換方式");
		}
	}

	public List<String> getAllColumn(){
		List<String> columnList = new ArrayList<String>();

		if(conditionList != null){
			for(int i = 0 ; i < this.conditionList.size() ; i++){
				ConditionContract conditionContract = conditionList.get(i);

				if(conditionContract.getClassType().equals(conditionClassType)){

					Condition condition = conditionContract.getCondition();

					columnList.add(condition.getKey());

				}else if(conditionContract.getClassType().equals(clauseClassType)){
					Clause clause = conditionContract.getClause();

					columnList.addAll(clause.getAllColumn());
				}else{
					throw new RuntimeException("錯誤");
				}
			}
		}
		return columnList;
	}
}