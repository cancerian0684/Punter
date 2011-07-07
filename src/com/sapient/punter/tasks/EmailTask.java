package com.sapient.punter.tasks;

import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.utils.EmailService;
import com.sapient.punter.utils.EmailServiceWithAuth;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="EmailTask",description="Email Task",documentation="com/sapient/punter/tasks/docs/EmailTask.html")
public class EmailTask extends Tasks {
	@InputParam(required = true,description="comma separated to addresses") 
	private String toAddress;
	@InputParam(required = true,description="comma separated to addresses") 
	private String ccAddress;
	@InputParam(required = true,description="Subject of Email")
	private String subject;
	@InputParam(required = true,description="from Address")
	private String fromAddress;
	@InputParam(required = true,description="html body")
	private String body;
	@InputParam(required = true,description="Comma Separated File Names")
	private String attachments;
	@InputParam(required = false,description="Username if Auth is required")
	private String username;
	@InputParam(required = false,description="Password if Auth is required")
	private String password;

	
	@Override
	public boolean run() {
		boolean status=false;
		try{
			attachments=attachments==null?"":attachments;
			String[] fileNames = attachments.split("[,;]");
			if(username!=null&&password!=null&&!username.isEmpty()&&!password.isEmpty()){
				//Authentication Based Email
				EmailServiceWithAuth.getInstance(username,password).sendEMail(subject, toAddress, body, fileNames, fromAddress, ccAddress);
			}else{
				//Non-Auth Based Email
				EmailService.getInstance().sendEMail(subject, toAddress, body, fileNames, fromAddress,ccAddress);
			}
			status=true;
			LOGGER.get().log(Level.INFO, "Email sent successfully To Addresses: "+toAddress);
		}catch (Exception e) {
			status=false;
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
	 return status;
	 }
	public static void main(String[] args) {
		EmailTask sqt=new EmailTask();
		sqt.run();
	}
}