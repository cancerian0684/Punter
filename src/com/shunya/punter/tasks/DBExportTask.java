package com.shunya.punter.tasks;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="DBExportTask",documentation= "docs/docs/DBExportTask.html")
public class DBExportTask extends Tasks {
	@InputParam(required = true,description="Get expCommand help from DBExport Task.") 
	private String expCommand;
	@InputParam(required = false,description="P:/dump/daisy")
	private String outFolderName;

	@OutputParam
	private String dumpFile;

	@Override
	public boolean run() {
		boolean status;
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
		AsynchronousStreamReader fCmdOutputThread = new AsynchronousStreamReader(processOut, fCmdOutput, new MyLogDevice(LOGGER.get()), "OUTPUT");
		fCmdOutputThread.start();
		
		StringBuffer fCmdError = new StringBuffer();
		AsynchronousStreamReader fCmdErrorThread = new AsynchronousStreamReader(processErr, fCmdError, new MyLogDevice(LOGGER.get()), "ERROR");
		fCmdErrorThread.start();
		fCmdOutputThread.join();
		fCmdErrorThread.join();

        return fCmdError.toString().contains("Export terminated successfully") || fCmdOutput.toString().contains("Export terminated successfully");
//		LOGGER.get().log(Level.INFO, fCmdErrorThread.getBuffer());
	}
	}

