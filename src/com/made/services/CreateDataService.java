package com.made.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.made.domain.ColumnData;
import com.made.domain.TableData;

public class CreateDataService {
	
	private Map<String,String> tSQLDataType = new HashMap<String,String>();
		
	private String tSQLType;
	
	private String tableName;
	
	private String tableDescribe;
	
	private String PK;
	
	private String indexKey;
	
	private List<ColumnData> tColumnDataList;
	
	public String sqlResult = ""; // sql的指令
	
	String tChaneLine = "\r\n"; // 換行
	
	String tTab = "\t"; // tab健
		
	public CreateDataService(String tSQLType,TableData tTableData) {
		this.tSQLType = tSQLType;
		this.tableName = tTableData.getTableName();
		this.tableDescribe = tTableData.getTableDescribe();
		this.PK = tTableData.getPK();
		this.indexKey = tTableData.getIndexKey();
		this.tColumnDataList = tTableData.getColumnDataList();
				
		// key 昰MSSQL value昰 Oracle
		tSQLDataType.put("datetime", "timestamp");
		tSQLDataType.put("float", "float");
		tSQLDataType.put("decimal", "decimal");
		tSQLDataType.put("varchar", "varchar");
		tSQLDataType.put("CHAR", "CHAR");
		tSQLDataType.put("nvarchar", "nvarchar2");
		tSQLDataType.put("nchar", "nchar");
		tSQLDataType.put("int", "integer");	
		tSQLDataType.put("ntext", "clob");	
	}
	
	/**
	  * 將table轉成SQL指令.
	  * @param pMap tables資料
	  * @author 伸儒
	 * @throws Exception 
	  */
	public void sendTableMapToSQL() throws Exception {
		
		String tTableSQL ="";
		if("MSSQL".equals(tSQLType)) {
			tTableSQL = "-- " + tableName + " " + tableDescribe + " : Unique: " + PK + tChaneLine;
			tTableSQL += "CREATE TABLE [" + tableName + "]\r\n(\r\n";
		}else if("ORACLE".equals(tSQLType)) {
			tTableSQL = "-- " + tableName + " " + tableDescribe + " : Unique: " + PK + tChaneLine;
			tTableSQL = "CREATE TABLE " + tableName + "\r\n(\r\n";
		}
		
		for(int i=0; i < tColumnDataList.size(); i++) {
			ColumnData tColumnData = (ColumnData) tColumnDataList.get(i);
//			int tIndex = tColumnData.getIndex();
			String tColumnName = tColumnData.getColumnName();
			String tDescribe = tColumnData.getDescribe();
			String tDataType = tColumnData.getDataType();
			String tColumnLength = tColumnData.getColumnLength();
			String tMark = "Y".equals(tColumnData.getMark()) ? "NOT NULL" : "";

			tTableSQL +="\t-- " + tColumnName + " " + tDescribe + "\r\n";
		
			if( tSQLDataType.containsKey(tDataType)) {
				
				if("MSSQL".equals(tSQLType)) {
					
					if( "int".equals(tDataType)) {
						tTableSQL +="\t["+tColumnName+"] " + tDataType + " "+tMark + ","+ "\r\n";
					}else if( "datetime".equals(tDataType)) {
						tTableSQL +="\t["+tColumnName+"] " + tDataType + " "+tMark + ","+ "\r\n";
					}else if( "ntext".equals(tDataType)) {
						tTableSQL +="\t["+tColumnName+"] " + tDataType + " "+tMark + ","+ "\r\n";
					}else {
						tTableSQL +="\t["+tColumnName+"] "+tDataType+"("+tColumnLength+") " +tMark + ","+ "\r\n";
					}
					
				}else if("ORACLE".equals(tSQLType)) {
					
					String tOracleDataType = tSQLDataType.get(tDataType);
					
					if( "integer".equals(tOracleDataType)) {
						tTableSQL +="\t"+tColumnName+" " + tOracleDataType + " "+tMark + ","+ "\r\n";
					}else if( "timestamp".equals(tOracleDataType)) {
						tTableSQL +="\t"+tColumnName+" " + tOracleDataType + " "+tMark + ","+ "\r\n";
					}else if( "clob".equals(tOracleDataType)) {
						tTableSQL +="\t"+tColumnName+" " + tOracleDataType + " "+tMark + ","+ "\r\n";
					}else {
						tTableSQL +="\t"+tColumnName+" "+tOracleDataType+"("+tColumnLength+") " +tMark + ","+ "\r\n";
					}
					
				}
			}else {
				throw new Exception("表格名稱 : " + tableName + " 欄位明稱 : " + tColumnName + " 的型態 "+tDataType+" 昰錯誤的");
				
			}
		}
		
		if(!"".equals(indexKey)) {
			tTableSQL += "\tCONSTRAINT " +indexKey+ " PRIMARY KEY ([" +PK+"])";
		}
		tTableSQL += "\r\n);";
		tTableSQL += "\r\n \r\n";
		
		sqlResult += tTableSQL;
	}
	
