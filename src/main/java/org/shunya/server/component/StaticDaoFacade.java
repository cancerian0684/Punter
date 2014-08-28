package org.shunya.server.component;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.shunya.kb.gui.SearchQuery;
import org.shunya.kb.model.Attachment;
import org.shunya.kb.model.Category;
import org.shunya.kb.model.Document;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.gui.PunterJobBasket;
import org.shunya.punter.gui.SingleInstanceFileLock;
import org.shunya.punter.jpa.ProcessData;
import org.shunya.punter.jpa.ProcessHistory;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.jpa.TaskHistory;
import org.shunya.punter.utils.ClipBoardListener;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StaticDaoFacade {
    private ClipBoardListener clipBoardListener;
    private SingleInstanceFileLock singleInstanceFileLock;
    private SessionFacade sessionFacade;
    private JPATransatomatic transatomatic;
    @Autowired
    private ServerSettings serverSettings;
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

    public StaticDaoFacade() {
        singleInstanceFileLock = new SingleInstanceFileLock("PunterServer.lock");
        sessionFacade = SessionFacade.getInstance();
        transatomatic = new JPATransatomatic(new JPASessionFactory());
        buildSynonymsCacheLocal();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public synchronized void makeConnection() {
        //nothing to make connection to
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>(20);
        Scanner scanner = new Scanner(StaticDaoFacade.class.getClassLoader().getResourceAsStream("resources/categories.properties"));
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
        List<String> result = LuceneIndexDao.getInstance().listAllTermsForTitle();
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
        return result;
    }

    public void updateAccessCounter(Document doc) {
        /*Session em = emf.createEntityManager();
          DocumentService service = new DocumentService(em);
          em.getTransaction().begin();
          service.updateAccessCounter(doc);
          em.getTransaction().commit();
          em.close();*/
    }

    public Document createDocument(String author) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Document doc = new Document();
            doc.setTitle("test title");
            doc.setContent("".getBytes());
            doc.setDateCreated(new Date());
            doc.setDateUpdated(new Date());
            doc.setCategory("/all");
            doc.setAuthor(author);
            session.persist(doc);
            session.flush();
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(doc);
        });
        return resultHolder.getResult();
    }

    public List<Document> getDocList(SearchQuery searchQuery) {
        try {
            long t1 = System.currentTimeMillis();
            List<Document> result = LuceneIndexDao.getInstance().search(searchQuery.getQuery(), searchQuery.getCategory(), searchQuery.isAndFilter(), 0, searchQuery.getMaxResults());
            long t2 = System.currentTimeMillis();
            System.err.println("time consumed : " + (t2 - t1));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void deleteAllForCategory(String category) throws IOException {
        long t1 = System.currentTimeMillis();
        List<Document> result = LuceneIndexDao.getInstance().search("*", category, true, 0, 100);
        for (Document document : result) {
            System.out.println("Deleting document - " + document);
            transatomatic.run(session -> {
                Document document1 = (Document) session.get(Document.class, document.getId());
                session.delete(document1);
                session.flush();
                LuceneIndexDao.getInstance().deleteIndexForDoc(document1.getId());
            });
        }
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
    }

    public Document saveDocument(Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(doc);
        });
        return resultHolder.getResult();
    }

    public Attachment saveAttachment(Attachment attach){
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(attach);
            session.flush();
            Document doc = attach.getDocument();
            doc = (Document) session.get(Document.class, doc.getId());
            session.refresh(doc);
            session.getTransaction().commit();
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(attach);
        });
        return resultHolder.getResult();
    }

    public Document getDocument(Long id) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Document document = (Document) session.get(Document.class, id);
            if (document != null) {
                session.refresh(document);
                Hibernate.initialize(document.getContent());
                resultHolder.setResult(document);
            }
        });
        return resultHolder.getResult();
    }

    public List<Long> getDocumentIds() {
        final ResultHolder<List<Long>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            List<Document> documents = session.createCriteria(Document.class).list();
            List<Long> ids = new ArrayList<>();
            for (Document o : documents) {
                ids.add(o.getId());
            }
            resultHolder.setResult(ids);
        });
        return resultHolder.getResult();
    }

    public Attachment getAttachment(Attachment doc) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Attachment persisted = (Attachment) session.get(Attachment.class, doc.getId());
            session.refresh(persisted);
            resultHolder.setResult(persisted);
        });
        return resultHolder.getResult();
    }

    public boolean deleteAttachment(long id) throws RemoteException {
        transatomatic.run(session -> {
            Attachment attchment = (Attachment) session.get(Attachment.class, id);
            attchment.getDocument().getAttachments().remove(attchment);
            session.delete(attchment);
            session.flush();
            Document doc = attchment.getDocument();
            doc = (Document) session.get(Document.class, doc.getId());
            session.refresh(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
        });
        return true;
    }

    public boolean deleteDocument(final Document document) {
        transatomatic.run(session -> {
            Document doc = (Document) session.get(Document.class, document.getId());
            if (doc != null) {
                session.delete(doc);
            }
            LuceneIndexDao.getInstance().deleteIndexForDoc(document.getId());
        });
        return true;
    }

    public void updateAllProcessProperties() {
        transatomatic.run(session -> {
            try {
                Query q = session.createQuery("from ProcessData p");
//                q.setHint("eclipselink.refresh", "true");
                List<ProcessData> processList = q.list();
                for (ProcessData processData : processList) {
                    FieldPropertiesMap inProp = org.shunya.punter.tasks.Process.listInputParams();
                    processData.setInputParams(inProp);
                    try {
                        ProcessData tmp = (ProcessData) session.get(ProcessData.class, processData.getId());
                        tmp.setInputParams(processData.getInputParams());
                        session.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }

    public int deleteStaleHistory(final int days) {
        AtomicInteger counter = new AtomicInteger(0);
        transatomatic.run(session -> {
            try {
                Calendar cal = GregorianCalendar.getInstance();
                cal.set(cal.DATE, cal.get(cal.DATE) - days);
                List<ProcessHistory> processHistoryList = session.createCriteria(ProcessHistory.class)
                        .setMaxResults(100)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.lt("startTime", cal.getTime()))
                        .list();
                for (ProcessHistory processHistory : processHistoryList) {
                    session.delete(processHistory);
                    counter.incrementAndGet();
                    System.out.println("Removed : " + processHistory.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return counter.get();
    }

    public List<Category> getCategoryList() {
        final ResultHolder<List<Category>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            resultHolder.setResult(session.createCriteria(Category.class)
                    .setMaxResults(100)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .list());
        });
        return resultHolder.getResult();
    }

    public Category saveCategory(Category category) throws RemoteException {
        final ResultHolder<Category> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(category);
            resultHolder.setResult(category);
        });
        return resultHolder.getResult();
    }

    public boolean deleteCategory(long id) throws RemoteException {
        transatomatic.run(session -> {
            Category category = (Category) session.get(Category.class, id);
            session.delete(category);
        });
        return true;
    }

    public void compressTables() {
        transatomatic.run(session -> {
            try {
                String[] tables = {"PROCESSHISTORY", "PROCESS", "TASK", "TASKHISTORY", "DOCUMENT", "DOCUMENT_LOB", "ATTACHMENT", "ATTACHMENT_LOB"};
                for (String string : tables) {
                    Query q = session.createQuery("call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('PUNTER', '" + string + "', 1)");
                    q.executeUpdate();
                    session.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshIndexes() {
        System.err.println("Refreshing Index's");
        buildSynonymsCacheLocal();
        transatomatic.run(session -> {
            System.out.println("Clearing old index");
            LuceneIndexDao.getInstance().deleteIndex();
            org.hibernate.Query query = session.createQuery("SELECT e FROM Document e");
            List<Document> allDocs = query.list();
            for (Document emp : allDocs) {
                System.out.println(emp.getCategory());
                LuceneIndexDao.getInstance().indexDocs(emp);
            }
        });
        System.err.println("Indexes Refreshed");
    }

    public void buildSynonymsCacheLocal() {
        System.out.println("Rebuilding Synonym Cache Local");
        Scanner scanner = new Scanner(StaticDaoFacade.class.getClassLoader().getResourceAsStream("resources/synonyms.properties"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            SynonymService.getService().addWords(line);
        }
        scanner.close();
    }

    public void rebuildIndex() throws RemoteException {
        transatomatic.run(session -> {
            System.out.println("Clearing old index");
            LuceneIndexDao.getInstance().deleteIndex();
            Query query = session.createQuery("SELECT e FROM Document e");
            List<Document> allDocs = query.list();
            for (Document doc : allDocs) {
                System.out.println(doc.getCategory());
                LuceneIndexDao.getInstance().indexDocs(doc);
            }
        });
    }

    public void removeTask(TaskData task) {
        transatomatic.run(session -> {
            TaskData tmp = (TaskData) session.get(TaskData.class, task.getId());
            tmp.getProcess().getTaskList().remove(tmp);
            session.delete(tmp);
        });
    }

    public void removeProcess(ProcessData proc) {
        transatomatic.run(session -> {
            ProcessData tmp = (ProcessData) session.get(ProcessData.class, proc.getId());
            session.delete(tmp);
        });
    }

    public <T> T create(T task) {
        final ResultHolder<Object> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.save(task);
            resultHolder.setResult(task);
        });
        return (T) resultHolder.getResult();
    }

    public void saveTaskHistory(TaskHistory th) {
        transatomatic.run(session -> {
            TaskHistory taskHistory = (TaskHistory) session.get(TaskHistory.class, th.getId());
            taskHistory.setRunState(th.getRunState());
            taskHistory.setRunStatus(th.getRunStatus());
            taskHistory.setSequence(th.getSequence());
            taskHistory.setLogs(th.getLogs());
            taskHistory.setStartTime(th.getStartTime());
            taskHistory.setFinishTime(th.getFinishTime());
            session.saveOrUpdate(taskHistory);
            session.flush();
        });
    }

    public void saveProcessHistory(ProcessHistory procHistory) {
        transatomatic.run(session -> {
            ProcessHistory ph = (ProcessHistory) session.get(ProcessHistory.class, procHistory.getId());
            ph.setRunState(procHistory.getRunState());
            ph.setRunStatus(procHistory.getRunStatus());
            ph.setStartTime(procHistory.getStartTime());
            ph.setFinishTime(procHistory.getFinishTime());
            ph.setClearAlert(procHistory.isClearAlert());
            session.saveOrUpdate(ph);
            session.flush();
        });
    }

    public TaskData saveTask(TaskData t) {
        final ResultHolder<TaskData> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            TaskData tmp = (TaskData) session.get(TaskData.class, t.getId());
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
            session.saveOrUpdate(tmp);
            session.flush();
            resultHolder.setResult(tmp);
        });
        return resultHolder.getResult();
    }


    public ProcessData saveProcess(ProcessData p) {
        final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            try {
                ProcessData tmp = (ProcessData) session.get(ProcessData.class, p.getId());
                //			em.lock(tmp, LockModeType.READ);
                tmp.setName(p.getName());
                tmp.setInputParams(p.getInputParams());
                tmp.setUsername(p.getUsername());
                tmp.setDescription(p.getDescription());
                session.flush();
                resultHolder.setResult(p);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return resultHolder.getResult();
    }


    public void listTask(long id) {
        transatomatic.run(session -> {
            TaskData task = (TaskData) session.get(TaskData.class, id);
            try {
                if (task != null) {
                    System.out.println("Listing task for " + task.getId());
                    Set<String> keySet = task.getInputParamsAsObject().keySet();
                    for (String object : keySet) {
                        System.out.println(object.toString() + " -- "
                                + task.getInputParamsAsObject().get(object));
                    }
                    task.getOutputParamsAsObject();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public List<ProcessData> getScheduledProcessList(String username) {
        try {
            final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                Query q = session.createQuery("from ProcessData p where p.username=:username");
                q.setParameter("username", username);
                //            q.setHint("eclipselink.refresh", "true");
                List<ProcessData> dbProcList = q.list();
                List<ProcessData> processList = new ArrayList<ProcessData>();
                for (ProcessData processDao : dbProcList) {
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
                resultHolder.setResult(processList);
            });
            return resultHolder.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    public List<ProcessData> getProcessList(String username) throws Exception {
        final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessData p where p.username=:username order by p.id asc");
            q.setParameter("username", username);
            q.setCacheMode(CacheMode.REFRESH);
            resultHolder.setResult(q.list());
        });
        return resultHolder.getResult();
    }


    public ProcessData getProcess(long id) {
        final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.setCacheMode(CacheMode.IGNORE);
            ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
//                session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }


    public TaskHistory getTaskDao(TaskHistory td) {
        final ResultHolder<TaskHistory> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            TaskHistory proc = (TaskHistory) session.get(TaskHistory.class, td.getId());
            session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }


    public List<ProcessHistory> getProcessHistoryListForProcessId(long id) {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
            session.refresh(proc);
            resultHolder.setResult(proc.getProcessHistoryList());
        });
        return resultHolder.getResult();
    }


    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
            //            q.setHint("eclipselink.refresh", "true");
            q.setParameter("pid", id);
            q.setFirstResult(0);
            q.setMaxResults(serverSettings.getMaxProcessHistory());
            List<ProcessHistory> processHistoryList = q.list();
            resultHolder.setResult(processHistoryList);
        });
        return resultHolder.getResult();
    }

    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessHistory ph where ph.process.username = :username AND ph.clearAlert=false order by ph.startTime desc");
            //            q.setHint("eclipselink.refresh", "true");
            q.setParameter("username", username);
            //    q.setParameter("runStatus", RunStatus.SUCCESS);
            q.setFirstResult(0);
            q.setMaxResults(serverSettings.getMaxProcessAlerts());
            List<ProcessHistory> processHistoryList = q.list();
            resultHolder.setResult(processHistoryList);
        });
        return resultHolder.getResult();
    }


    public ProcessHistory getProcessHistoryById(long id) {
        final ResultHolder<ProcessHistory> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            ProcessHistory proc = (ProcessHistory) session.get(ProcessHistory.class, id);
            session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }


    public List<TaskData> getProcessTasksById(long pid) {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from TaskData t where t.process.id=:pid order by t.sequence");
            q.setParameter("pid", pid);
            //            q.setHint("eclipselink.refresh", "true");
            List<TaskData> taskList = q.list();
            resultHolder.setResult(taskList == null ? Collections.EMPTY_LIST : taskList);
        });
        return resultHolder.getResult();
    }


    public List<TaskData> getSortedTasksByProcessId(long pid) {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
            q.setParameter("pid", pid);
            q.setCacheMode(CacheMode.REFRESH);
            List<TaskData> taskList = q.list();
            resultHolder.setResult(taskList);
        });
        return resultHolder.getResult();
    }

    public List<TaskData> getProcessTasks(long pid) {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessData p where p.id=:pid");
            q.setParameter("pid", pid);
            List<ProcessData> processList = q.list();
            System.out.println(processList.get(0).getDescription());
            List<TaskData> tl = processList.get(0).getTaskList();
            resultHolder.setResult(tl);
        });
        return resultHolder.getResult();
    }

    public String getDevEmailCSV() throws RemoteException {
        return serverSettings.getDevEmailCSV();
    }
}
