package com.shunya.server.component;

import com.shunya.kb.jpa.Document;
import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.punter.gui.PunterJobBasket;
import com.shunya.punter.jpa.ProcessData;
import com.shunya.server.PunterProcessRunMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/punter")
public class PunterController {
    final Logger logger = LoggerFactory.getLogger(PunterController.class);
    @Autowired
    private StaticDaoFacade service;

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
        try {
            Document document = new Document();
            document.setId(id);
            document = service.getDocument(document);
            return document;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    @ResponseBody
    public Map getDocument(@RequestBody PunterProcessRunMessage runMessage) throws InterruptedException {
        PunterJobBasket.getInstance().addJobToBasket(runMessage);
        return runMessage.get();
    }
}
