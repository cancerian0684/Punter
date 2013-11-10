package com.shunya.kb.utils;/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Copyright (c) 1997-2001 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */


import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This program traces the execution of another program.
 * See "java com.shunya.kb.utils.Trace -help".
 * It is a simple example of the use of the Java Debug Interface.
 *
 * @author Robert Field
 * @version %Z% %M% %I% %E% %T%
 */
public class Trace {

    // Running remote VM
    private final VirtualMachine vm;

    // Thread transferring remote error stream to our error stream
    private Thread errThread = null;

    // Thread transferring remote output stream to our output stream
    private Thread outThread = null;

    // Mode for tracing the com.shunya.kb.utils.Trace program (default= 0 off)
    private int debugTraceMode = 0;

    //  Do we want to watch assignments to fields
    private boolean watchFields = false;

    // Class patterns for which we don't want events
    private String[] excludes = {"java.*", "javax.*", "sun.*",
            "com.sun.*", "org.*", "$*", "com.shunya.kb.gui.DocumentTableModel.*", "*getRowCount*"};

    private String[] includes={"com.shunya.kb.*"};
    /**
     * main
     */
    public static void main(String[] args) {

        new Trace(args);
    }

    /**
     * Parse the command line arguments.
     * Launch target VM.
     * Generate the trace.
     */
    Trace(String[] args) {
        PrintWriter writer = new PrintWriter(System.out);
        int inx;
        for (inx = 0; inx < args.length; ++inx) {
            String arg = args[inx];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.equals("-output")) {
                try {
                    writer = new PrintWriter(new FileWriter(args[++inx]));
                } catch (IOException exc) {
                    System.err.println("Cannot open output file: " + args[inx]
                            + " - " + exc);
                    System.exit(1);
                }
            } else if (arg.equals("-all")) {
                excludes = new String[0];
            } else if (arg.equals("-fields")) {
                watchFields = true;
            } else if (arg.equals("-dbgtrace")) {
                debugTraceMode = Integer.parseInt(args[++inx]);
            } else if (arg.equals("-help")) {
                usage();
                System.exit(0);
            } else {
                System.err.println("No option: " + arg);
                usage();
                System.exit(1);
            }
        }
        if (inx >= args.length) {
            System.err.println("<class> missing");
            usage();
            System.exit(1);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(args[inx]);
        for (++inx; inx < args.length; ++inx) {
            sb.append(' ');
            sb.append(args[inx]);
        }
        vm = launchTarget(sb.toString());
        generateTrace(writer);
    }


    /**
     * Generate the trace.
     * Enable events, start thread to display events,
     * start threads to forward remote error and output streams,
     * resume the remote VM, wait for the final event, and shutdown.
     */
    void generateTrace(PrintWriter writer) {
        vm.setDebugTraceMode(debugTraceMode);
        EventThread eventThread = new EventThread(vm, excludes, writer, includes);
        eventThread.setEventRequests(watchFields);
        eventThread.start();
        registerMBean(eventThread);
        redirectOutput();
        vm.resume();

        // Shutdown begins when event thread terminates
        try {
            eventThread.join();
            errThread.join(); // Make sure output is forwarded
            outThread.join(); // before we exit
        } catch (InterruptedException exc) {
            // we don't interrupt
        }
        writer.close();
    }

    private void registerMBean(EventThread eventThread) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(eventThread, new ObjectName("EventThread:type=EventThread"));
            System.err.println("AppSettings registered with MBean Server.");
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch target VM.
     * Forward target's output and error.
     */
    VirtualMachine launchTarget(String mainArgs) {
        LaunchingConnector connector = findLaunchingConnector();
        Map arguments = connectorArguments(connector, mainArgs);
        try {
            return connector.launch(arguments);
        } catch (IOException exc) {
            throw new Error("Unable to launch target VM: " + exc);
        } catch (IllegalConnectorArgumentsException exc) {
            throw new Error("Internal error: " + exc);
        } catch (VMStartException exc) {
            throw new Error("Target VM failed to initialize: " +
                    exc.getMessage());
        }
    }

    void redirectOutput() {
        Process process = vm.process();
        // Copy target's output and error to our output and error.
        errThread = new StreamRedirectThread("error reader",
                process.getErrorStream(),
                System.err);
        outThread = new StreamRedirectThread("output reader",
                process.getInputStream(),
                System.out);
        errThread.start();
        outThread.start();
    }

    /**
     * Find a com.sun.jdi.CommandLineLaunch connector
     */
    LaunchingConnector findLaunchingConnector() {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector) iter.next();
            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                return (LaunchingConnector) connector;
            }
        }
        throw new Error("No launching connector");
    }

    /**
     * Return the launching connector's arguments.
     */
    Map connectorArguments(LaunchingConnector connector, String mainArgs) {
        Map arguments = connector.defaultArguments();
        Connector.Argument mainArg = (Connector.Argument) arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }
        mainArg.setValue(mainArgs);
        Connector.Argument optsArg = (Connector.Argument) arguments.get("options");
        if (optsArg == null) {
            throw new Error("Bad launching connector");
        }
        String cp = "-Xms1024m -cp \"" + System.getProperty("java.class.path") + "\"";
        optsArg.setValue(cp);

        if (watchFields) { // We need a VM that supports watchpoints
            Connector.Argument optionArg = (Connector.Argument) arguments.get("options");
            if (optionArg == null) {
                throw new Error("Bad launching connector");
            }
            optionArg.setValue("-classic");
        }
        return arguments;
    }

    /**
     * Print command line usage help
     */
    void usage() {
        System.err.println("Usage: java com.shunya.kb.utils.Trace <options> <class> <args>");
        System.err.println("<options> are:");
        System.err.println(
                "  -output <filename>   Output trace to <filename>");
        System.err.println(
                "  -all                 Include system classes in output");
        System.err.println(
                "  -help                Print this help message");
        System.err.println("<class> is the program to trace");
        System.err.println("<args> are the arguments to <class>");
    }
}