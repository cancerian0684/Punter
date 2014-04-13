package com.shunya.punter.utils;

import java.util.Date;
import java.util.logging.*;

public class MemoryHandlerDemo {
  private ConsoleHandler handler = null;

  private MemoryHandler mHandler = null;

  private static Logger logger = Logger.getLogger("sam.logging");
  private StringBuilder strLogger = new StringBuilder();
  public String getMemoryLogs(){
	  return strLogger.toString();
  }
  public MemoryHandlerDemo(int size, Level pushLevel) {
	    handler = new ConsoleHandler();
	    handler.setFormatter(new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				return new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
      		  + " "+record.getSourceClassName()+"." 
	          + record.getSourceMethodName() + "() - "
	          + record.getMessage() + "\n";
			}
		});
	    mHandler = new MemoryHandler(new Handler() {
	        public void publish(LogRecord record) {
	        	strLogger.append(new Date(record.getMillis())+" ["+Thread.currentThread().getName()+"] "+record.getLevel()
	        		  + " "+record.getSourceClassName()+"." 
			          + record.getSourceMethodName() + "() - "
			          + record.getMessage() + "\n");
	    }

        public void flush() {
        }

        public void close() {
        }
        
      }, size, pushLevel);
	logger.addHandler(mHandler);
	logger.addHandler(handler);
    logger.setUseParentHandlers(false);
  }

  public void logMessage() {
    LogRecord record1 = new LogRecord(Level.SEVERE, "This is SEVERE level message");
    LogRecord record2 = new LogRecord(Level.WARNING, "This is WARNING level message");

    logger.log(record1);
    logger.log(record2);
    logger.log(Level.WARNING, "Munish");
  }

  public static void main(String args[]) {
    MemoryHandlerDemo demo = new MemoryHandlerDemo(2, Level.WARNING);
    demo.logMessage();
    System.out.println(demo.getMemoryLogs());
    demo = new MemoryHandlerDemo(2, Level.SEVERE);
    demo.logMessage();
    System.out.println(demo.getMemoryLogs());
  }
}