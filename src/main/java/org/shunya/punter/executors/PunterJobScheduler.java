package org.shunya.punter.executors;

import it.sauronsoftware.cron4j.SchedulingPattern;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.gui.PunterJobBasket;
import org.shunya.punter.jpa.ProcessData;
import org.shunya.server.PunterProcessRunMessage;
import org.shunya.server.component.DBService;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PunterJobScheduler extends Timer {
    private static long TIMER_PERIOD = 1000 * 60 * 1;
    private static long lastReferenceTimeLong;
    private final DBService dbService;

    public PunterJobScheduler(DBService dbService) {
        super(true);
        this.dbService = dbService;
    }

    public void start() {
        scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<ProcessData> scheduledProcList = dbService.getScheduledProcessList(AppSettings.getInstance().getUsername());
                    if (lastReferenceTimeLong == 0L) {
                        lastReferenceTimeLong = System.currentTimeMillis();
                    }
                    for (ProcessData pd : scheduledProcList) {
                        String ss = pd.getInputParams().get("scheduleString").getValue().trim();
                        if (checkIfScheduledInPeriod(ss, lastReferenceTimeLong, TIMER_PERIOD)) {
                            System.err.println(checkIfScheduledInPeriod(ss, lastReferenceTimeLong, TIMER_PERIOD) + " " + new Date());
                            PunterProcessRunMessage punterProcessRunMessage=new PunterProcessRunMessage();
                            punterProcessRunMessage.setProcessId(pd.getId());
                            PunterJobBasket.getInstance().addJobToBasket(punterProcessRunMessage);
                        }
                    }
                    lastReferenceTimeLong += TIMER_PERIOD;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000, TIMER_PERIOD);
    }

    public void stop() {
        cancel();
    }

    private boolean checkIfScheduledInPeriod(String pattern, long refrenceTime, long delay) {
        boolean tmp = false;
        SchedulingPattern sp = new SchedulingPattern(pattern);
        delay = delay - 60 * 1000L;
        while (delay >= 0) {
            if (sp.match(refrenceTime + delay)) {
                tmp = true;
                break;
            }
            delay = delay - 60 * 1000L;
        }
        return tmp;
    }
}