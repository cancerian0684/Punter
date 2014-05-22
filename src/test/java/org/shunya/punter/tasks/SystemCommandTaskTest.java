package org.shunya.punter.tasks;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/3/11
 * Time: 8:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SystemCommandTaskTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRunSystemCommand1() throws Exception {
        SystemCommandTask systemCommandTask = new SystemCommandTask();
        systemCommandTask.systemCommand = "netstat -ab";
        systemCommandTask.run();
        String logs = systemCommandTask.getMemoryLogs();
        assertNotNull(logs.length());
    }

    @Test
    public void testRunSystemCommand2() throws Exception {
        SystemCommandTask systemCommandTask = new SystemCommandTask();
        systemCommandTask.systemCommand = "ping www.google.com";
        systemCommandTask.run();
        String logs = systemCommandTask.getMemoryLogs();
        assertNotNull(logs.length());
    }

    @Test
    public void testRunSystemCommand3() throws Exception {
        SystemCommandTask systemCommandTask = new SystemCommandTask();
        systemCommandTask.systemCommand = "java -version";
        systemCommandTask.run();
        String logs = systemCommandTask.getMemoryLogs();
        assertNotNull(logs.length());
    }
}
