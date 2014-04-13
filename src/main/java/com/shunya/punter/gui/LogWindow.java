package com.shunya.punter.gui;

import com.shunya.punter.tasks.LogListener;

import javax.swing.*;
import java.awt.*;

public class LogWindow extends JFrame implements LogListener{
    private TextAreaFIFO logArea;

    public LogWindow(int bufferSize, String title) {
        super(title);
        logArea = new TextAreaFIFO(bufferSize);
        logArea.setRows(30);
        logArea.setColumns(80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial Unicode MS", 0, 11));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().add(new JScrollPane(logArea));
        setLocationRelativeTo(null);
        pack();
    }

    public void log(final String line) {
        SwingUtilities.invokeLater(() -> logArea.append(line + "\n"));
    }

    @Override
    public void showLog(){
        setVisible(true);
    }

    @Override
    public void disposeLogs(){
        dispose();
    }

    public static void main(String[] args) {
        LogWindow lw = new LogWindow(100, "Log Console");
        lw.setVisible(true);
        lw.setExtendedState(NORMAL);
    }
}
