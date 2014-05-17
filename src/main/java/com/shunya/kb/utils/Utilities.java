package com.shunya.kb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utilities {
    static final Logger logger = LoggerFactory.getLogger(Utilities.class);
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

    public static <T> void save(Class<T> clazz, T obj, String fileName) {
        try {
            Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"));
            File file = new File(path.resolve(fileName).toUri());
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(obj, file);
            logger.info("Persisting File : " + fileName);
            marshaller.marshal(obj, System.out);
        } catch (JAXBException e) {
            System.err.println("Error saving settings file to disk.");
            e.printStackTrace();
        }
    }

    public static <T> T load(Class<T> clazz, String fileName) throws IllegalAccessException, InstantiationException {
        try {
            logger.info("Loading File : " + fileName);
            Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"));
            File file = new File(path.resolve(fileName).toUri());
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            System.err.println("Could not load XML file : " + fileName+ ", creating a new instance.");
            e.printStackTrace();
        }
        return clazz.newInstance();
    }
}
