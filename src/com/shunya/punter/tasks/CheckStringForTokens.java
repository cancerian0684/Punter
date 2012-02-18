package com.shunya.punter.tasks;

import com.shunya.punter.annotations.InputParam;
import com.shunya.punter.annotations.OutputParam;
import com.shunya.punter.annotations.PunterTask;

import java.util.logging.Level;

@PunterTask(author = "munishc", name = "CheckStringForTokens", description = "Echo's the input data to SOP", documentation = "com/sapient/punter/tasks/docs/TextSamplerDemoHelp.html")
public class CheckStringForTokens extends Tasks {
	@InputParam(required = true,description="enter the input string")
	private String input;
	@InputParam(required = true,description="enter line separated expected message strings")
	private String expectedMessages;
	
	@InputParam(required = true,description="Tell if the expected message is required or not (true, false)")
	private boolean assertion=true;
	
	@OutputParam
	private String outName="";

	@Override
	public boolean run() {
        boolean tokenFound=false;
		if(input!=null && expectedMessages!=null && !expectedMessages.isEmpty()){
				String[] messages = expectedMessages.split("\n");
				for (String message : messages) {
					if(!input.contains(message)){
						outName+=message+" not true,"+System.getProperty("line.separator");
				      }
				}
				if(outName.length()>=1){
					LOGGER.get().log(Level.SEVERE, outName);
                    tokenFound=true;
				}
            if(tokenFound && assertion){
                LOGGER.get().log(Level.INFO, "every expectation met.");
                return true;
            }
           return false;
	      }
		return true;
	}
}
