package com.shunya.server.component;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.gui.PunterJobBasket;
import com.shunya.punter.gui.SingleInstanceFileLock;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.utils.ClipBoardListener;
import com.shunya.punter.utils.FieldPropertiesMap;
import com.shunya.server.*;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
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
                LuceneIndexDao.getInstance().deleteIndexForDoc(document1);
            });
        }
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
    }

    public Document saveDocument(Document doc) throws RemoteException {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(doc);
        });
        return resultHolder.getResult();
    }

    public Attachment saveAttachment(Attachment attach) throws RemoteException {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.save(attach);
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

    public Document getDocument(Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Document document = (Document) session.get(Document.class, doc.getId());
            session.refresh(document);
            resultHolder.setResult(document);
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

    public boolean deleteAttachment(Attachment attch) throws RemoteException {
        transatomatic.run(session -> {
            Attachment attchment = (Attachment) session.get(Attachment.class, attch.getId());
            session.delete(attchment);
            session.flush();
            Document doc = attchment.getDocument();
            doc = (Document) session.get(Document.class, doc.getId());
            session.refresh(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
        });
        return true;
    }

    public boolean deleteDocument(Document attch) {
        transatomatic.run(session -> {
            Document document = (Document) session.get(Document.class, attch.getId());
            session.delete(document);
            session.flush();
            LuceneIndexDao.getInstance().deleteIndexForDoc(document);
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
                    FieldPropertiesMap inProp = com.shunya.punter.tasks.Process.listInputParams();
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
            for (Document emp : allDocs) {
                System.out.println(emp.getCategory());
                LuceneIndexDao.getInstance().indexDocs(emp);
            }
        });
    }

    public void removeTask(TaskData task) {
        transatomatic.run(session -> {
            TaskData tmp = (TaskData) session.get(TaskData.class, task.getId());
            session.delete(tmp);
            session.flush();
        });
    }

    public void removeProcess(ProcessData proc) {
        transatomatic.run(session -> {
            ProcessData tmp = (ProcessData) session.get(ProcessData.class, proc.getId());
            session.delete(tmp);
            session.flush();
        });
    }

    public <T> T create(T task) {
        final ResultHolder<Object> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(task);
            session.flush();
            session.clear();
            resultHolder.setResult(task);
        });
        return (T) resultHolder.getResult();
    }

    public void saveTaskHistory(TaskHistory t) {
        transatomatic.run(session -> {
            TaskHistory taskHistory = (TaskHistory) session.get(TaskHistory.class, t.getId());
            taskHistory.setRunState(t.getRunState());
            taskHistory.setRunStatus(t.getRunStatus());
            taskHistory.setSequence(t.getSequence());
            taskHistory.setLogs(t.getLogs());
            taskHistory.setStartTime(t.getStartTime());
            taskHistory.setFinishTime(t.getFinishTime());
            session.merge(taskHistory);
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
            session.merge(ph);
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
            session.merge(tmp);
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
        try {
            final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                Query q = session.createQuery("from ProcessData p where p.username=:username order by p.id asc");
                q.setParameter("username", username);
                //            q.setHint("eclipselink.refresh", "true");
                resultHolder.setResult(q.list());
            });
            return resultHolder.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public ProcessData getProcess(long id) {
        try {
            final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
                session.refresh(proc);
                resultHolder.setResult(proc);
            });
            return resultHolder.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        try {
            final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
                session.refresh(proc);
                resultHolder.setResult(proc.getProcessHistoryList());
            });
            return resultHolder.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(long id) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProcessHistory> getMySortedProcessHistoryList(String username) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ProcessHistory getProcessHistoryById(long id) {
        try {
            final ResultHolder<ProcessHistory> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                ProcessHistory proc = (ProcessHistory) session.get(ProcessHistory.class, id);
                session.refresh(proc);
                resultHolder.setResult(proc);
            });
            return resultHolder.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<TaskData> getProcessTasksById(long pid) {
        try {
            final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                Query q = session.createQuery("from TaskData t where t.process.id=:pid order by t.sequence");
                q.setParameter("pid", pid);
                //            q.setHint("eclipselink.refresh", "true");
                List<TaskData> taskList = q.list();
                resultHolder.setResult(taskList == null ? Collections.EMPTY_LIST : taskList);
            });
            return resultHolder.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }


    public List<TaskData> getSortedTasksByProcessId(long pid) {
        try {
            final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
            transatomatic.run(session -> {
                Query q = session.createQuery("from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
                q.setParameter("pid", pid);
                List<TaskData> taskList = q.list();
                resultHolder.setResult(taskList);
            });
            return resultHolder.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<TaskData> getProcessTasks(long pid) {
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public String getDevEmailCSV() throws RemoteException {
        return serverSettings.getDevEmailCSV();
    }
}
