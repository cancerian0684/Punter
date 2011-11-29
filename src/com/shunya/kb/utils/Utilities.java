package com.sapient.kb.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class Utilities {
    public String getAllThreadDump(){
        StringBuilder stringBuilder=new StringBuilder(1000);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(true, true);
//        Thread.currentThread().getStackTrace()
//        Thread.getAllStackTraces()
        for (ThreadInfo info : infos) {
          StackTraceElement[] stackTrace = info.getStackTrace();
            for( StackTraceElement ste : stackTrace ) {
                stringBuilder.append(ste + "\n");
            }
        }
        return stringBuilder.toString();
    }
}
