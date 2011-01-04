package com.sapient.punter.tasks;

import java.io.InputStream;
import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;

@PunterTask(author="munishc",name="DBImportTask",documentation="com/sapient/punter/tasks/docs/DBImportTask.html") 
public class DBImportTask extends Tasks {
	@InputParam(required = true,description="imp 'DAISY4/Welcome1@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=1523)(HOST=xldn2738dor.ldn.swissbank.com)))(CONNECT_DATA=(SERVICE_NAME=DELSHRD1)))' fromuser=AIS1 touser=DAISY2 ignore=y log=C:\\import_daisy.log") 
	private String impCommand;
	@InputParam(required = false,description="P:\\dump\\daisy\\20101007.dmp")
	private String dumpFile;

	@Override
	public boolean run() {
		boolean status=false;
		try{
			java.lang.Process p = Runtime.getRuntime().exec(impCommand+" file="+dumpFile);
			status=startOutputAndErrorReadThreads(p.getInputStream(),p.getErrorStream());
			p.waitFor();
			LOGGER.get().log(Level.INFO, "DM Import successful.");
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
		if(fCmdError.toString().contains("Import terminated successfully")||fCmdOutput.toString().contains("Import terminated successfully")){
			return true;
		}else{
			return false;
		}
//		LOGGER.get().log(Level.INFO, fCmdErrorThread.getBuffer());
	}
}