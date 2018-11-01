package org.shunya.server.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.shunya.kb.gui.SearchQuery;
import org.shunya.kb.model.*;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.gui.PunterJobBasket;
import org.shunya.punter.gui.SingleInstanceFileLock;
import org.shunya.punter.jpa.*;
import org.shunya.punter.utils.ClipBoardListener;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.server.ClipboardPunterMessage;
import org.shunya.server.PunterMessage;
import org.shunya.server.PunterProcessRunMessage;
import org.shunya.server.SessionFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
@Transactional
public class DBService {
    private ClipBoardListener clipBoardListener;
    private SingleInstanceFileLock singleInstanceFileLock;
    private SessionFacade sessionFacade;

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private AccessCounterRepository accessCounterRepository;
    @Autowired
    private SynonymWordRepository synonymWordRepository;
    @Autowired
    private ServerSettings serverSettings;
    @Autowired
    private LuceneIndexService luceneIndexService;
    @Autowired
    private SynonymService synonymService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TaskDataRepository taskDataRepository;
    @Autowired
    private TaskHistoryRepository taskHistoryRepository;
    @Autowired
    private ProcessDataRepository processDataRepository;
    @Autowired
    private ProcessHistoryRepository processHistoryRepository;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public DBService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    private RestClient restClient = new RestClient();

    public void setClipBoardListener(ClipBoardListener clipBoardListener) {
        this.clipBoardListener = clipBoardListener;
    }

    public String getSessionId() {
        return sessionId;
    }

    private String sessionId;

    public String getUsername() {
        return AppSettings.getInstance().getUsername();
    }

