package org.shunya.server.component;

import org.eclipse.jdt.internal.compiler.util.Util;
import org.markdown4j.Markdown4jProcessor;
import org.shunya.kb.model.Document;
import org.shunya.kb.utils.Utilities;
import org.shunya.punter.gui.AppConstants;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.tasks.Tasks;
import org.shunya.server.PunterMessage;
import org.shunya.server.PunterWebDocumentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class PunterService {
    private final Logger logger = LoggerFactory.getLogger(PunterService.class);
    @Autowired
    private StaticDaoFacade daoFacade;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final RestClient restClient = new RestClient();
    private final Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();

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
                boolean status = task.execute();
                task.afterTaskFinish();
                resultsMap.put("logs", task.getMemoryLogs());
                resultsMap.put("status", status);
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
        Document doc = daoFacade.getDocument(id);
//        String html = markdown4jProcessor.process(new String(doc.getContent()));
        File targetFile = PunterWebDocumentHandler.process(doc);
        return new FileSystemResource(targetFile);
    }

    public void sendMessageToPeers(PunterMessage punterMessage) throws InterruptedException, RemoteException {
        Map<String, Object> appProperties = (Map<String, Object>) AppSettings.getInstance().getObject(AppConstants.APP_PROPERTIES_MAP);
        if(appProperties.get(AppConstants.CLIPBOARD_PEERS)==null || appProperties.get(AppConstants.CLIPBOARD_PEERS).toString().trim().isEmpty())
            return;
        String peersCsv = (String) appProperties.get(AppConstants.CLIPBOARD_PEERS);
        if (peersCsv != null && !peersCsv.isEmpty()) {
            peersCsv = Utilities.substituteVariables(peersCsv, appProperties);
            String[] peers = peersCsv.split("[,;]");
            for (String peer : peers) {
                try {
                    if (peer != null && !peer.trim().isEmpty()){
                        executorService.execute(() -> {
                            try {
                                restClient.sendClipBoardMessage(peer.trim(), punterMessage);
                            } catch (Exception e) {
                                logger.error("Error sending message to clipboard : " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getPercentFree(String drive){
        long freeSpace = Paths.get(drive).toFile().getUsableSpace();
        long totalSpace = Paths.get(drive).toFile().getTotalSpace();
        int percentFree = (int) (100 * freeSpace / totalSpace);
        return percentFree;
    }
}
