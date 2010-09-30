package com.sapient.punter.utils;

public class Test {
public static void main(String[] args) {
	String taskPackage="com.sapient.punter.tasks";;
	Package pkg=Package.getPackage(taskPackage);
	
	double f=3.226;
	double g=f+1;
	System.out.println(f);
	System.out.println(g);
	System.out.println(Double.parseDouble("12560.23565498794645465")*.2);
	System.out.println("--- Normal Print-----");
	System.out.println(2.00 - 1.1);
	System.out.println(2.00 - 1.2);
	System.out.println(2.00 - 1.3);
	System.out.println(2.00 - 1.4);
	System.out.println(2.00 - 1.5);
	System.out.println(2.00 - 1.6);
	System.out.println(2.00 - 1.7);
	System.out.println(2.00 - 1.8);
	System.out.println(2.00 - 1.9);
	for(int i=0;i<=100;i++)
	System.out.println(2.00005 - 2.0);
}
}
