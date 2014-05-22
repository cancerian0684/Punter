package org.shunya.punter.tasks;

import org.shunya.punter.annotations.InputParam;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.jpa.RunStatus;
import org.shunya.server.PunterProcessRunMessage;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

@PunterTask(author="munishc",name="EchoTask",description="Echo's the input data to SOP",documentation= "src/main/resources/docs/TextSamplerDemoHelp.html")
public class RunProcessTask extends Tasks {
	@InputParam(required = true,description="enter process id")
	private String id;

	@Override
	public boolean run() {
		LOGGER.get().log(Level.INFO, "Running process - "+ id);
        PunterProcessRunMessage processRunMessage = new PunterProcessRunMessage();
        processRunMessage.setProcessId(Long.parseLong(id));
//        processRunMessage.setParams();
        Future<Map> results = observer.createAndRunProcessSync(processRunMessage);
        try {
            sessionMap.putAll(results.get());
            return results.get().get("status").equals(RunStatus.SUCCESS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
	}
}