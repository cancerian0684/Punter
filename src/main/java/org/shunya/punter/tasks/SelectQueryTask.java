package org.shunya.punter.tasks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.utils.StringUtils;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

@PunterTask(author="munishc",name="GenTableDDLTask",documentation= "src/main/resources/docs/GenTableDDLTask.html")
public class SelectQueryTask extends Tasks {
	@InputParam(required = true,description="jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1") 
	private String conURL;
	@InputParam(required = true,description="DAISY2")
	private String username;
	@InputParam(required = true,description="Welcome1")
	private String password;
	@InputParam(required = true,description="select * from dual")
	private String query;
	@InputParam(required = true,description="File Name")
	@OutputParam
	private String outFileName;
	
	public static void main(String[] args) {
		SelectQueryTask sqt=new SelectQueryTask();
		sqt.run();
	}
	@Override
	public boolean run() {
		boolean status=false;
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(conURL, username, password);
	//		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@xldn2738dor.ldn.swissbank.com:1523:DELSHRD1", "AISDB4", "Welcome1");
			conn.setReadOnly(true);
			Scanner stk = new Scanner(query).useDelimiter("\r\n|\n\r|\r|\n");
			String sheetName="Sheet";
			Workbook wb = new HSSFWorkbook();
		    while (stk.hasNext()) {
				String token = stk.next().trim();
				if(token.trim().isEmpty())
					continue;
				if(token.startsWith("--")){
					sheetName=token;
					continue;
				}
				LOGGER.get().log(Level.INFO, sheetName);
				Sheet sheet = wb.createSheet(sheetName);
				if(!token.isEmpty()){
					Statement s = conn.createStatement();
					s.setQueryTimeout(2*60);
					ResultSet rs=s.executeQuery(token);
					int columns = createXLSFile(rs,sheet,wb);
					s.close();
					LOGGER.get().log(Level.FINE, "Resizing Excel Columns.");
					for(int i=0;i<columns;i++){
						sheet.autoSizeColumn(i);
					}
					LOGGER.get().log(Level.INFO, "\n-------------------------************-------------------------\n");
				}
		    }
			conn.close();
			if(outFileName!=null&&!outFileName.isEmpty()){
				LOGGER.get().log(Level.INFO, "Writing Query output to XLS File : "+outFileName);
				FileOutputStream fileOut = new FileOutputStream(outFileName);
			    wb.write(fileOut);
			    fileOut.close();
			}
			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
	 return status;
	 }
	
	public int createXLSFile(ResultSet rs, Sheet sheet, Workbook wb) throws Exception{
	    ResultSetMetaData metaData = rs.getMetaData();
		int columns=metaData.getColumnCount();
		List<String> columnNames=new ArrayList<String>();
		String out="";
		Row row = sheet.createRow((short)0);
		for (int i = 1; i <= columns; i++) {
			columnNames.add(metaData.getColumnName(i));
//			System.out.println(metaData.getColumnTypeName(i)+ " - "+metaData.getColumnName(i));
			out+=metaData.getColumnName(i)+"\t";
			Cell cell=row.createCell(i-1);
			cell.setCellValue(metaData.getColumnName(i));
			cell.setCellStyle(getHeaderDataCellStyle( (HSSFWorkbook) wb));
		}
		LOGGER.get().log(Level.INFO, out);
		rs.setFetchSize(20);
		int rowCount=0;
		while(rs.next()){
			out="";
			rowCount++;
			row = sheet.createRow((short)rowCount);
			for (int i = 1; i <= columns; i++) {
				out+=rs.getString(i)+"\t";
				setAppropriateCellValue(rs, i, row, metaData,wb);
			}
			LOGGER.get().log(Level.INFO, out);
		}
		rs.close();
		return columns;
	}
	public void setAppropriateCellValue(ResultSet rs,int column,Row row, ResultSetMetaData metaData, Workbook wb) throws Exception{
		Cell cell=row.createCell(column-1);
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle cellStyle = wb.createCellStyle();
		int columnType=metaData.getColumnType(column);
		if(rs.getObject(column)!=null)
		switch (columnType) {
		case 12:
			cell.setCellValue(rs.getString(column));
			break;
		case Types.INTEGER:
		case Types.NUMERIC:
			cell.setCellValue(rs.getDouble(column));
			break;
		case Types.DATE:
		case Types.TIMESTAMP:
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"));
			cell.setCellStyle(cellStyle);
			cell.setCellValue(rs.getDate(column));
			break;
		default:
			cell.setCellValue(rs.getString(column));
			break;
		}
	}
	public HSSFCellStyle getHeaderDataCellStyle(HSSFWorkbook wb) {
		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Frutiger 45 Light");
		HSSFCellStyle headerDataCellStyle=wb.createCellStyle();
		headerDataCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		headerDataCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		headerDataCellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		headerDataCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		headerDataCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		headerDataCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		headerDataCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		headerDataCellStyle.setFont(font);
		return headerDataCellStyle;
	}
}	