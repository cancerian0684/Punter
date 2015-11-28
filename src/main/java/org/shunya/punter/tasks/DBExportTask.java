package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.OutputParam;
import org.shunya.punter.annotations.PunterTask;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@PunterTask(author="munishc",name="DBExportTask",documentation= "src/main/resources/docs/DBExportTask.html")
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
			LOGGER.get().info(expCommand + " file=" + dumpFile);
			java.lang.Process p = Runtime.getRuntime().exec(expCommand+" file="+dumpFile);
			status=startOutputAndErrorReadThreads(p.getInputStream(),p.getErrorStream());
			p.waitFor();
			LOGGER.get().info("DB Backup taken successfully to file : " + dumpFile);
//			status=true;
		}catch (Exception e) {
			status=false;
			LOGGER.get().error(e.getMessage());
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

