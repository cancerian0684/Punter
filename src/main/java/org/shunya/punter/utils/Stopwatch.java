package org.shunya.punter.utils;

public class Stopwatch {

    private long startTime = -1;
    private long stopTime = -1;
    private boolean running = false;

    public Stopwatch start() {
       startTime = System.currentTimeMillis();
       running = true;
       return this;
    }
    public Stopwatch stop() {
       stopTime = System.currentTimeMillis();
       running = false;
       return this;
    }
    /** returns elapsed time in milliseconds
      * if the watch has never been started then
      * return zero
      */
    public long getElapsedTime() {
       if (startTime == -1) {
          return 0;
       }
       if (running){
       return System.currentTimeMillis() - startTime;
       } else {
       return stopTime-startTime;
       } 
    }

    public Stopwatch reset() {
       startTime = -1;
       stopTime = -1;
       running = false;
       return this;
    }
    public String getElapsedTimeString(){
        long elapsedTime=getElapsedTime();
        int hr=(int)(elapsedTime/3600000);
        long rest=elapsedTime%3600000;
        int mm=(int)rest/60000;
        rest=rest%60000;
        int sec=(int)rest/1000;
        String time=hr+" hr "+mm+" min "+sec+" sec ";
        return time;
    }
}