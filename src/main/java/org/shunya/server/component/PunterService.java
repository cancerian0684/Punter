package org.shunya.server.component;

import org.asciidoctor.Asciidoctor;
import org.shunya.kb.model.Document;
import org.shunya.kb.utils.Utilities;
import org.shunya.punter.gui.AppConstants;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.tasks.Tasks;
import org.shunya.punter.utils.StringUtils;
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
import java.util.concurrent.*;

import static org.asciidoctor.Asciidoctor.Factory.create;

@Service
public class PunterService {
    private final Logger logger = LoggerFactory.getLogger(PunterService.class);
    @Autowired
    private DBService daoFacade;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final RestClient restClient = new RestClient();
    private final ConcurrentMap<Long, Tasks> taskCache = new ConcurrentHashMap<>();
    private final Asciidoctor asciidoctor = create();

    public void syncRemoteDocuments(String baseUri) {
        Long[] remoteDocList = restClient.getRemoteDocList(baseUri);
        System.out.println("remoteDocList = " + remoteDocList);
        for (Long docId : remoteDocList) {
//            int answer = JOptionPane.showConfirmDialog(Main.KBFrame, "Do you want to copy " + docId, "Confirm Copy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//            if (answer == JOptionPane.YES_OPTION) {
            try {
                Document remoteDoc = restClient.getRemoteDoc(baseUri, docId);
                Document existingMatchingDoc = daoFacade.getDocumentByMD5(remoteDoc.getMd5());
                if (existingMatchingDoc == null) {
                    daoFacade.saveDocument(remoteDoc);
                    System.out.println("copied remote remoteDoc = " + docId);
                } else {
                    System.out.println("Ignored existing remote remoteDoc = " + docId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }
    }

    public String getMemoryLogs(long taskId) {
        Tasks task = taskCache.get(taskId);
        if (task != null) {
            return task.getMemoryLogs();
        }
        return "";
    }

    public Map<String, Object> runTask(TaskData taskData, Long taskId) throws ExecutionException, InterruptedException {
        final Map<String, Object> resultsMap = new HashMap<>();
        Future<?> future = executorService.submit(() -> {
            System.out.println("task = " + taskData);
            Tasks task = Tasks.getTask(taskData);
            try {
                taskCache.put(taskId, task);
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
                resultsMap.put("error", StringUtils.getExceptionStackTrace(e));
            } finally {
                taskCache.remove(taskId);
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
        if (appProperties.get(AppConstants.CLIPBOARD_PEERS) == null || appProperties.get(AppConstants.CLIPBOARD_PEERS).toString().trim().isEmpty())
            return;
        String peersCsv = (String) appProperties.get(AppConstants.CLIPBOARD_PEERS);
        if (peersCsv != null && !peersCsv.isEmpty()) {
            peersCsv = Utilities.substituteVariables(peersCsv, appProperties);
            String[] peers = peersCsv.split("[,;]");
            for (String peer : peers) {
                try {
                    if (peer != null && !peer.trim().isEmpty()) {
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

    public int getPercentFree(String drive) {
        long freeSpace = Paths.get(drive).toFile().getUsableSpace();
        long totalSpace = Paths.get(drive).toFile().getTotalSpace();
        int percentFree = (int) (100 * freeSpace / totalSpace);
        return percentFree;
    }
}
