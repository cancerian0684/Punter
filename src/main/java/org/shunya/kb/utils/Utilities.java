package org.shunya.kb.utils;

import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static <T> void save(T obj, String fileName) throws IOException {
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"));
        File file = new File(path.resolve(fileName).toUri());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF8"))) {
            objectWriter.writeValue(out, obj);
        }
    }

    public static <T> T loadJson(Class<T> clazz, String fileName) throws IllegalAccessException, InstantiationException, IOException {
        logger.info("Loading File : " + fileName);
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"));
        File file = new File(path.resolve(fileName).toUri());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader objectReader = mapper.reader();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            return objectReader.readValue(new MappingJsonFactory().createParser(IOUtils.toString(in)), clazz);
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
            System.err.println("Could not load XML file : " + fileName + ", creating a new instance.");
            e.printStackTrace();
        }
        return clazz.newInstance();
    }

    public static String substituteVariables(String inputString, Map<String, Object> variables) {
        List<String> vars = getVariablesFromString(inputString);
        for (String string : vars) {
            String variable = "#{" + string + "}";
            if (null == variables.get(string)) {
                throw new RuntimeException("Variable Binding not Found :" + variable);
            }
            String variableBinding = variables.get(string).toString();
            inputString = inputString.replace(variable, variableBinding);
        }
        return inputString;
    }

    private static List<String> getVariablesFromString(String test) {
        char prevChar = ' ';
        String var = "";
        List<String> vars = new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < test.length(); i++) {
            char ch = test.charAt(i);
            if (ch == '{' && prevChar == '#') {
                var = "";
                found = true;
            } else if (ch == '}') {
                found = false;
                if (!var.isEmpty())
                    vars.add(var);
                var = "";
            } else if (found) {
                var += ch;
            }
            prevChar = ch;
        }
        return vars;
    }
}
