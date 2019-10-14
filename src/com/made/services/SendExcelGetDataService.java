package com.made.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.made.domain.ColumnData;
import com.made.domain.TableData;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * 將Table的excel轉成createSQL指令,pojo物件,hibernateXml
 * @author 伸儒
 */
public class SendExcelGetDataService {

	String tChaneLine = "\r\n"; // 換行
	
	String tSQLName = "CreateSQL"; // SQL檔名
	
	String tPackageName = "createDate"; // 存放資料夾
	
	String tPojoName = "pojo"; // pojo資料夾
	
	String tHibernameName = "hibernateXml"; // hibernate資料夾
	
	// Excel檔案名稱
	public static String tFileName = "TableSkema.xls";
	
	// 檔案產生路徑
	public static String path = "C:/Users/ShenLu/Desktop/ShenLuModule/";
	
//	private static String tTestPath = "";
	
	public static void main(String[] args) throws Exception {
		
		// jar檔路徑
		path = getAppPath(SendExcelGetDataService.class);
		
		System.out.println(path + "/" + tFileName);
		
		SendExcelGetDataService obj = new SendExcelGetDataService();
		
		String tExcelPath = path + "/" + tFileName;
		
		// 傳入Excel路徑
		File file = new File(tExcelPath);

		// 將Excel 內的table資料 轉成 Map
		Map<Integer, TableData> tData = obj.readExcel(file);
		
		// 處理資料的地方		
		obj.runTableDoWork("MSSQL",tData);
		obj.runTableDoWork("ORACLE",tData);
		
		System.out.println("已完成!");
	}

