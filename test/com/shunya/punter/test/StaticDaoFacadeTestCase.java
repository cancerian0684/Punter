package com.shunya.punter.test;

import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.ProcessHistory;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.jpa.TaskHistory;
import com.shunya.server.StaticDaoFacade;
import com.shunya.server.model.JPATransatomatic;
import com.shunya.server.model.SessionCache;
import com.shunya.server.model.ThreadLocalSession;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StaticDaoFacadeTestCase {
    private StaticDaoFacade staticDaoFacade;
    private SessionCache sessionCache;
    private JPATransatomatic transatomatic;

    @BeforeClass
    public void setUp() {
        sessionCache = new ThreadLocalSession();
        transatomatic = new JPATransatomatic((ThreadLocalSession) sessionCache);
        staticDaoFacade = new StaticDaoFacade(sessionCache, transatomatic);
    }


    @Test
    public void createDocumentTest() throws RemoteException {
        Document document = staticDaoFacade.createDocument("Munish chandel");
        System.out.println("document = " + document.getCategory() + " - " + document.getId());
        document.setCategory("retro daisy");
        document = staticDaoFacade.saveDocument(document);
        System.out.println("document = " + document.getCategory() + " - " + document.getId());
    }

    @Test
    public void addAttachmentToDocument() {
        Document document = staticDaoFacade.createDocument("Munish chandel");
        document.setCategory("retro daisy");
        Attachment attachment = new Attachment();
        attachment.setAuthor("Munish Chandel");
        attachment.setDocument(document);
        staticDaoFacade.saveAttachment(attachment);
    }

    @Test
    public void testRemoveTask() throws Exception {
        com.shunya.punter.jpa.ProcessData process = new com.shunya.punter.jpa.ProcessData();
        process.setId(1L);
        for (int i = 0; i <= 10; i++) {
            TaskData task = new TaskData();
            task.setProcess(process);
            task.setName("Test Task");
            task = (TaskData) staticDaoFacade.save(task);
            System.out.println(task.getId());
            staticDaoFacade.removeTask(task);
            staticDaoFacade.listTask(task.getId());
        }
    }

    @Test
    public void testCreateProcessHistory() throws Exception {
        ProcessData proc = new ProcessData();
        proc.setId(1603L);
        ProcessHistory ph = new ProcessHistory();
        ph.setName("Test-1");
        ph.setStartTime(new Date());
        ph.setProcess(proc);
        staticDaoFacade.save(ph);
        TaskData task = new TaskData();
        task.setId(1602L);
        for (int i = 0; i <= 10; i++) {
            TaskHistory th = new TaskHistory();
            th.setProcessHistory(ph);
            th.setTask(task);
            th.setSequence(1);
            th.setStatus(true);
            th.setLogs("dummy logs");
            staticDaoFacade.save(th);
        }
    }

    @Test
    public void testGetProcessHistoryForProcessId() throws Exception {
        List<ProcessHistory> phl = staticDaoFacade.getProcessHistoryListForProcessId(1603L);
        for (ProcessHistory processHistory : phl) {
            System.out.println(processHistory.getId());
        }
    }

    @Test
    public void testGetProcessTaskHistoryForProcessHistoryId() throws Exception {
        List<ProcessHistory> phl = staticDaoFacade.getProcessHistoryListForProcessId(1603L);
        for (ProcessHistory processHistory : phl) {
            System.out.println(processHistory.getId());
        }
    }

    @Test
    public void testListTask() throws Exception {
        com.shunya.punter.jpa.ProcessData process = new com.shunya.punter.jpa.ProcessData();
        process.setId(712L);
        List<TaskData> t1 = staticDaoFacade.getProcessTasks(712L);
        TaskData task = new TaskData();
        task.setProcess(process);
        task.setName("Test Task");
        task = (TaskData) staticDaoFacade.save(task);
        List<TaskData> t2 = staticDaoFacade.getProcessTasks(712L);
        assertEquals(1, (t2.size() - t1.size()));
    }

    @Test
    public void testGetProcessList() throws Exception {
        com.shunya.punter.jpa.ProcessData process = new com.shunya.punter.jpa.ProcessData();
        process.setName("UBS-101");
        List<ProcessData> pl1 = staticDaoFacade.getProcessList(AppSettings.getInstance().getUsername());
        staticDaoFacade.save(process);
        List<ProcessData> pl2 = staticDaoFacade.getProcessList(AppSettings.getInstance().getUsername());
        assertEquals(1, (pl2.size() - pl1.size()));
    }

    @Test
    public void testGetProcess() throws Exception {
        List<ProcessData> pl1 = staticDaoFacade.getProcessList(AppSettings.getInstance().getUsername());
        for (ProcessData process : pl1) {
            System.out.println(process.getId() + " -- " + process.getName());
        }
    }

    @Test
    public void testGetProcessTasksById() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testGetProcessTasks() {
        fail("Not yet implemented"); // TODO
    }

}
