package org.shunya.punter.utils;

import org.asciidoctor.Asciidoctor;
import org.shunya.punter.gui.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.asciidoctor.Asciidoctor.Factory.create;

public class StackWindow extends JFrame implements Thread.UncaughtExceptionHandler {
    private final Asciidoctor asciidoctor = create();
    private final String devEmailCSV;
    private JTextArea textArea;
    private volatile int count = 0;

    public static void main(String[] args) {
        Thread.UncaughtExceptionHandler handler = new StackWindow("Unhandled Exception", 500, 400, "munishc@xxx.com");
        Thread.setDefaultUncaughtExceptionHandler(handler);
        throw new RuntimeException("should be caught");
    }

    public StackWindow(String title, final int width, final int height, String devEmailCSV) {
        super(title);
        this.devEmailCSV = devEmailCSV;
        setSize(width, height);
        textArea = new JTextArea();
        JScrollPane pane = new JScrollPane(textArea);
        textArea.setEditable(false);
        getContentPane().add(pane);
        setLocationRelativeTo(null);
    }

    public void uncaughtException(Thread t, Throwable e) {
        ++count;
        addStackInfo(e);
    }

    public void addStackInfo(final Throwable t) {
        EventQueue.invokeLater(() -> {
            final StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);
            t.printStackTrace(out);
            t.printStackTrace();
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (count > 10) {
                            JOptionPane.showMessageDialog(null, "Punter will exit now");
                            Thread.sleep(500);
                            System.exit(0);
                        } else {
                            DevEmailService.getInstance().sendEmail("Unknown Punter Exception : [" + AppSettings.getInstance().getUsername() + "] ", devEmailCSV,asciidoctor.convert("```java\n\n"+sw.toString()+"\n```", Collections.emptyMap()), Collections.<File>emptyList());
                        }
                    } catch (Throwable E) {
                        System.err.println(E.toString());
                    }
                }
            }.start();
            setVisible(true);
            toFront();
            textArea.setText(sw.toString());
        });

    }
}
