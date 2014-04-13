package com.shunya.punter.test;

public class MyFinalTest {
   public int doMethod(){
        try{
            throw new Exception();
        }
        finally{
            return 10;
        }
    }
public static void main(String[] args) {
        MyFinalTest testEx = new MyFinalTest();
        int rVal = testEx.doMethod();
        System.out.println("The return Val : "+rVal);
    }
}