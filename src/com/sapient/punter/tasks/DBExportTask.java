package com.sapient.punter.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="DBExportTask",documentation="com/sapient/punter/tasks/docs/DBExportTask.html") 
public class DBExportTask extends Tasks {
	@InputParam(required = true,description="Get expCommand help from DBExport Task.") 
	private String expCommand;
	@InputParam(required = false,description="P:/dump/daisy")
	private String outFolderName;

	@OutputParam
	private String dumpFile;

	@Override
	public boolean run() {
		boolean status=false;
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
			dumpFile=outFolderName+"/"+sdf.format(new Date())+".dmp";
			LOGGER.get().log(Level.INFO, expCommand+" file="+dumpFile);
			java.lang.Process p = Runtime.getRuntime().exec(expCommand+" file="+dumpFile);
			status=startOutputAndErrorReadThreads(p.getInputStream(),p.getErrorStream());
			p.waitFor();
			LOGGER.get().log(Level.INFO,"DB Backup taken successfully to file : "+dumpFile);
//			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, e.getMessage());
		}
		 return status;
	}
	private static boolean startOutputAndErrorReadThreads(InputStream processOut, InputStream processErr) throws Exception
	{
		StringBuffer fCmdOutput = new StringBuffer();
		AsyncStreamReader  fCmdOutputThread = new AsyncStreamReader(processOut, fCmdOutput, new myLogger(LOGGER.get()), "OUTPUT");		
		fCmdOutputThread.start();
		
		StringBuffer fCmdError = new StringBuffer();
		AsyncStreamReader  fCmdErrorThread = new AsyncStreamReader(processErr, fCmdError, new myLogger(LOGGER.get()), "ERROR");
		fCmdErrorThread.start();
		fCmdOutputThread.join();
		fCmdErrorThread.join();
		
		if(fCmdError.toString().contains("Export terminated successfully")||fCmdOutput.toString().contains("Export terminated successfully")){
			return true;
		}else{
			return false;
		}
//		LOGGER.get().log(Level.INFO, fCmdErrorThread.getBuffer());
	}
	}
	interface ILogDevice
	{
		public void log(String str);
	}
	class myLogger implements ILogDevice
	{
		private Logger logger;
		public myLogger(Logger logger) {
			this.logger=logger;
		}
		@Override
		public void log(String str) {
			logger.log(Level.INFO, str);
		}
		
	}

	class AsyncStreamReader extends Thread
	{
		private StringBuffer fBuffer = null;
		private InputStream fInputStream = null;
		private String fThreadId = null;
		private boolean fStop = false;
		private ILogDevice fLogDevice = null;
		
		private String fNewLine = null;
		
		public AsyncStreamReader(InputStream inputStream, StringBuffer buffer, ILogDevice logDevice, String threadId)
		{
			fInputStream = inputStream;
			fBuffer = buffer;
			fThreadId = threadId;
			fLogDevice = logDevice;
			
			fNewLine = System.getProperty("line.separator");
		}	
		
		public String getBuffer() {		
			return fBuffer.toString();
		}
		
		public void run()
		{
			try {
				readCommandOutput();
			} catch (Exception ex) {
				//ex.printStackTrace(); //DEBUG
			}
		}
		
		private void readCommandOutput() throws IOException
		{		
			BufferedReader bufOut = new BufferedReader(new InputStreamReader(fInputStream));		
			String line = null;
			while ( (fStop == false) && ((line = bufOut.readLine()) != null) )
			{
				fBuffer.append(line + fNewLine);
				printToDisplayDevice(line);
			}		
			bufOut.close();
//			printToConsole("END OF: " + fThreadId); //DEBUG
		}
		
		public void stopReading() {
			fStop = true;
		}
		
		private void printToDisplayDevice(String line)
		{
			if( fLogDevice != null )
				fLogDevice.log(line);
			else
			{
				if(fThreadId.equalsIgnoreCase("error"))
					printToConsole2(line);//DEBUG
				else{
					printToConsole(line);//DEBUG
					
				}
			}
		}
		
		private synchronized void printToConsole(String line) {
//			System.out.println(line);
			DBExportTask.LOGGER.get().log(Level.INFO, line);
		}
		
		private synchronized void printToConsole2(String line) {
			DBExportTask.LOGGER.get().log(Level.WARNING, line);
//			System.err.println(line);
		}
}