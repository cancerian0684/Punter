package com.shunya.punter.utils;

import java.math.BigDecimal;

strictfp class Strictfp {
  public static void main(String[] args) {
    double d = Double.MAX_VALUE;
    System.out.println("This value \"" + ((d * 1.1) / 1.1) + "\" cannot be represented as double.");
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
	
	System.out.println("--- BigDecimal-----");
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.1")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.2")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.3")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.4")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.5")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.6")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.7")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.8")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.9")));
	  System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("2")));
	  
	  
	  System.err.println(0.0 == -0.01);
  }
}