	/**
	  * 將table轉成Hibernate xml指令.
	  * @param pMap tables資料
	  * @author 伸儒
	  * @throws Exception 
	  */
	public String sendTableMapToHibernateXml() throws Exception {
		
		if("ORACLE".equals(tSQLType)) {
			return "";
		}
		
		String hibernateXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + tChaneLine +
								     "<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">";
		hibernateXml += tChaneLine;
		hibernateXml += tChaneLine + "<hibernate-mapping>" + tChaneLine + tTab;
		hibernateXml += "<class lazy=\"false\" table=\"" + tableName + "\" name=\"\">";
		hibernateXml += tChaneLine + tChaneLine + tTab;
		
		hibernateXml += "<id name=\"" + this.PK + "\" column=\"" + this.PK + "\">";
		hibernateXml += tChaneLine + tTab + "  " + "<generator class=\"uuid\"/>";
		hibernateXml += tChaneLine + tTab;
		hibernateXml += "</id>";
		hibernateXml += tChaneLine + tChaneLine + tTab;
		
		for(int i=0; i < tColumnDataList.size(); i++) {
			ColumnData tColumnData = (ColumnData) tColumnDataList.get(i);
			String tColumnName = tColumnData.getColumnName();
			
			if(!this.PK.equals(tColumnName)) {
				String tMark = "Y".equals(tColumnData.getMark()) ? "true" : "false";
				String tDataType = tColumnData.getDataType();
				
				if( tSQLDataType.containsKey(tDataType)) {
					
					switch (tDataType) {
						case "int":
							tDataType = "integer";
							break;
						case "datetime":
							tDataType = "timestamp";
							break;
						case "nvarchar":
							tDataType = "string";
							break;
						case "float":
							tDataType = "double";
							break;
						case "decimal":
							tDataType = "double";
							break;
						case "varchar":
							tDataType = "string";
							break;
						case "CHAR":
							tDataType = "string";
							break;
						case "nchar":
							tDataType = "string";
							break;
						case "ntext":
							tDataType = "string";
							break;
					}
		
					hibernateXml += "<property name=\"" + tColumnName + "\" column=\"" + tColumnName + "\" not-null=\"" + tMark + "\" type=\"" + tDataType + "\"/>";
					hibernateXml += tChaneLine + tTab;
				}else {
					throw new Exception("表格名稱 : " + tableName + " 欄位明稱 : " + tColumnName + " 的型態 "+tDataType+" 昰錯誤的");
				}
			}
		}
		
		hibernateXml += tChaneLine + tTab;
		hibernateXml += "</class>";
		hibernateXml += tChaneLine + "</hibernate-mapping>";
		
		return hibernateXml;
	}
	
	/**
	  * 將table轉成pojo
	  * @param pMap tables資料
	  * @author 伸儒
	  * @throws Exception 
	  */
	public String sendTableMapToPojo() throws Exception {
		
		if("ORACLE".equals(tSQLType)) {
			return "";
		}
		
		String tPojo  = "/**" + tChaneLine;
			   tPojo += " *  "+ tableName + tChaneLine;
			   tPojo += " */"+ tChaneLine;
			   tPojo += "public class " + tableName + " extends PersistentObject {";
			   
		for(int i=0; i < tColumnDataList.size(); i++) {
			tPojo += tChaneLine + tChaneLine;
			ColumnData tColumnData = (ColumnData) tColumnDataList.get(i);
			String tDescribe = tColumnData.getDescribe();
			String tColumnName = tColumnData.getColumnName();
			String tDataType = tColumnData.getDataType();
			
			switch (tDataType) {
				case "int":
					tDataType = "int";
					break;
				case "datetime":
					tDataType = "Date";
					break;
				case "nvarchar":
					tDataType = "String";
					break;
				case "float":
					tDataType = "Double";
					break;
				case "decimal":
					tDataType = "Double";
					break;
				case "varchar":
					tDataType = "String";
					break;
				case "CHAR":
					tDataType = "String";
					break;
				case "nchar":
					tDataType = "String";
					break;
				case "ntext":
					tDataType = "String";
					break;
			}

			tPojo += tTab + "/** " + tDescribe + " */" + tChaneLine;
			tPojo += tTab + "private " + tDataType + " " + tColumnName + ";";
		}
		tPojo += tChaneLine + tChaneLine + "}"; 
			   
		return tPojo;
	}
}
