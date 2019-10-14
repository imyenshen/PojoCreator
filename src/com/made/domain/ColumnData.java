package com.made.domain;

public class ColumnData {
	
	/** 欄位編號 */
	private int index;
	
	/** 欄位名稱 */
	private String columnName;
	
	/** 欄位敘述 */
	private String describe;
	
	/** 資料型態 */
	private String dataType;
	
	/** 欄位長度 */
	private String columnLength;
	
	/** 備註 */
	private String mark;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(String columnLength) {
		this.columnLength = columnLength;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
}
