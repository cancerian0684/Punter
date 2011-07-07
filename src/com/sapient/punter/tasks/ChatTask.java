package com.sapient.punter.tasks;

import java.util.logging.Level;

import com.sapient.punter.annotations.InputParam;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.tasks.chat.MindAlignBot;
import com.sapient.punter.utils.StringUtils;

@PunterTask(author="munishc",name="ChatTask",description="Echo's the input data to MindAlign Chat Channel.",documentation="com/sapient/punter/tasks/docs/TextSamplerDemoHelp.html")
public class ChatTask extends Tasks {
	@InputParam(required = true,description="Enter username (UBSW\\chandemu)")
	private String username;
	@InputParam(required = true)
	private String password;
	@InputParam(required = true,description="<html>Enter Chat Channel to send Msg to</html>")
	private String channel;
	@InputParam(required = true,description="<html>Enter Message to Send</html>")
	private String message;

	@Override
	public boolean run() {
		boolean status=false;
		LOGGER.get().log(Level.INFO, "sending message to :"+channel);
		try {
			MindAlignBot instance = MindAlignBot.getInstance(channel, username, password);
			instance.sendMessage(message, false);
			instance.disconnect();
			LOGGER.get().log(Level.INFO, "Message Sent successfully.");
			status=true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.get().log(Level.SEVERE, StringUtils.getExceptionStackTrace(e));
		}
		return status;
	}
}