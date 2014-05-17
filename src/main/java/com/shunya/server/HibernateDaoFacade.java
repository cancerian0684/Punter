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
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HibernateDaoFacade {
    private JPATransatomatic transatomatic;
    private ServerSettings settings;

    public void setSettings(ServerSettings settings) {
        this.settings = settings;
    }

    public HibernateDaoFacade(JPATransatomatic transatomatic) {
        this.transatomatic = transatomatic;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>(20);
        Scanner scanner = new Scanner(HibernateDaoFacade.class.getClassLoader().getResourceAsStream("resources/categories.properties"));
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
        transatomatic.run(session -> {
                    session.persist(words);
                    session.flush();
                    resultHolder.setResult(words);
                }
        );
        return resultHolder.getResult();
    }

    public SynonymWord saveSynonymWords(final SynonymWord doc) {
        final ResultHolder<SynonymWord> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            SynonymWord document = (SynonymWord) session.merge(doc);
            SynonymService.getService().addWords(document.getWords());
            resultHolder.setResult(document);
        });
        return resultHolder.getResult();
    }

    public Document saveDocument(final Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            session.saveOrUpdate(doc);
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(doc);
        });
        return resultHolder.getResult();
    }

    public Attachment mergeAttachment(final Attachment attach) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Attachment attachment = (Attachment) session.merge(attach);
            session.flush();
            Document doc = attachment.getDocument();
            doc = (Document) session.get(Document.class, doc.getId());
            session.refresh(doc);
            session.getTransaction().commit();
            LuceneIndexDao.getInstance().indexDocs(doc);
            resultHolder.setResult(attachment);
        });
        return resultHolder.getResult();
    }

    public Attachment saveAttachment(final Attachment attach) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<Attachment>();
        transatomatic.run(session -> {
            session.persist(attach);
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

    public Document getDocument(final Document doc) {
        final ResultHolder<Document> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Document document = (Document) session.get(Document.class, doc.getId());
            session.refresh(document);
            resultHolder.setResult(document);
        });
        return resultHolder.getResult();
    }

    public Attachment getAttachment(final Attachment attachment) {
        final ResultHolder<Attachment> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Attachment persisted = (Attachment) session.get(Attachment.class, attachment.getId());
            session.refresh(persisted);
            resultHolder.setResult(persisted);
        });
        return resultHolder.getResult();
    }

    public boolean deleteAttachment(final Attachment attch) {
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

    public boolean deleteDocument(final Document doc) {
        transatomatic.run(session -> {
            Document document = (Document) session.get(Document.class, doc.getId());
            session.delete(document);
            session.flush();
            LuceneIndexDao.getInstance().deleteIndexForDoc(document);
        });
        return true;
    }

    public void buildSynonymCache() {
        transatomatic.run(session -> {
            System.out.println("Rebuilding Synonym Cache");
            Query query = session.createQuery("SELECT e FROM SynonymWord e");
            List<SynonymWord> allDocs = query.list();
            for (SynonymWord synonymWord : allDocs) {
                System.out.println(synonymWord.getWords());
                SynonymService.getService().addWords(synonymWord.getWords());
            }
        });
    }

    public void buildSynonymsCacheLocal() {
        System.out.println("Rebuilding Synonym Cache Local");
        Scanner scanner = new Scanner(HibernateDaoFacade.class.getClassLoader().getResourceAsStream("resources/synonyms.properties"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            SynonymService.getService().addWords(line);
        }
        scanner.close();
    }

    public void rebuildIndex() {
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

    public void removeTask(final TaskData task) throws Exception {
        transatomatic.run(session -> {
            TaskData tmp = (TaskData) session.get(TaskData.class, task.getId());
            session.delete(tmp);
            session.flush();
        });

    }

    public void removeProcess(final ProcessData proc) throws Exception {
        transatomatic.run(session -> {
            ProcessData tmp = (ProcessData) session.get(ProcessData.class, proc.getId());
            session.delete(tmp);
            session.flush();
        });

    }

    public void saveAll(final Object... objects) throws Exception {
        transatomatic.run(session -> {
            for (Object object : objects) {
                session.persist(object);
            }
            session.flush();
            session.clear();
        });
    }

    public Object save(final Object object) throws Exception {
        final ResultHolder<Object> resultHolder = new ResultHolder<Object>();
        transatomatic.run(session -> {
            session.persist(object);
            session.flush();
            session.clear();
            resultHolder.setResult(object);
        });
        return resultHolder.getResult();
    }

    public void saveTaskHistory(final TaskHistory t) throws Exception {
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

    public void saveProcessHistory(final ProcessHistory procHistory)
            throws Exception {
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

    public TaskData saveTask(final TaskData t) throws Exception {
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

    public ProcessData saveProcess(final ProcessData p) throws Exception {
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

    public void listTask(final long id) throws Exception {
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

    public List<ProcessData> getScheduledProcessList(final String username) throws Exception {
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
    }

    public List<ProcessData> getProcessList(final String username) throws Exception {
        final ResultHolder<List<ProcessData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessData p where p.username=:username order by p.id asc");
            q.setParameter("username", username);
//            q.setHint("eclipselink.refresh", "true");
            resultHolder.setResult(q.list());
        });
        return resultHolder.getResult();
    }

    public void updateAllProcessProperties() {
        transatomatic.run(session -> {
            try {
                Query q = session.createQuery("from ProcessData p");
//                q.setHint("eclipselink.refresh", "true");
                List<ProcessData> processList = q.list();
                for (ProcessData processData : processList) {
                    FieldPropertiesMap inProp = Process.listInputParams();
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

    public ProcessData getProcess(final long id) throws Exception {
        final ResultHolder<ProcessData> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
            session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public TaskHistory getTaskDao(final TaskHistory td) throws Exception {
        final ResultHolder<TaskHistory> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            TaskHistory proc = (TaskHistory) session.get(TaskHistory.class, td.getId());
            session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public List<ProcessHistory> getProcessHistoryListForProcessId(final long id) throws Exception {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            ProcessData proc = (ProcessData) session.get(ProcessData.class, id);
            session.refresh(proc);
            resultHolder.setResult(proc.getProcessHistoryList());
        });
        return resultHolder.getResult();

    }

    public List<ProcessHistory> getSortedProcessHistoryListForProcessId(final long id) throws Exception {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessHistory ph where ph.process.id = :pid order by ph.id desc");
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
        transatomatic.run(session -> {
            ProcessHistory proc = (ProcessHistory) session.get(ProcessHistory.class, id);
            session.refresh(proc);
            resultHolder.setResult(proc);
        });
        return resultHolder.getResult();
    }

    public List<TaskData> getProcessTasksById(final long pid) throws Exception {
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

    public List<TaskData> getSortedTasksByProcessId(final long pid) throws Exception {
        final ResultHolder<List<TaskData>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from TaskData t where t.process.id=:pid and t.active=true order by t.sequence");
            q.setParameter("pid", pid);
            List<TaskData> taskList = q.list();
            resultHolder.setResult(taskList);
        });
        return resultHolder.getResult();
    }

    public List<TaskData> getProcessTasks(final long pid) throws Exception {
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

    public List<ProcessHistory> getMySortedProcessHistoryList(final String username) {
        final ResultHolder<List<ProcessHistory>> resultHolder = new ResultHolder<>();
        transatomatic.run(session -> {
            Query q = session.createQuery("from ProcessHistory ph where ph.process.username = :username AND ph.clearAlert=false order by ph.startTime desc");
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
}