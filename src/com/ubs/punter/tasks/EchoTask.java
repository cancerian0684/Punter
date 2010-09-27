package com.ubs.punter.tasks;

import java.util.Date;

import com.ubs.punter.Tasks;
import com.ubs.punter.annotations.InputParam;
import com.ubs.punter.annotations.OutputParam;

public class EchoTask extends Tasks {
	@InputParam(required = true)
	private String name;
	@InputParam(required = false)
	private int age;
	@InputParam(required = false)
	private Date dob;

	@OutputParam
	private String outName;

	@Override
	public void run() {
		System.out.println("Hello " + name);
		System.out.println("age " + age);
		System.out.println("dob " + dob);
		outName = "Hello " + name+" "+age+" "+dob;
	}
}