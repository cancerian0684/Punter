package com.shunya.server.component;

import com.shunya.kb.jpa.Document;
import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.punter.gui.PunterJobBasket;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.punter.jpa.TaskData;
import com.shunya.server.PunterProcessRunMessage;
import com.shunya.server.PunterWebDocumentHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/punter")
public class PunterController {
    final Logger logger = LoggerFactory.getLogger(PunterController.class);
    @Autowired
    private StaticDaoFacade service;

    @Autowired
    private PunterService punterService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessData> index(@ModelAttribute("model") ModelMap model) throws Exception {
        List<ProcessData> historyList = service.getProcessList("Munish");
        System.out.println("historyList = " + historyList);
        return historyList;
    }

    @RequestMapping(value = "/doc/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Document getDocument(@ModelAttribute("model") ModelMap model, @PathVariable("id") long id) {
        Document document = new Document();
        document.setId(id);
        document = service.getDocument(document);
        return document;
    }

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    @ResponseBody
    public Map runRemoteProcess(@RequestBody PunterProcessRunMessage runMessage) throws InterruptedException {
        PunterJobBasket.getInstance().addJobToBasket(runMessage);
        return runMessage.get();
    }

    @RequestMapping(value = "/runTask", method = RequestMethod.POST)
    @ResponseBody
    public Map runTask(@RequestBody TaskData taskData) throws InterruptedException {
        return punterService.runTask(taskData);
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public FileSystemResource get(@PathVariable("id") Long id) throws IOException {
        logger.info("Serving file : " + id);
        Document doc = new Document();
        doc.setId(id);
        doc = service.getDocument(doc);
        File targetFile = PunterWebDocumentHandler.process(doc);
        return new FileSystemResource(targetFile);
    }

    @RequestMapping(value = "/run/{host}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void runProcess(@PathVariable("id") Long processId, HttpServletRequest request, @PathVariable("host") String host) {
        PunterProcessRunMessage runMessage = new PunterProcessRunMessage();
        runMessage.setHostname(host);
        runMessage.setProcessId(processId);
        runMessage.setParams(parseQueryString(request.getQueryString()));
        PunterJobBasket.getInstance().addJobToBasket(runMessage);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileUpload(@RequestParam("name") String name, @RequestParam(value = "path", required = false) String path, @RequestParam("file") MultipartFile file) throws IOException {
        if (path == null)
            path = "uploads";
        File dir = new File(path);
        dir.mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(dir, name));
        IOUtils.copyLarge(file.getInputStream(), fileOutputStream);
        fileOutputStream.close();
        String absolutePath = new File(dir, name).getAbsolutePath();
        logger.info("File saved at location : {}", absolutePath);
        return absolutePath;
    }

    public static Map<String, String> parseQueryString(String data) {
        if (data.indexOf("?") == -1 || data.length() <= (data.indexOf("?") + 1))
            return Collections.emptyMap();
        data = data.substring(data.indexOf("?") + 1);
        Map<String, String> paramMap = new LinkedHashMap<>();
        for (String parameter : data.split("&")) {
            String[] entities = parameter.split("=");
            try {
                String key = URLDecoder.decode(entities[0], "utf-8");
                if (!paramMap.containsKey(key)) {
                    paramMap.put(key, URLDecoder.decode(entities[1], "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return paramMap;
    }
}
