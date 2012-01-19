package com.shunya.kb.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utilities {
    private static final DateFormat simpleDateFormat = new SimpleDateFormat("HH'h:'mm'm:'ss's'");

    public String getAllThreadDump() {
        StringBuilder stringBuilder = new StringBuilder(1000);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(true, true);
//        Thread.currentThread().getStackTrace()
//        Thread.getAllStackTraces()
        for (ThreadInfo info : infos) {
            StackTraceElement[] stackTrace = info.getStackTrace();
            for (StackTraceElement ste : stackTrace) {
                stringBuilder.append(ste + "\n");
            }
        }
        return stringBuilder.toString();
    }

    public static String formatMillis(long millis) {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return simpleDateFormat.format(new Date(millis));
    }
}
