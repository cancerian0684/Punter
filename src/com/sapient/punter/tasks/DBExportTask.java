package com.sapient.punter.tasks;

import java.util.Date;
import java.util.logging.Level;

import com.sapient.punter.Tasks;
import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.OutputParam;
import com.sapient.punter.annotations.Task;

@Task(author="munishc",name="DBExportTask",description="Export DB into specified file.")
public class DBExportTask extends Tasks {
	@InputParam(required = true)
	private String targetFile;
	@InputParam(required = false)
	private String tnsEntry;
	@InputParam(required = false)
	private Date dob;

	@OutputParam
	private String dumpFile;

	@Override
	public boolean run() {
		boolean status=false;
		
		status=true;
		return status;
	}
}