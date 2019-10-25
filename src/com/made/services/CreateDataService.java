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
	
	private List<ColumnData> tIndexDataList;
	
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
		this.tIndexDataList = tTableData.getIndexDataList();
				
		// key 昰MSSQL value昰 Oracle
		tSQLDataType.put("datetime", "timestamp");
		tSQLDataType.put("float", "float");
		tSQLDataType.put("decimal", "decimal");
		tSQLDataType.put("varchar", "varchar");
		tSQLDataType.put("CHAR", "CHAR");
		tSQLDataType.put("nvarchar", "nvarchar2");
		tSQLDataType.put("nchar", "nchar");
		tSQLDataType.put("int", "Number");	
		tSQLDataType.put("ntext", "clob");
		tSQLDataType.put("bigint", "Number");// oracel 要用 Number(19)
	}
	
	/**
	  * 將table轉成SQL指令.
	  * @param pMap tables資料
	  * @author 伸儒
	  * @throws Exception 
	  */
	public void sendTableMapToSQL() throws Exception {
		
		if("MSSQL".equals(tSQLType)) {
			this.returnSqlOfMssql();
		}else if("ORACLE".equals(tSQLType)) {
			this.returnSqlOfOracle();
		}
	}
	
	/**
	 * 產生MSSQL 的SQL指令
	 * @author 伸儒
	 * @throws Exception
	 */
	public void returnSqlOfMssql() throws Exception {
		StringBuffer tTableSQL = new StringBuffer();
		String tCreateTableSQL = "";
		
		String tIfNotExists = "IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='";
		
		for(int i=0; i < tColumnDataList.size(); i++) {
			ColumnData tColumnData = (ColumnData) tColumnDataList.get(i);
			String tColumnName = tColumnData.getColumnName();
			String tDescribe = tColumnData.getDescribe();
			String tDataType = tColumnData.getDataType();
			String tColumnLength = tColumnData.getColumnLength();
			String tMark = "Y".equals(tColumnData.getMark()) ? "NOT NULL" : "";
			
			if(tSQLDataType.containsKey(tDataType)) {
				tTableSQL.append(tChaneLine);
				tTableSQL.append("-- " + tColumnName + " " + tDescribe + tChaneLine);
				tTableSQL.append(tIfNotExists + tableName + "' AND COLUMN_NAME = '" + tColumnName + "')" + tChaneLine);
				tTableSQL.append("BEGIN" + tChaneLine + tTab);
				
				if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
					tTableSQL.append("ALTER TABLE [" + tableName + "] ADD [" + tColumnName + "] " + tDataType + " " + tMark);
				}else {
					tTableSQL.append("ALTER TABLE [" + tableName + "] ADD [" + tColumnName + "] " + tDataType + "("+tColumnLength+") " + tMark);
				}
				
				tTableSQL.append(tChaneLine);
				tTableSQL.append("END");
				tTableSQL.append(tChaneLine);
				
				tTableSQL.append(tIfNotExists + tableName + "' AND COLUMN_NAME = '" + tColumnName + "' AND DATA_TYPE = '" + tDataType + "'"); 
				if(!("int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType))) {

					if("MAX".equals(tColumnLength.toUpperCase())) {
						// MSSQL 長度建立為max時,他會建-1進去
						tTableSQL.append(" and CHARACTER_MAXIMUM_LENGTH = '" + "-1" + "'");
					}else {
						tTableSQL.append(" and CHARACTER_MAXIMUM_LENGTH >= '" + tColumnLength + "'");
					}
				}
				tTableSQL.append(")");
				
				tTableSQL.append(tChaneLine);
				tTableSQL.append("BEGIN" + tChaneLine + tTab);
				
				if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
					tTableSQL.append("ALTER TABLE [" + tableName + "] ALTER COLUMN [" + tColumnName + "] " + tDataType + " " + tMark);
				}else {
					tTableSQL.append("ALTER TABLE [" + tableName + "] ALTER COLUMN [" + tColumnName + "] " + tDataType + "("+tColumnLength+") " + tMark);
				}
				
				tTableSQL.append(tChaneLine);
				tTableSQL.append("END");
				
				if(i == 0) {
					tCreateTableSQL += "-- " + tableName + " " + tableDescribe + " : Unique: " + PK + tChaneLine;
					tCreateTableSQL += tIfNotExists + tableName + "')";
					tCreateTableSQL += tChaneLine;
					tCreateTableSQL += "BEGIN";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "CREATE TABLE [" + tableName + "]";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "(";
					tCreateTableSQL += tChaneLine + tTab + tTab;
					
					// 如果有主鍵,create指令就先建主鍵的欄位
					if(tIndexDataList.size() > 0) {
						
						for(int j=0; j < tIndexDataList.size(); j++) {
							ColumnData tIndexData = (ColumnData) tIndexDataList.get(j);
							String tIndexName = tIndexData.getColumnName();
							String tIndexDescribe = tIndexData.getDescribe();
							String tIndexDataType = tIndexData.getDataType();
							String tIndexLength = tIndexData.getColumnLength();
							String tIndexMark = "NOT NULL"; // 索引不能為null

							tCreateTableSQL += "-- " + tIndexName + " " + tIndexDescribe;
							tCreateTableSQL += tChaneLine + tTab + tTab;
							
							if( "int".equals(tIndexDataType) || "datetime".equals(tIndexDataType) || "ntext".equals(tIndexDataType) || "bigint".equals(tIndexDataType)) {
								tCreateTableSQL += "[" + tIndexName + "] " + tIndexDataType + " " + tIndexMark;
							}else {
								tCreateTableSQL += "[" + tIndexName + "] " + tIndexDataType + "(" + tIndexLength + ") " + tIndexMark;
							}
							
							// 如果還有下一行就先加換行
							if(j < tIndexDataList.size()-1) {
								tCreateTableSQL += " ,";
								tCreateTableSQL += tChaneLine + tTab + tTab;
							}
						}
					}else {
						tCreateTableSQL += "--" + tColumnName + " " + tDescribe;
						tCreateTableSQL += tChaneLine + tTab + tTab;
						
						if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
							tCreateTableSQL += "[" + tColumnName + "] " + tDataType + " " + tMark;
						}else {
							tCreateTableSQL += "[" + tColumnName + "] " + tDataType + "(" + tColumnLength + ")" + tMark;
						}
					}

				
					if(!"".equals(indexKey) && !"".equals(PK)) {
						tCreateTableSQL += ",";
						tCreateTableSQL += tChaneLine + tTab + tTab;
						tCreateTableSQL += "CONSTRAINT " + indexKey + " PRIMARY KEY (" + PK + ")";
					}
					
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += ")";
					tCreateTableSQL += tChaneLine;
					tCreateTableSQL += "END";
				}
			}else {
				throw new Exception("表格名稱 : " + tableName + " 欄位明稱 : " + tColumnName + " 的型態 "+tDataType+" 昰錯誤的");
			}
		}
		
		tTableSQL.insert(0, tCreateTableSQL);
		tTableSQL.append(tChaneLine + tChaneLine);
		
		sqlResult += tTableSQL;
	}
	
	/**
	 * 產生Oracle 的SQL指令
	 * @author 伸儒
	 * @throws Exception
	 */
	public void returnSqlOfOracle() throws Exception {

		StringBuffer tTableSQL = new StringBuffer();
		String tCreateTableSQL = "";

		for(int i=0; i < tColumnDataList.size(); i++) {
			ColumnData tColumnData = (ColumnData) tColumnDataList.get(i);
			String tColumnName = tColumnData.getColumnName();
			String tDescribe = tColumnData.getDescribe();
			String tDataType = tColumnData.getDataType();
			String tColumnLength = tColumnData.getColumnLength().toLowerCase();
			tColumnLength = "max".equals(tColumnLength) ? "4000" : tColumnLength;
			tColumnLength = Integer.parseInt(tColumnLength)>4000 ? "4000" : tColumnLength;
			
			String tMark = "Y".equals(tColumnData.getMark()) ? "NOT NULL" : "";
			
			if(tSQLDataType.containsKey(tDataType)) {
				
				String tOracleDataType = tSQLDataType.get(tDataType);
				
				tTableSQL.append(tChaneLine + tChaneLine + tTab);
				tTableSQL.append("-- " + tColumnName + " " + tDescribe);
				tTableSQL.append(tChaneLine + tTab);
				
				tTableSQL.append("select count(1) into v_cnt1 from user_tab_cols where table_name = upper('" + tableName + "') and column_name = upper('" + tColumnName + "');");
				tTableSQL.append(tChaneLine + tTab);
				
				if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
					tTableSQL.append("v_sql1 := 'alter table " + tableName + " add " + tColumnName + " " + tOracleDataType + " " + tMark + "';");
				}else {
					tTableSQL.append("v_sql1 := 'alter table " + tableName + " add " + tColumnName + " " + tOracleDataType + "("+tColumnLength+") " + tMark + "';");
				}
				
				tTableSQL.append(tChaneLine + tChaneLine + tTab);
				
				tTableSQL.append("if v_cnt1 = 0 then");
				tTableSQL.append(tChaneLine + tTab + tTab);
				
				tTableSQL.append("execute immediate v_sql1;");
				tTableSQL.append(tChaneLine + tTab);
				
				tTableSQL.append("end if;");
				tTableSQL.append(tChaneLine + tChaneLine + tTab);
				
				tTableSQL.append("select count(1) into v_cnt2 from user_tab_cols where table_name = upper('" + tableName + "') and column_name = upper('" + tColumnName + "') and data_type like upper('" + tOracleDataType + "%')");
				
				if( !("int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType))) {
					tTableSQL.append(" and char_col_decl_length >= '" + tColumnLength + "'");
				}
				tTableSQL.append(";");
				
				tTableSQL.append(tChaneLine + tTab);
				
				if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
					tTableSQL.append("v_sql2 := 'alter table " + tableName + " modify " + tColumnName + " " + tOracleDataType + "';");
				}else {
					tTableSQL.append("v_sql2 := 'alter table " + tableName + " modify " + tColumnName + " " + tOracleDataType + "("+tColumnLength+")" + "';");
				}
				tTableSQL.append(tChaneLine + tChaneLine + tTab);
				
				tTableSQL.append("if v_cnt2 = 0 then");
				tTableSQL.append(tChaneLine + tTab + tTab);
				
				tTableSQL.append("execute immediate v_sql2; ");
				tTableSQL.append(tChaneLine + tTab);
				
				tTableSQL.append("end if;");
				
				if(i == 0) {
					tCreateTableSQL += "-- " + tableName + " " + tableDescribe + " : Unique: " + PK + tChaneLine;
					tCreateTableSQL += "create or replace procedure sp_" + tableName;
					tCreateTableSQL += tChaneLine;
					tCreateTableSQL += "is";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "v_cnt number;";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "v_sql varchar2(4000);";
					tCreateTableSQL += tChaneLine + tChaneLine + tTab;
					
					tCreateTableSQL += "v_cnt1 number;";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "v_sql1 varchar2(4000);";
					tCreateTableSQL += tChaneLine + tChaneLine + tTab;
					
					tCreateTableSQL += "v_cnt2 number;";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "v_sql2 varchar2(4000);";
					tCreateTableSQL += tChaneLine + tChaneLine;
					
					tCreateTableSQL += "begin";
					tCreateTableSQL += tChaneLine + tChaneLine + tTab;
					tCreateTableSQL += "select count(1) into v_cnt from user_tab_cols where table_name = upper('" + tableName + "');";
					tCreateTableSQL += tChaneLine + tChaneLine + tTab;
					
					tCreateTableSQL += "v_sql := 'create table " + tableName + "( ";
					
					// 如果有主鍵,create指令就先建主鍵的欄位
					if(tIndexDataList.size() > 0) {
						
						for(int j=0; j < tIndexDataList.size(); j++) {
							ColumnData tIndexData = (ColumnData) tIndexDataList.get(j);
							String tIndexName = tIndexData.getColumnName();
							String tIndexDescribe = tIndexData.getDescribe();
							String tIndexDataType = tIndexData.getDataType();
							String tOracleIndexDataType = tSQLDataType.get(tIndexDataType);
							String tIndexLength = tIndexData.getColumnLength();
							String tIndexMark = "NOT NULL"; // 索引不能為null
							
							if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
								tCreateTableSQL += tIndexName + " " + tOracleIndexDataType + " " + tIndexMark;
							}else {
								tCreateTableSQL += tIndexName + " " + tOracleIndexDataType + "(" + tIndexLength + ") " + tIndexMark;
							}
							
							// 如果還有下一行就先加換行
							if(j < tIndexDataList.size()-1) {
								tCreateTableSQL += " ,";
								// tCreateTableSQL += tChaneLine + tTab + tTab;
							}
						}
					}else {
						if( "int".equals(tDataType) || "datetime".equals(tDataType) || "ntext".equals(tDataType) || "bigint".equals(tDataType)) {
							tCreateTableSQL += tColumnName + " " + tOracleDataType + " " + tMark;
						}else {
							tCreateTableSQL += tColumnName + " " + tOracleDataType + "(" + tColumnLength + ")" + tMark;
						}
					}
					
					if(!"".equals(indexKey) && !"".equals(PK)) {
						tCreateTableSQL += ",";
						tCreateTableSQL += "CONSTRAINT " + indexKey + " PRIMARY KEY (" + PK + ")";
					}
					
					tCreateTableSQL += ")';";
					tCreateTableSQL += tChaneLine + tChaneLine + tTab;
					
					tCreateTableSQL += "if v_cnt = 0 then";
					tCreateTableSQL += tChaneLine + tTab + tTab;
					tCreateTableSQL += "execute immediate v_sql;";
					tCreateTableSQL += tChaneLine + tTab;
					tCreateTableSQL += "end if;";
				}
			}else {
				throw new Exception("表格名稱 : " + tableName + " 欄位明稱 : " + tColumnName + " 的型態 "+tDataType+" 昰錯誤的");
			}
		}
		
		tTableSQL.append(tChaneLine + tChaneLine);
		tTableSQL.append("end sp_" + tableName + ";");
		tTableSQL.append(tChaneLine + tChaneLine);
		
		tTableSQL.append("/"); // '/'這個是Oracle的go
		tTableSQL.append(tChaneLine + tChaneLine);
		
		tTableSQL.append("exec sp_" + tableName + ";"); // 執行procedure
		tTableSQL.append(tChaneLine + tChaneLine);
		
		tTableSQL.append("drop procedure sp_" + tableName + ";"); // 刪除procedure

		tTableSQL.insert(0, tCreateTableSQL);
		tTableSQL.append(tChaneLine + tChaneLine);
		
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
						case "bigint":
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
				case "bigint":
					tDataType = "string";
					break;
			}

			tPojo += tTab + "/** " + tDescribe + " */" + tChaneLine;
			tPojo += tTab + "private " + tDataType + " " + tColumnName + ";";
		}
		tPojo += tChaneLine + tChaneLine + "}"; 
			   
		return tPojo;
	}
}
