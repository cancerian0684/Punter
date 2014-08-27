package org.shunya.server.component;

import org.apache.commons.io.IOUtils;
import org.shunya.kb.model.Document;
import org.shunya.punter.gui.PunterJobBasket;
import org.shunya.punter.jpa.ProcessData;
import org.shunya.punter.jpa.TaskData;
import org.shunya.server.ClipboardPunterMessage;
import org.shunya.server.PunterProcessRunMessage;
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
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping(value = "/punter")
public class PunterController {
    final Logger logger = LoggerFactory.getLogger(PunterController.class);
    @Autowired
    private StaticDaoFacade daoService;

    @Autowired
    private PunterService punterService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessData> index(@ModelAttribute("model") ModelMap model) throws Exception {
        List<ProcessData> historyList = daoService.getProcessList("Munish");
        System.out.println("historyList = " + historyList);
        return historyList;
    }

    @RequestMapping(value = "/doc/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Document getDocument(@ModelAttribute("model") ModelMap model, @PathVariable("id") long id) {
        return daoService.getDocument(id);
    }

    @RequestMapping(value = "/doc/list", method = RequestMethod.GET)
    @ResponseBody
    public List<Long> getDocument(@ModelAttribute("model") ModelMap model) {
        return daoService.getDocumentIds();
    }

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    @ResponseBody
    public Map runRemoteProcess(@RequestBody PunterProcessRunMessage runMessage) throws InterruptedException {
        PunterJobBasket.getInstance().addJobToBasket(runMessage);
        return runMessage.get();
    }

    @RequestMapping(value = "/clipboard", method = RequestMethod.POST)
    @ResponseBody
    public void processClipboardMsg(@RequestBody ClipboardPunterMessage copyMessage) throws InterruptedException {
        logger.info("received clipboard message");
        daoService.process(copyMessage);
    }

    @RequestMapping(value = "/runTask", method = RequestMethod.POST)
    @ResponseBody
    public Map runTask(@RequestBody TaskData taskData) throws InterruptedException, ExecutionException {
        return punterService.runTask(taskData);
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public FileSystemResource getFile(@PathVariable("id") Long id) throws IOException {
        return punterService.getFile(id);
    }

    @RequestMapping(value = "/uploads/{name}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public FileSystemResource downloadFile(@PathVariable("name") String name, HttpServletResponse response) throws IOException {
//        response.setContentType(file.getContentType());
//        response.setContentLength((new Long(file.getLength()).intValue()));
        response.setHeader( "Content-Disposition", "attachment;filename=" + name );
        return new FileSystemResource(new File("uploads", name));
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

    @RequestMapping(value = "/discSpace/{drive}", method = RequestMethod.GET)
    @ResponseBody
    public int getDocument(@ModelAttribute("model") ModelMap model, @PathVariable("drive") String drive) {
        return punterService.getPercentFree(drive);
    }
}
