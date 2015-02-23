package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.telegram.bot.Application;

import java.io.IOException;
import java.util.logging.Level;

@PunterTask(author="munishc",name="TelegramTask",description="Echo's the input data to Telegram API", documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class TelegramTask extends Tasks {
	@InputParam(required = true,description="enter your name here")
	private String message;

	@Override
	public boolean run() {
		LOGGER.get().log(Level.INFO, message);
        Application telegram = new Application();
        try {
            telegram.sendMessageToTestM(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
	}
}