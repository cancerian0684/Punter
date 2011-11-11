package com.sapient.kb.utils;/*
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


import com.sapient.punter.utils.ClipBoardUtils;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class processes incoming JDI events and displays them
 *
 * @author Robert Field
 * @version %Z% %M% %I% %E% %T%
 */
public class EventThread extends Thread implements EventThreadMBean {

    private final VirtualMachine vm;   // Running VM
    private final String[] excludes;   // Packages to exclude
    private final String[] includes;   // Packages to exclude
    private final PrintWriter writer;  // Where output goes

    private boolean connected = true;  // Connected to VM
    private boolean vmDied = true;     // VMDeath occurred

    // Maps ThreadReference to ThreadTrace instances
    private Map<ThreadReference, ThreadTrace> traceMap = new ConcurrentHashMap<ThreadReference, ThreadTrace>();

    EventThread(VirtualMachine vm, String[] excludes, PrintWriter writer, String[] includes) {
        super("event-handler");
        this.vm = vm;
        this.excludes = excludes;
        this.writer = writer;
        this.includes = includes;
    }

    /**
     * Run the event handling thread.
     * As long as we are connected, get event sets off
     * the queue and dispatch the events within them.
     */
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    handleEvent(it.nextEvent());
                }
                eventSet.resume();
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
    }

    /**
     * Create the desired event requests, and enable
     * them so that we will get events.
     *
     * @param watchFields Do we want to watch assignments to fields
     */
    void setEventRequests(boolean watchFields) {
        EventRequestManager mgr = vm.eventRequestManager();

        // want all exceptions
        ExceptionRequest excReq = mgr.createExceptionRequest(null,
                true, true);
        // suspend so we can step
        excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        excReq.enable();

        MethodEntryRequest menr = mgr.createMethodEntryRequest();
        for (int i = 0; i < excludes.length; ++i) {
            menr.addClassExclusionFilter(excludes[i]);
        }
        for (String include : includes) {
               menr.addClassFilter(include);
        }
        menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        menr.enable();

        MethodExitRequest mexr = mgr.createMethodExitRequest();
        for (int i = 0; i < excludes.length; ++i) {
            mexr.addClassExclusionFilter(excludes[i]);
        }
         for (String include : includes) {
               mexr.addClassFilter(include);
        }
        mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        mexr.enable();

        ThreadDeathRequest tdr = mgr.createThreadDeathRequest();
        // Make sure we sync on thread death
        tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        tdr.enable();

        if (watchFields) {
            ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
            for (int i = 0; i < excludes.length; ++i) {
                cpr.addClassExclusionFilter(excludes[i]);
            }
            cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            cpr.enable();
        }
    }

    /**
     * This class keeps context on events in one thread.
     * In this implementation, context is the indentation prefix.
     */
    class ThreadTrace {
        private final ThreadReference thread;
        private StringBuffer indent;
        private StringWriter sw = new StringWriter();
        private String lastMethodCall = "";
        private final String threadName;

        public String getThreadName() {
            return threadName;
        }

        ThreadTrace(ThreadReference thread) {
            this.thread = thread;
            this.threadName = thread.name();
            indent = new StringBuffer("");
//            println("====== " + thread.name() + " ======");
        }

        private void println(String str) {
            sw.append(indent);
            sw.append(str + "\n");
            sw.flush();
        }

        void methodEntryEvent(MethodEntryEvent event) {
            try{
            String methodCall = event.method().declaringType().name() + "." + event.method().name() + "("+event.location().sourceName()+":"+event.location().lineNumber()+")";
            if (!lastMethodCall.equalsIgnoreCase(methodCall)) {
                println(methodCall);
                lastMethodCall = methodCall;
            }
            indent.append("| ");
            }catch (Exception e){e.printStackTrace();}
        }

        void methodExitEvent(MethodExitEvent event) {
            indent.setLength(indent.length() - 2);
        }

        void fieldWatchEvent(ModificationWatchpointEvent event) {
            Field field = event.field();
            Value value = event.valueToBe();
            println("    " + field.name() + " = " + value);
        }

        void exceptionEvent(ExceptionEvent event) {
            println("Exception: " + event.exception() +
                    " catch: " + event.catchLocation());
            // Step to the catch
            EventRequestManager mgr = vm.eventRequestManager();
            StepRequest req = mgr.createStepRequest(thread,
                    StepRequest.STEP_MIN,
                    StepRequest.STEP_INTO);
            req.addCountFilter(1);  // next step only
            req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            req.enable();
        }

        // Step to exception catch
        void stepEvent(StepEvent event) {
            // Adjust call depth
            int cnt = 0;
            indent = new StringBuffer("");
            try {
                cnt = thread.frameCount();
            } catch (IncompatibleThreadStateException exc) {
            }
            while (cnt-- > 0) {
                indent.append("| ");
            }

            EventRequestManager mgr = vm.eventRequestManager();
            mgr.deleteEventRequest(event.request());
        }

        public String reStartCapture(String note) {
            try {
                return sw.toString();
            } finally {
                sw.write("\n -----------" + note + "-----------" + new Date() + " \n");
            }
        }

        void threadDeathEvent(ThreadDeathEvent event) {
            indent = new StringBuffer("");
            writer.print(sw.toString());
//            writer.println("====== " + thread.name() + " end ======");
            writer.flush();
        }
    }

    /**
     * Returns the ThreadTrace instance for the specified thread,
     * creating one if needed.
     */
    ThreadTrace threadTrace(ThreadReference thread) {
        ThreadTrace trace = (ThreadTrace) traceMap.get(thread);
        if (trace == null) {
            trace = new ThreadTrace(thread);
            traceMap.put(thread, trace);
        }
        return trace;
    }

    @Override
    public void threadReport(String note) {
        StringWriter stringWriter = new StringWriter(10000);
        for (ThreadReference thread : traceMap.keySet()) {
            ThreadTrace trace = (ThreadTrace) traceMap.get(thread);
            stringWriter.write("\n===Start===============>>>" + trace.getThreadName() + "\n");
            stringWriter.write(trace.reStartCapture(note));
            stringWriter.write("\n===End===============>>>" + trace.getThreadName() + "\n");
        }
        stringWriter.flush();
        ClipBoardUtils.setClipboardContents(stringWriter.toString());
    }

    /**
     * Dispatch incoming events
     */
    private void handleEvent(Event event) {
        if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent) event);
        } else if (event instanceof ModificationWatchpointEvent) {
            fieldWatchEvent((ModificationWatchpointEvent) event);
        } else if (event instanceof MethodEntryEvent) {
            methodEntryEvent((MethodEntryEvent) event);
        } else if (event instanceof MethodExitEvent) {
            methodExitEvent((MethodExitEvent) event);
        } else if (event instanceof StepEvent) {
            stepEvent((StepEvent) event);
        } else if (event instanceof ThreadDeathEvent) {
            threadDeathEvent((ThreadDeathEvent) event);
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent((VMStartEvent) event);
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent) event);
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent((VMDisconnectEvent) event);
        } else {
            throw new Error("Unexpected event type");
        }
    }

    /**
     * A VMDisconnectedException has happened while dealing with
     * another event. We need to flush the event queue, dealing only
     * with exit events (VMDeath, VMDisconnect) so that we terminate
     * correctly.
     */
    synchronized void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent) event);
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent) event);
                    }
                }
                eventSet.resume(); // Resume the VM
            } catch (InterruptedException exc) {
                // ignore
            }
        }
    }

    private void vmStartEvent(VMStartEvent event) {
        writer.println("-- VM Started --");
    }

    // Forward event for thread specific processing
    private void methodEntryEvent(MethodEntryEvent event) {
        threadTrace(event.thread()).methodEntryEvent(event);
    }

    // Forward event for thread specific processing
    private void methodExitEvent(MethodExitEvent event) {
        threadTrace(event.thread()).methodExitEvent(event);
    }

    // Forward event for thread specific processing
    private void stepEvent(StepEvent event) {
        threadTrace(event.thread()).stepEvent(event);
    }

    // Forward event for thread specific processing
    private void fieldWatchEvent(ModificationWatchpointEvent event) {
        threadTrace(event.thread()).fieldWatchEvent(event);
    }

    void threadDeathEvent(ThreadDeathEvent event) {
        ThreadTrace trace = (ThreadTrace) traceMap.get(event.thread());
        if (trace != null) {  // only want threads we care about
            trace.threadDeathEvent(event);   // Forward event
        }
    }

    /**
     * A new class has been loaded.
     * Set watchpoints on each of its fields
     */
    private void classPrepareEvent(ClassPrepareEvent event) {
        EventRequestManager mgr = vm.eventRequestManager();
        List fields = event.referenceType().visibleFields();
        for (Iterator it = fields.iterator(); it.hasNext(); ) {
            Field field = (Field) it.next();
            ModificationWatchpointRequest req =
                    mgr.createModificationWatchpointRequest(field);
            for (int i = 0; i < excludes.length; ++i) {
                req.addClassExclusionFilter(excludes[i]);
            }
            req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            req.enable();
        }
    }

    private void exceptionEvent(ExceptionEvent event) {
        ThreadTrace trace = (ThreadTrace) traceMap.get(event.thread());
        if (trace != null) {  // only want threads we care about
            // trace.exceptionEvent(event); // Forward event
        }
    }

    public void vmDeathEvent(VMDeathEvent event) {
        vmDied = true;
        writer.println("-- The application exited --");
    }

    public void vmDisconnectEvent(VMDisconnectEvent event) {
        connected = false;
        if (!vmDied) {
            writer.println("-- The application has been disconnected --");
        }
    }
}
