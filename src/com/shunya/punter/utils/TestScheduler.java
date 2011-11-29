package com.sapient.punter.utils;

import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.SchedulingPattern;

public class TestScheduler {
public static void main(String[] args) {
	System.out.println(System.getProperty("user.name"));
	String pattern = "13 * * jan-jun,sep-dec mon-fri,sat";
	SchedulingPattern sp=new SchedulingPattern(pattern);
	System.out.println(sp.match(System.currentTimeMillis()-60*1000*60));
	Predictor p = new Predictor(pattern);
	for (int i = 0; i < 5; i++) {
	        System.out.println(p.nextMatchingDate());
	}
}
}