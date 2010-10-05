package com.sapient.punter.executors;

import it.sauronsoftware.cron4j.SchedulingPattern;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sapient.punter.gui.PunterGUI;
import com.sapient.punter.jpa.ProcessData;
import com.sapient.punter.jpa.StaticDaoFacade;
import com.sapient.punter.utils.Stopwatch;

public class ScheduledJobPicker {
	private PunterGUI guiReference;
	private static long TIMER_PERIOD=1000*60*1;
	private static long lastReferenceTimeLong;
	private Timer timer;
	public void setGuiReference(PunterGUI guiReference) {
		this.guiReference = guiReference;
	}
	public ScheduledJobPicker() {
		timer=new Timer(true);
		final Stopwatch sw=new Stopwatch();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					sw.start();
					List<ProcessData> scheduledProcList = StaticDaoFacade.getScheduledProcessList();
//					System.err.println(scheduledProcList.size()+" == "+sw.getElapsedTime()+ " ms");
					sw.reset();
					if(lastReferenceTimeLong==0L){
						lastReferenceTimeLong=System.currentTimeMillis();
					}
					for (ProcessData pd : scheduledProcList) {
						String ss=pd.getInputParams().get("scheduleString").getValue().trim();
						if(checkIfScheduledInPeriod(ss,lastReferenceTimeLong,TIMER_PERIOD)){
							System.err.println(checkIfScheduledInPeriod(ss,lastReferenceTimeLong,TIMER_PERIOD)+" "+new Date());							
							guiReference.createProcess(pd);
						}
					}
					lastReferenceTimeLong+=TIMER_PERIOD;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 10000, TIMER_PERIOD);
	}
	public static void main(String[] args) {
		
	}
	public static boolean checkIfScheduledInPeriod(String pattern,long refrenceTime,long delay){
		boolean tmp=false;
		SchedulingPattern sp=new SchedulingPattern(pattern);
		delay=delay-60*1000L;
		while(delay>=0){
			if(sp.match(refrenceTime+delay)){
				tmp=true;
				break;
			}
			delay=delay-60*1000L;
		}
		return tmp;
	}
}