    public void export(File outputDir, Long documentId) throws IOException {
        if (!outputDir.exists())
            outputDir.mkdirs();
        Document document = getDocument(documentId);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        File fileDir = new File(outputDir, "" + documentId + "-json.gz");
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(new FileOutputStream(fileDir)), "UTF8"))) {
            objectWriter.writeValue(out, document);
        }
    }

    public void exportAll(File outputDir) throws IOException {
        List<Long> documentIds = getDocumentIds();
        outputDir.mkdirs();
        for (Long documentId : documentIds) {
            export(outputDir, documentId);
        }
    }

    public void importAll(File dataDir) throws IOException {
        File[] files = dataDir.listFiles();
        for (File file : files) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader objectReader = mapper.reader();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new GZIPInputStream(new FileInputStream(file)), "UTF8"))) {
                Document remoteDoc = objectReader.readValue(new MappingJsonFactory().createParser(IOUtils.toString(in)), new TypeReference<Document>() {
                });
                if (remoteDoc.getMd5() != null) {
                    Document existingMatchingDoc = getDocumentByMD5(remoteDoc.getMd5());
                    if (existingMatchingDoc == null) {
                        saveDocument(remoteDoc);
                        System.out.println("copied remote remoteDoc = " + file.getName());
                    } else {
                        deleteDocument(existingMatchingDoc);
                        saveDocument(remoteDoc);
                        System.out.println("updated existing remote remoteDoc = " + file.getName());
                    }
                } else {
                    saveDocument(remoteDoc);
                }
            }
        }
    }

    public void messageProcessor() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        PunterMessage message = getMessage();
                        process(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setName("Message.Listener");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void process(PunterMessage message) {
        if (message instanceof ClipboardPunterMessage && clipBoardListener != null) {
            clipBoardListener.handleContent((ClipboardPunterMessage) message);
        } else if (message instanceof PunterProcessRunMessage) {
            try {
                if (((PunterProcessRunMessage) message).getHostname().equalsIgnoreCase(getLocalHostAddress().getHostName())) {
                    PunterJobBasket.getInstance().addJobToBasket((PunterProcessRunMessage) message);
                }
            } catch (Exception e) {
            }
        }
    }

    public void restartClient() {
        //this will not work
    }

    public PunterMessage getMessage() throws InterruptedException, RemoteException {
        return sessionFacade.getMessage(getSessionId());
    }

    public void ping() throws RemoteException {
//        hibernateDaoFacade.ping(getSessionId());
    }

    public InetAddress getServerHostAddress() throws Exception {
        return InetAddress.getLocalHost();
    }

    public InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    public long getWebServerPort() throws RemoteException {
        return serverSettings.getWebServerPort();
    }

    @PostConstruct
    public void initialize() {
        singleInstanceFileLock = new SingleInstanceFileLock("PunterServer.lock");
        sessionFacade = SessionFacade.getInstance();
        synonymService.loadFromDB();
    }

    public void publish(AccessEvent accessEvent) {
        publisher.publishEvent(accessEvent);
    }

    public void incrementCounter(String entityName, long entityId) {
        final Optional<AccessCounter> counterOptional = accessCounterRepository.findOneByEntityIdAndEntityName(entityId, entityName);
        if (counterOptional.isPresent()) {
            final AccessCounter counter = counterOptional.get();
            counter.setCounter(counter.getCounter() + 1);
        } else {
            AccessCounter ac = new AccessCounter();
            ac.setEntityId(entityId);
            ac.setEntityName(Document.class.getSimpleName());
            ac.setCounter(0);
            accessCounterRepository.save(ac);
        }
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public synchronized void makeConnection() {
        //nothing to make connection to
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>(20);
        Scanner scanner = new Scanner(DBService.class.getClassLoader().getResourceAsStream("resources/categories.properties"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            StringTokenizer stk = new StringTokenizer(line, ",");
            while (stk.hasMoreTokens()) {
                categories.add(stk.nextToken());
            }
        }
        scanner.close();
        return categories;
    }

    public List<String> getAllTerms() throws IOException {
        long t1 = System.currentTimeMillis();
        List<String> result = luceneIndexService.listAllTermsForTitle();
        long t2 = System.currentTimeMillis();
//        System.err.println("time consumed : " + (t2 - t1));
        return result;
    }

    public Document createDocument(String author) {
        Document doc = new Document();
        doc.setTitle("test title");
        doc.setContent("".getBytes());
        doc.setDateCreated(new Date());
        doc.setDateUpdated(new Date());
        doc.setCategory("/all");
        doc.setAuthor(author);
        documentRepository.save(doc);
        luceneIndexService.indexDocs(doc);
        return doc;
    }

    public SearchResult getDocList(SearchQuery searchQuery) {
        try {
            long t1 = System.currentTimeMillis();
            SearchResult searchResult = luceneIndexService.search(searchQuery.getQuery(), searchQuery.getCategory(), searchQuery.isAndFilter(), searchQuery.getStart(), searchQuery.getMaxResults());
            long t2 = System.currentTimeMillis();
            System.err.println("time consumed : " + (t2 - t1));
            return searchResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SearchResult();
    }

    public void deleteAllForCategory(String category) throws IOException {
        long t1 = System.currentTimeMillis();
        SearchResult searchResult = luceneIndexService.search("*", category, true, 0, 100);
        for (Document document : searchResult.getDocuments()) {
            System.out.println("Deleting document - " + document);
            documentRepository.deleteById(document.getId());
            luceneIndexService.deleteIndexForDoc(document.getId());
        }
        long t2 = System.currentTimeMillis();
//        System.err.println("time consumed : " + (t2 - t1));
    }

    public Document saveDocument(Document doc) {
        documentRepository.save(doc);
        luceneIndexService.indexDocs(doc);
        return doc;
    }

    public Attachment saveAttachment(Attachment attach) {
        attachmentRepository.save(attach);
        final Document document = attach.getDocument();
        luceneIndexService.indexDocs(document);
        return attach;
    }

    public Document getDocument(Long id) {
        final Document document = documentRepository.findById(id).get();
        Hibernate.initialize(document.getContent());
        return document;
    }

    public Document getDocumentByMD5(String md5) {
        final Optional<Document> documentOptional = documentRepository.findOneByMd5(md5);
        if (documentOptional.isPresent()) {
            final Document document = documentOptional.get();
            Hibernate.initialize(document.getContent());
            return document;
        }
        return null;
    }

    public List<SynonymWord> getSynonymWords() {
        return synonymWordRepository.findAll();
    }

    public List<SynonymWord> getSynonymWords(String filter) {
        return synonymWordRepository.findAllByWordsContainingIgnoreCase(filter);
    }

    public List<Long> getDocumentIds() {
        /**
         try {
         final List<Document> documents = luceneIndexService.search("**", "/all", false, 0, 1000);
         List<Long> ids = new ArrayList<>();
         for (Document o : documents) {
         ids.add(o.getId());
         }
         return ids;
         } catch (IOException e) {
         e.printStackTrace();
         }
         **/
        final List<Document> documents = documentRepository.findAll();
        List<Long> ids = new ArrayList<>();
        for (Document o : documents) {
            ids.add(o.getId());
        }
        return ids;
    }

    public Attachment getAttachment(Attachment doc) {
        return attachmentRepository.findById(doc.getId()).get();
    }

    public boolean deleteAttachment(long id) throws RemoteException {
        final Attachment attachment = attachmentRepository.findById(id).get();
        attachment.getDocument().getAttachments().remove(attachment);
        attachmentRepository.deleteById(attachment.getId());

        final Document document = documentRepository.findById(attachment.getDocument().getId()).get();
        luceneIndexService.indexDocs(document);
        return true;
    }

    public boolean deleteDocument(final Document document) {
        final Document one = documentRepository.findById(document.getId()).get();
        luceneIndexService.deleteIndexForDoc(one.getId());
        return true;
    }

    public void updateAllProcessProperties() {
        try {
            final List<ProcessData> processList = processDataRepository.findAll();
            for (ProcessData processData : processList) {
                FieldPropertiesMap inProp = org.shunya.punter.tasks.Process.listInputParams();
                processData.setInputParams(inProp);
                try {
                    ProcessData tmp = processDataRepository.findById(processData.getId()).get();
                    tmp.setInputParams(processData.getInputParams());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public int deleteStaleHistory(final int days) {
        AtomicInteger counter = new AtomicInteger(0);
        try {
            Calendar cal = GregorianCalendar.getInstance();
            cal.set(cal.DATE, cal.get(cal.DATE) - days);
            final List<ProcessHistory> histories = processHistoryRepository.findByStartTimeLessThan(cal.getTime());
            for (ProcessHistory processHistory : histories) {
                processHistoryRepository.deleteById(processHistory.getId());
                counter.incrementAndGet();
                System.out.println("Removed : " + processHistory.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counter.get();
    }

    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(Category category) throws RemoteException {
        categoryRepository.save(category);
        return category;
    }

    public boolean deleteCategory(long id) throws RemoteException {
        categoryRepository.deleteById(id);
        return true;
    }

    public void refreshIndexes() {
        System.err.println("Refreshing Index's");
//        buildSynonymsCacheLocal();
        System.out.println("Clearing old index");
        luceneIndexService.deleteIndex();
        List<Document> allDocs = documentRepository.findAll();
        for (Document emp : allDocs) {
            System.out.println(emp.getCategory());
            luceneIndexService.indexDocs(emp);
        }
        System.err.println("Indexes Refreshed");
    }

    public void buildSynonymsCacheLocal() {
        System.out.println("Rebuilding Synonym Cache Local");
        Scanner scanner = new Scanner(DBService.class.getClassLoader().getResourceAsStream("resources/synonyms.properties"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
//            synonymService.addWordsToCache(line);
        }
        scanner.close();
    }

    public void rebuildIndex() throws RemoteException {
        try {
            System.out.println("Clearing old index");
            luceneIndexService.deleteIndex();
            List<Document> allDocs = documentRepository.findAll();
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            int counter = 0;
            for (Document doc : allDocs) {
                System.out.println(++counter + " --> [" + doc.getId() + "] " + doc.getTitle());
                executorService.submit(() -> luceneIndexService.indexDocs(doc));
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("Index rebuilding complete now");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeTask(TaskData task) {
        TaskData tmp = taskDataRepository.findById(task.getId()).get();
        tmp.getProcess().getTaskList().remove(tmp);
        taskDataRepository.delete(tmp);
    }

    public void removeProcess(ProcessData proc) {
        processDataRepository.delete(proc);
    }

    public void saveTaskHistory(TaskHistory th) {
        final TaskHistory taskHistory = taskHistoryRepository.findById(th.getId()).get();
        taskHistory.setRunState(th.getRunState());
        taskHistory.setRunStatus(th.getRunStatus());
        taskHistory.setSequence(th.getSequence());
        taskHistory.setLogs(th.getLogs());
        taskHistory.setStartTime(th.getStartTime());
        taskHistory.setFinishTime(th.getFinishTime());
        taskHistoryRepository.save(taskHistory);
    }

    public void saveProcessHistory(ProcessHistory procHistory) {
        final ProcessHistory ph = processHistoryRepository.findById(procHistory.getId()).get();
        ph.setRunState(procHistory.getRunState());
        ph.setRunStatus(procHistory.getRunStatus());
        ph.setStartTime(procHistory.getStartTime());
        ph.setFinishTime(procHistory.getFinishTime());
        ph.setClearAlert(procHistory.isClearAlert());
        processHistoryRepository.save(ph);
    }

    public TaskData saveTask(TaskData t) {
        final TaskData tmp = taskDataRepository.findById(t.getId()).get();
        //			em.lock(tmp, LockModeType.READ);
        tmp.setActive(t.isActive());
        tmp.setAuthor(t.getAuthor());
        tmp.setClassName(t.getClassName());
        tmp.setDescription(t.getDescription());
        try {
            tmp.setInputParamsAsObject(t.getInputParamsAsObject());
            tmp.setOutputParamsAsObject(t.getOutputParamsAsObject());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        tmp.setName(t.getName());
        tmp.setProcess(t.getProcess());
        tmp.setSequence(t.getSequence());
        tmp.setFailOver(t.isFailOver());
        tmp.setHosts(t.getHosts());
        taskDataRepository.save(tmp);
        return tmp;
    }

    public ProcessData saveProcess(ProcessData p) {
        final ProcessData tmp = processDataRepository.findById(p.getId()).get();
        tmp.setName(p.getName());
        try {
            tmp.setInputParams(p.getInputParams());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        tmp.setUsername(p.getUsername());
        tmp.setDescription(p.getDescription());
        processDataRepository.save(tmp);
        return tmp;
    }

    public List<ProcessData> getScheduledProcessList(String username) {
        try {
            final List<ProcessData> dataList = processDataRepository.findByUsername(username);
            List<ProcessData> processList = new ArrayList<>();
            for (ProcessData processDao : dataList) {
                String ss;
                try {
                    ss = processDao.getInputParams().get("scheduleString").getValue().trim();
                } catch (JAXBException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                if (!ss.isEmpty())
                    processList.add(processDao);
            }
            return processList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    public List<ProcessData> getProcessList(String username) throws Exception {
        final List<ProcessData> list = processDataRepository.findByUsername(username);
        return list;
    }

    public ProcessData getProcess(long id) {
        return processDataRepository.findById(id).get();
    }

    public TaskHistory getTaskDao(TaskHistory td) {
        return taskHistoryRepository.findById(td.getId()).get();
    }

    public List<ProcessHistory> getProcessHistoryListForProcessId(long id) {
        final ProcessData processData = processDataRepository.findById(id).get();
        return processData.getProcessHistoryList();
    }

    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        return processHistoryRepository.findByProcessId(id);
    }

    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        final int processAlerts = serverSettings.getMaxProcessAlerts();
        //TODO - show only this much process alerts
        final List<ProcessHistory> byUsernameActive = processHistoryRepository.findByUsernameActive(username);
        return byUsernameActive;
    }


    public ProcessHistory getProcessHistoryById(long id) {
        return processHistoryRepository.findById(id).get();
    }

    public List<TaskData> getProcessTasksById(long pid) {
        final List<TaskData> tasks = taskDataRepository.findTasks(pid);
        return tasks;
    }

    public List<TaskData> getSortedTasksByProcessId(long pid) {
        List<TaskData> taskList = taskDataRepository.findActiveTasks(pid);
        return taskList;
    }

    public String getDevEmailCSV() throws RemoteException {
        return serverSettings.getDevEmailCSV();
    }

    public void saveSynonym(SynonymWord word) {
        final SynonymWord one = synonymWordRepository.findById(word.getId()).get();
        one.setWords(word.getWords());
    }

    public void create(SynonymWord synonymWord) {
        synonymWordRepository.save(synonymWord);
    }

    public ProcessData create(ProcessData processData) {
        processDataRepository.save(processData);
        return processData;
    }

    public TaskData create(TaskData taskData) {
        taskDataRepository.save(taskData);
        return taskData;
    }

    public ProcessHistory create(ProcessHistory processHistory) {
        processHistoryRepository.save(processHistory);
        return processHistory;
    }
}
