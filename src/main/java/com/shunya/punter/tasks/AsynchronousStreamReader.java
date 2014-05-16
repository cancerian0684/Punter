package com.shunya.punter.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
* Created by IntelliJ IDEA.
* User: mchan2
* Date: 10/3/11
* Time: 8:03 PM
* To change this template use File | Settings | File Templates.
*/
class AsynchronousStreamReader extends Thread
{
    private StringBuffer fBuffer = null;
    private InputStream fInputStream = null;
    private String fThreadId = null;
    private boolean fStop = false;
    private ILogDevice fLogDevice = null;

    private String fNewLine = null;

    public AsynchronousStreamReader(InputStream inputStream, StringBuffer buffer, ILogDevice logDevice, String threadId)
    {
        fInputStream = inputStream;
        fBuffer = buffer;
        fThreadId = threadId;
        fLogDevice = logDevice;

        fNewLine = System.getProperty("line.separator");
    }

    public String getBuffer() {
        return fBuffer.toString();
    }

    public void run()
    {
        try {
            readCommandOutput();
        } catch (Exception ex) {
            //ex.printStackTrace(); //DEBUG
        }
    }

    private void readCommandOutput() throws IOException
    {
        BufferedReader bufOut = new BufferedReader(new InputStreamReader(fInputStream));
        String line;
        while ( (fStop == false) && ((line = bufOut.readLine()) != null) )
        {
            fBuffer.append(line + fNewLine);
            printToDisplayDevice(line);
        }
        bufOut.close();
    }

    public void stopReading() {
        fStop = true;
    }

    private void printToDisplayDevice(String line)
    {
        if( fLogDevice != null )
            fLogDevice.log(line);
        else
        {
            if(fThreadId.equalsIgnoreCase("error"))
                printToConsole2(line);//DEBUG
            else{
                printToConsole(line);//DEBUG

            }
        }
    }

    private synchronized void printToConsole(String line) {
        DBExportTask.LOGGER.get().log(Level.INFO, line);
    }

    private synchronized void printToConsole2(String line) {
        DBExportTask.LOGGER.get().log(Level.WARNING, line);
    }
}
