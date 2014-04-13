package com.shunya.server;

import com.shunya.kb.gui.SearchQuery;
import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.kb.jpa.SynonymWord;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.punter.tasks.Process;
import com.shunya.punter.utils.FieldPropertiesMap;
import com.shunya.server.model.JPATransatomatic;
import com.shunya.server.model.ResultHolder;
import com.shunya.server.model.SessionCache;
import com.shunya.server.model.Transatomatic;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticDaoFacade {
    private SessionCache sessionCache;
    private JPATransatomatic transatomatic;
    private ServerSettings settings;

    public void setSettings(ServerSettings settings) {
        this.settings = settings;
    }

    public StaticDaoFacade(SessionCache sessionCache, JPATransatomatic transatomatic) {
        this.sessionCache = sessionCache;
        this.transatomatic = transatomatic;
    }

    public Session getSession() {
        return sessionCache.getUnderlyingEntityManager();
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<String>(20);
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

    public void updateAccessCounter(Document doc) {
        /*Session em = emf.createEntityManager();
          DocumentService service = new DocumentService(em);
          em.getTransaction().begin();
          service.updateAccessCounter(doc);
          em.getTransaction().commit();
          em.close();*/
    }

    public Document createDocument(final String author) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(new Transatomatic.UnitOfWork() {
            @Override
            public void run() {
                Session em = getSession();
                Document doc = new Document();
                doc.setTitle("test title");
                doc.setContent("".getBytes());
                doc.setDateCreated(new Date());
                doc.setDateUpdated(new Date());
                doc.setCategory("/all");
                doc.setAuthor(author);
                em.persist(doc);
                em.flush();
                LuceneIndexDao.getInstance().indexDocs(doc);
                resultHolder.setResult(doc);
            }
        });
        return resultHolder.getResult();
    }

    public List<Document> getDocList(SearchQuery query) throws IOException {
        long t1 = System.currentTimeMillis();
        List<Document> result = LuceneIndexDao.getInstance().search(query.getQuery(), query.getCategory(), query.isAndFilter(), 0, query.getMaxResults());
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
        return result;
    }

    public void deleteAllForCategory(String category) throws IOException {
        long t1 = System.currentTimeMillis();
        List<Document> result = LuceneIndexDao.getInstance().search("*", category, true, 0, 100);
        for (Document document : result) {
            System.out.println("Deleting document - " + document);
            deleteDocument(document);
        }
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
    }

    public List<String> getAllTerms() throws IOException {
        long t1 = System.currentTimeMillis();
        List<String> result = LuceneIndexDao.getInstance().listAllTermsForTitle();
        long t2 = System.currentTimeMillis();
        System.err.println("time consumed : " + (t2 - t1));
        return result;
    }

    public SynonymWord create(final SynonymWord words) {
        final ResultHolder<SynonymWord> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
                    Session em = getSession();
                    em.persist(words);
                    em.flush();
                    resultHolder.setResult(words);
                }
        );
        return resultHolder.getResult();
    }

    public SynonymWord saveSynonymWords(final SynonymWord doc) {
        final ResultHolder<SynonymWord> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            SynonymWord document = (SynonymWord) em.merge(doc);
            SynonymService.getService().addWords(document.getWords());
            resultHolder.setResult(document);
        });
        return resultHolder.getResult();
    }

    public Document saveDocument(final Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            em.saveOrUpdate(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(doc);
        });
        return resultHolder.getResult();
    }

    public Attachment mergeAttachment(final Attachment attach) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Attachment attachment = (Attachment) em.merge(attach);
            em.flush();
            Document doc = attachment.getDocument();
            doc = (Document) em.get(Document.class, doc.getId());
            em.refresh(doc);
            em.getTransaction().commit();
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(attachment);
        });
        return resultHolder.getResult();
    }

    public Attachment saveAttachment(final Attachment attach) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<Attachment>();
        transatomatic.run(() -> {
            Session em = getSession();
            em.persist(attach);
            em.flush();
            Document doc = attach.getDocument();
            doc = (Document) em.get(Document.class, doc.getId());
            em.refresh(doc);
            em.getTransaction().commit();
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(attach);
        });
        return resultHolder.getResult();
    }

    public Document getDocument(final Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Document document = (Document) em.get(Document.class, doc.getId());
            em.refresh(document);
            resultHolder.setResult(document);
        });
        return resultHolder.getResult();
    }

    public Attachment getAttachment(final Attachment attachment) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Attachment persisted = (Attachment) em.get(Attachment.class, attachment.getId());
            em.refresh(persisted);
            resultHolder.setResult(persisted);
        });
        return resultHolder.getResult();
    }

    public boolean deleteAttachment(final Attachment attch) {
        transatomatic.run(() -> {
            Session em = getSession();
            Attachment attchment = (Attachment) em.get(Attachment.class, attch.getId());
            em.delete(attchment);
            em.flush();
            Document doc = attchment.getDocument();
            doc = (Document) em.get(Document.class, doc.getId());
            em.refresh(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
        });
        return true;
    }

    public boolean deleteDocument(final Document doc) {
        transatomatic.run(() -> {
            Session em = getSession();
            Document document = (Document) em.get(Document.class, doc.getId());
            em.delete(document);
            em.flush();
            LuceneIndexDao.getInstance().deleteIndexForDoc(document);
        });
        return true;
    }

    public void buildSynonymCache() {
        transatomatic.run(() -> {
            System.out.println("Rebuilding Synonym Cache");
            Session em = getSession();
            Query query = em.createQuery("SELECT e FROM SynonymWord e");
            List<SynonymWord> allDocs = query.list();
            for (SynonymWord synonymWord : allDocs) {
                System.out.println(synonymWord.getWords());
                SynonymService.getService().addWords(synonymWord.getWords());
            }
        });
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

    public void rebuildIndex() {
        transatomatic.run(() -> {
            System.out.println("Clearing old index");
            LuceneIndexDao.getInstance().deleteIndex();
            Session em = getSession();
            Query query = em.createQuery("SELECT e FROM Document e");
            List<Document> allDocs = query.list();
            for (Document emp : allDocs) {
                System.out.println(emp.getCategory());
                LuceneIndexDao.getInstance().indexDocs(emp);
            }
        });
    }

    public void removeTask(final TaskData task) throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            TaskData tmp = (TaskData) em.get(TaskData.class, task.getId());
            em.delete(tmp);
            em.flush();
        });

    }

    public void removeProcess(final ProcessData proc) throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            ProcessData tmp = (ProcessData) em.get(ProcessData.class, proc.getId());
            em.delete(tmp);
            em.flush();
        });

    }

    public void saveAll(final Object... objects) throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            for (Object object : objects) {
                em.persist(object);
            }
            em.flush();
            em.clear();
        });
    }

    public Object save(final Object object) throws Exception {
        final ResultHolder<Object> resultHolder = new ResultHolder<Object>();
        transatomatic.run(() -> {
            Session em = getSession();
            em.persist(object);
            em.flush();
            em.clear();
            resultHolder.setResult(object);
        });
        return resultHolder.getResult();
    }

    public void saveTaskHistory(final TaskHistory t) throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            TaskHistory taskHistory = (TaskHistory) em.get(TaskHistory.class, t.getId());
            taskHistory.setRunState(t.getRunState());
            taskHistory.setRunStatus(t.getRunStatus());
            taskHistory.setSequence(t.getSequence());
            taskHistory.setLogs(t.getLogs());
            taskHistory.setStartTime(t.getStartTime());
            taskHistory.setFinishTime(t.getFinishTime());
            em.merge(taskHistory);
            em.flush();
        });
    }

    public void saveProcessHistory(final ProcessHistory procHistory)
            throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            ProcessHistory ph = (ProcessHistory) em.get(ProcessHistory.class, procHistory.getId());
            ph.setRunState(procHistory.getRunState());
            ph.setRunStatus(procHistory.getRunStatus());
            ph.setStartTime(procHistory.getStartTime());
            ph.setFinishTime(procHistory.getFinishTime());
            ph.setClearAlert(procHistory.isClearAlert());
            em.merge(ph);
        });
    }

    public TaskData saveTask(final TaskData t) throws Exception {
        final ResultHolder<TaskData> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            TaskData tmp = (TaskData) em.get(TaskData.class, t.getId());
            //			em.lock(tmp, LockModeType.READ);
            tmp.setActive(t.isActive());
            tmp.setAuthor(t.getAuthor());
            tmp.setClassName(t.getClassName());
            tmp.setDescription(t.getDescription());
            try {
                tmp.setInputParams(t.getInputParams());
                tmp.setOutputParams(t.getOutputParams());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            tmp.setName(t.getName());
            tmp.setProcess(t.getProcess());
            tmp.setSequence(t.getSequence());
            tmp.setFailOver(t.isFailOver());
            em.merge(tmp);
            em.flush();
            resultHolder.setResult(tmp);
        });
        return resultHolder.getResult();
    }

    public ProcessData saveProcess(final ProcessData p) throws Exception {
        final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            try {
                Session em = getSession();
                ProcessData tmp = (ProcessData) em.get(ProcessData.class, p.getId());
                //			em.lock(tmp, LockModeType.READ);
                tmp.setName(p.getName());
                tmp.setInputParams(p.getInputParams());
                tmp.setUsername(p.getUsername());
                tmp.setDescription(p.getDescription());
                em.flush();
                resultHolder.setResult(p);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return resultHolder.getResult();
    }

    public void listTask(final long id) throws Exception {
        transatomatic.run(() -> {
            Session em = getSession();
            TaskData task = (TaskData) em.get(TaskData.class, id);
            try {
                if (task != null) {
                    System.out.println("Listing task for " + task.getId());
                    Set<String> keySet = task.getInputParams().keySet();
                    for (String object : keySet) {
                        System.out.println(object.toString() + " -- "
                                + task.getInputParams().get(object));
                    }
                    task.getOutputParams();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public List<ProcessData> getScheduledProcessList(final String username) throws Exception {
        final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from ProcessData p where p.username=:username");
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
    }

    public List<ProcessData> getProcessList(final String username) throws Exception {
        final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from ProcessData p where p.username=:username order by p.id asc");
            q.setParameter("username", username);
//            q.setHint("eclipselink.refresh", "true");
            resultHolder.setResult(q.list());
        });
        return resultHolder.getResult();
    }

    public void updateAllProcessProperties() {
        transatomatic.run(() -> {
            Session em = getSession();
            try {
                Query q = em.createQuery("from ProcessData p");
//                q.setHint("eclipselink.refresh", "true");
                List<ProcessData> processList = q.list();
                for (ProcessData processData : processList) {
                    FieldPropertiesMap inProp = Process.listInputParams();
                    processData.setInputParams(inProp);
                    try {
                        ProcessData tmp = (ProcessData) em.get(ProcessData.class, processData.getId());
                        tmp.setInputParams(processData.getInputParams());
                        em.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }

    public ProcessData getProcess(final long id) throws Exception {
        final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            ProcessData proc = (ProcessData) em.get(ProcessData.class, id);
            em.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public TaskHistory getTaskDao(final TaskHistory td) throws Exception {
        final ResultHolder<TaskHistory> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            TaskHistory proc = (TaskHistory) em.get(TaskHistory.class, td.getId());
            em.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public List<ProcessHistory> getProcessHistoryListForProcessId(final long id) throws Exception {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            ProcessData proc = (ProcessData) em.get(ProcessData.class, id);
            em.refresh(proc);
            resultHolder.setResult(proc.getProcessHistoryList());
        });
        return resultHolder.getResult();

    }

    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(final long id) throws Exception {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
//            q.setHint("eclipselink.refresh", "true");
            q.setParameter("pid", id);
            q.setFirstResult(0);
            q.setMaxResults(settings.getMaxProcessHistory());
            List<ProcessHistory> processHistoryList = q.list();
            resultHolder.setResult(processHistoryList);
        });
        return resultHolder.getResult();
    }

    public ProcessHistory getProcessHistoryById(final long id) throws Exception {
        final ResultHolder<ProcessHistory> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            ProcessHistory proc = (ProcessHistory) em.get(ProcessHistory.class, id);
            em.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public List<TaskData> getProcessTasksById(final long pid) throws Exception {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from TaskData t where t.process.id=:pid order by t.sequence");
            q.setParameter("pid", pid);
//            q.setHint("eclipselink.refresh", "true");
            List<TaskData> taskList = q.list();
            resultHolder.setResult(taskList == null ? Collections.EMPTY_LIST : taskList);
        });
        return resultHolder.getResult();

    }

    public List<TaskData> getSortedTasksByProcessId(final long pid) throws Exception {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
            q.setParameter("pid", pid);
            List<TaskData> taskList = q.list();
            resultHolder.setResult(taskList);
        });
        return resultHolder.getResult();
    }

    public List<TaskData> getProcessTasks(final long pid) throws Exception {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from ProcessData p where p.id=:pid");
            q.setParameter("pid", pid);
            List<ProcessData> processList = q.list();
            System.out.println(processList.get(0).getDescription());
            List<TaskData> tl = processList.get(0).getTaskList();
            resultHolder.setResult(tl);
        });
        return resultHolder.getResult();

    }

    public int deleteStaleHistory(final int days) {
        AtomicInteger counter =  new AtomicInteger(0);
        transatomatic.run(() -> {
            try {
                Session session = getSession();
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

    public List<ProcessHistory> getMySortedProcessHistoryList(final String username) {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(() -> {
            Session em = getSession();
            Query q = em.createQuery("from ProcessHistory ph where ph.process.username = :username AND ph.clearAlert=false order by ph.startTime desc");
//            q.setHint("eclipselink.refresh", "true");
            q.setParameter("username", username);
            //    q.setParameter("runStatus", RunStatus.SUCCESS);
            q.setFirstResult(0);
            q.setMaxResults(settings.getMaxProcessAlerts());
            List<ProcessHistory> processHistoryList = q.list();
            resultHolder.setResult(processHistoryList);
        });
        return resultHolder.getResult();
    }

    public void compressTables() {
        transatomatic.run(() -> {
            try {
                String[] tables = {"PROCESSHISTORY", "PROCESS", "TASK", "TASKHISTORY", "DOCUMENT", "DOCUMENT_LOB", "ATTACHMENT", "ATTACHMENT_LOB"};
                Session em = getSession();
                for (String string : tables) {
                    Query q = em.createQuery("call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('PUNTER', '" + string + "', 1)");
                    q.executeUpdate();
                    em.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}