	public void runTableDoWork(String tSQLType,Map<Integer, TableData> pData) throws Exception {

		Set<Integer> tKey = pData.keySet();		
		Iterator<Integer> it = tKey.iterator();
		
		String tSQL = "";
		while(it.hasNext()) {
			
			Object tableKey = it.next();
			
			// 先取得table資料
			TableData tTableData = (TableData) pData.get(tableKey);
			
			// 將table 資料 轉成 SQL 指令
			CreateDataService tCreateDataService = new CreateDataService(tSQLType,tTableData);
			tCreateDataService.sendTableMapToSQL();

			tSQL += tCreateDataService.sqlResult + tChaneLine;
			
			// 將table 轉成 hibernate.xml (已完成,但目前不使用)
			//String tHibernateXml = tCreateDataService.sendTableMapToHibernateXml();
			//this.madeCreateSQLToTxt(path + "/" + tPackageName + "/" + tHibernameName + "/" + tTableData.getTableName() + ".hbm.xml",tHibernateXml);
			
			// 將table 轉成 Pojo類別
			String tPojoClass = tCreateDataService.sendTableMapToPojo();
			this.madeCreateSQLToTxt(path + "/" + tPackageName + "/" + tPojoName +"/" + tTableData.getTableName() + ".java",tPojoClass);
		}
		
		// 將SQL檔案產生
		this.madeCreateSQLToTxt(path + "/" + tPackageName + "/" + tSQLName +"_"+ tSQLType + ".sql", tSQL);
	}
	
	
	public Map<Integer, TableData> readExcel(File file) {
		
		Map<Integer, TableData> tData = new HashMap<Integer, TableData>();
		
		// 建立輸入流，讀取Excel
		try {
			InputStream is = new FileInputStream(file.getAbsoluteFile());
			
			// jxl提供的Workbook類
			Workbook wb = Workbook.getWorkbook(is);
			
			// Excel的頁籤數量
			int sheet_size = wb.getNumberOfSheets();
							
			for(int index=0; index < sheet_size; index++) {
				
				// 每個頁籤建立一個Sheet物件
				Sheet sheet = wb.getSheet(index);
				
//				Map tTable = new HashMap();
				TableData tTableData = new TableData();
						
				// sheet.getRows()返回該頁的總行數
				for(int i=0; i < sheet.getRows(); i++) {
					
					//Map tRow = new HashMap();
					ColumnData tColumnData = new ColumnData();

					// sheet.getColumns()返回該頁的總列數
					for(int j=0; j < sheet.getColumns(); j++) {
						
						String cellinfo = sheet.getCell(j, i).getContents();
						if(j==1 && i==0) {
							tTableData.setTableName(cellinfo);
						}else if(j==1 && i==1) {
							tTableData.setPK(cellinfo);
						}else if(j==3 && i==1) {
							tTableData.setIndexKey(cellinfo);
						}else if(j==3 && i==0) {
							tTableData.setTableDescribe(cellinfo);
						}else if(i > 2) {
							if(j==0) {
								tColumnData.setIndex(Integer.parseInt(cellinfo));
							}else if(j==1) {
								tColumnData.setColumnName(cellinfo);
							}else if(j==2) {
								tColumnData.setDescribe(cellinfo);
							}else if(j==3) {
								tColumnData.setDataType(cellinfo);
							}else if(j==4) {
								tColumnData.setColumnLength(cellinfo);
							}else if(j==5) {
								tColumnData.setMark(cellinfo);
							}
						}
					}
					
					if(i >2) {
						//tTable.put((i-3), tColumnData);
						tTableData.setColumnDataList(tColumnData);
					}
				}
				//tData.put(index, tTable);
				tData.put(index, tTableData);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tData;	
	}
	
	/**
	 * 將檔案產生
	 */
	public void madeCreateSQLToTxt(String tPath,String tCreateSQL) {
		
		// 先判斷有沒有目錄資料夾 沒有就建立
		File file = new File(tPath);
		
		// 檔案如果存在先刪除
		if(file.exists()) {
			file.delete();
		}
		
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		try {
			FileWriter l_log_file_write = new FileWriter(tPath, true);
			l_log_file_write.write(tCreateSQL);
			l_log_file_write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  取出class檔的路徑不含class名稱
	 */
    public static String getAppPath(Class<SendExcelGetDataService> cls){
        //檢查使用者傳入的引數是否為空 cls:class com.util.PropertiesUtil
        if(cls==null) {
        	 throw new java.lang.IllegalArgumentException("引數不能為空！");
        }
        
        ClassLoader loader=cls.getClassLoader();
        //獲得類的全名，包括包名 //com.util.PropertiesUtil.class
        String clsName=cls.getName()+".class";
        //獲得傳入引數所在的包 package com.util
        Package pack=cls.getPackage();
        String path="";//包名相對應路徑
        //如果不是匿名包，將包名轉化為路徑
        if(pack!=null){
            String packName=pack.getName();//com.util
            //此處簡單判定是否是Java基礎類庫，防止使用者傳入JDK內建的類庫
            if(packName.startsWith("java.")||packName.startsWith("javax.")){
        	   throw new java.lang.IllegalArgumentException("請不要傳送系統內建類！");
            }
            
            //在類的名稱中，去掉包名的部分，獲得類的檔名 PropertiesUtil.class
            clsName=clsName.substring(packName.length()+1);
            //判定包名是否是簡單包名，如果是，則直接將包名轉換為路徑
            if(packName.indexOf(".")<0){
            	path=packName+"/";
            }
            else{//否則按照包名的組成部分，將包名轉換為路徑
                int start=0,end=0;
                end=packName.indexOf(".");
                while(end!=-1){
                    path=path+packName.substring(start,end)+"/";
                    start=end+1;
                    end=packName.indexOf(".",start);
                }
                path=path+packName.substring(start)+"/"; //com/util/
            }
        }
        //呼叫ClassLoader的getResource方法，傳入包含路徑資訊的類檔名
        //file:/D:/Workspaces/springjdbc/bin/com/util/PropertiesUtil.class
        java.net.URL url =loader.getResource(path+clsName);
        //從URL物件中獲取路徑資訊
        //   /D:/Workspaces/springjdbc/bin/com/util/PropertiesUtil.class
        String realPath=url.getPath();
        //去掉路徑資訊中的協議名"file:"
        int pos=realPath.indexOf("file:");
        if(pos>-1){
        	realPath=realPath.substring(pos+5);
        }
        //去掉路徑資訊最後包含類檔案資訊的部分，得到類所在的路徑
        pos=realPath.indexOf(path+clsName);
        realPath=realPath.substring(0,pos-1);//  /D:/Workspaces/springjdbc/bin
        //如果類檔案被打包到JAR等檔案中時，去掉對應的JAR等打包檔名
        if(realPath.endsWith("!")){
        	realPath=realPath.substring(0,realPath.lastIndexOf("/"));
        }
            
      /*------------------------------------------------------------
       ClassLoader的getResource方法使用了utf-8對路徑資訊進行了編碼，當路徑
                    中存在中文和空格時，他會對這些字元進行轉換，這樣，得到的往往不是我們想要
                     的真實路徑，在此，呼叫了URLDecoder的decode方法進行解碼，以便得到原始的
                     中文及空格路徑
      -------------------------------------------------------------*/
       try{
          realPath=java.net.URLDecoder.decode(realPath,"utf-8");
       }catch(Exception e){
    	   throw new RuntimeException(e);
       }
       return realPath.substring(1, realPath.length());
    }
}
