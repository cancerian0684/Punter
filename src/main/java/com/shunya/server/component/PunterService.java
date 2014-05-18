package com.shunya.server.component;

import com.shunya.kb.jpa.Document;
import com.shunya.kb.utils.Utilities;
import com.shunya.punter.gui.AppConstants;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.tasks.Tasks;
import com.shunya.server.PunterMessage;
import com.shunya.server.PunterWebDocumentHandler;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class PunterService {
    final Logger logger = LoggerFactory.getLogger(PunterService.class);
    @Autowired
    private StaticDaoFacade daoFacade;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private RestClient restClient = new RestClient();

    public Map<String, Object> runTask(TaskData taskData) throws ExecutionException, InterruptedException {
        final Map<String, Object> resultsMap = new HashMap<>();
        Future<?> future = executorService.submit(() -> {
            System.out.println("task = " + taskData);
            Tasks task = Tasks.getTask(taskData);
            try {
                task.setTaskDao(taskData);
                task.setHosts(null);
                task.setSessionMap(resultsMap);
                task.beforeTaskStart();
                task.execute();
                task.afterTaskFinish();
                resultsMap.put("logs", task.getMemoryLogs());
                resultsMap.put("status", true);
            } catch (Exception e) {
                logger.info("runTask failed - ", e);
                resultsMap.put("status", false);
                resultsMap.put("error", Util.getExceptionSummary(e));
            }
        });
        future.get();
        return resultsMap;
    }

    public FileSystemResource getFile(Long id) throws IOException {
        logger.info("Serving file : " + id);
        Document doc = new Document();
        doc.setId(id);
        doc = daoFacade.getDocument(doc);
        File targetFile = PunterWebDocumentHandler.process(doc);
        return new FileSystemResource(targetFile);
    }

    public void sendMessageToPeers(PunterMessage punterMessage) throws InterruptedException, RemoteException {
        Map<String, Object> appProperties = (Map<String, Object>) AppSettings.getInstance().getObject(AppConstants.APP_PROPERTIES_MAP);
        String peersCsv = (String) appProperties.get(AppConstants.CLIPBOARD_PEERS);
        if (peersCsv != null && !peersCsv.isEmpty()) {
            peersCsv = Utilities.substituteVariables(peersCsv, appProperties);
            String[] peers = peersCsv.split("[,;]");
            for (String peer : peers) {
                try {
                    if (peer != null && !peer.trim().isEmpty())
                        restClient.sendClipBoardMessage(peer.trim(), punterMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
