package com.made.domain;

import java.util.ArrayList;
import java.util.List;

public class TableData {

	/** 表格名稱 */
	private String tableName;
	
	/** 表格敘述 */
	private String tableDescribe;
	
	/** 主鍵 */
	private String PK;
	
	/** 索引鍵 */
	private String indexKey;
	
	/** 表格欄位的List */
	private List<ColumnData> columnDataList = new ArrayList<ColumnData>();

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPK() {
		return PK;
	}

	public void setPK(String pK) {
		PK = pK;
	}

	public String getIndexKey() {
		return indexKey;
	}

	public void setIndexKey(String indexKey) {
		this.indexKey = indexKey;
	}

	public List<ColumnData> getColumnDataList() {
		return columnDataList;
	}

	public void setColumnDataList(ColumnData columnData) {
		this.columnDataList.add(columnData);
	}

	public String getTableDescribe() {
		return tableDescribe;
	}

	public void setTableDescribe(String tableDescribe) {
		this.tableDescribe = tableDescribe;
	}

	public void setColumnDataList(List<ColumnData> columnDataList) {
		this.columnDataList = columnDataList;
	}
}
