package org.shunya.punter.gui;

import ch.qos.logback.classic.Level;
import org.shunya.punter.tasks.LogListener;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class LogWindow extends JFrame implements LogListener {
    private JTextPane logArea;

    private static SimpleAttributeSet ERROR_ATT, WARN_ATT, INFO_ATT, DEBUG_ATT, TRACE_ATT, RESTO_ATT;

    static {
        // ERROR
        ERROR_ATT = new SimpleAttributeSet();
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        ERROR_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 0, 0));

        // WARN
        WARN_ATT = new SimpleAttributeSet();
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        WARN_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 76, 0));

        // INFO
        INFO_ATT = new SimpleAttributeSet();
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
        INFO_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0, 0, 153));

        // DEBUG
        DEBUG_ATT = new SimpleAttributeSet();
        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
//        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        DEBUG_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(64, 64, 64));

        // TRACE
        TRACE_ATT = new SimpleAttributeSet();
        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
//        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        TRACE_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(153, 0, 76));

        // RESTO
        RESTO_ATT = new SimpleAttributeSet();
        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
//        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
        RESTO_ATT.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0, 0, 0));
    }

    public LogWindow(int bufferSize, String title) {
        super(title);
        logArea = new JTextPane();
        logArea.setEditable(false);
//        logArea.setSize(600, 500);
        logArea.setFont(new Font("Arial Unicode MS", 0, 11));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().add(new JScrollPane(logArea));
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setSize(700, 600);
    }

    public void log(String line, Level level) {
        SwingUtilities.invokeLater(() -> {
            try {
                int limite = 1000;
                int apaga = 200;
                if (logArea.getDocument().getDefaultRootElement().getElementCount() > limite) {
                    int end = 0;
                    end = getLineEndOffset(logArea, apaga);
                    replaceRange(logArea, null, 0, end);
                }

                if (level == Level.ERROR)
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, ERROR_ATT);
                else if (level == Level.WARN)
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, WARN_ATT);
                else if (level == Level.INFO)
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, INFO_ATT);
                else if (level == Level.DEBUG)
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, DEBUG_ATT);
                else if (level == Level.TRACE)
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, TRACE_ATT);
                else
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), line, RESTO_ATT);

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private int getLineCount(JTextPane textPane) {
        return textPane.getDocument().getDefaultRootElement().getElementCount();
    }

    private int getLineEndOffset(JTextPane textPane, int line) throws BadLocationException {
        int lineCount = getLineCount(textPane);
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", textPane.getDocument().getLength() + 1);
        } else {
            Element map = textPane.getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            // hide the implicit break at the end of the document
            return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
        }
    }

    private void replaceRange(JTextPane textPane, String str, int start, int end) throws IllegalArgumentException {
        if (end < start) {
            throw new IllegalArgumentException("end before start");
        }
        Document doc = textPane.getDocument();
        if (doc != null) {
            try {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument) doc).replace(start, end - start, str, null);
                } else {
                    doc.remove(start, end - start);
                    doc.insertString(start, str, null);
                }
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    @Override
    public void showLog() {
        setVisible(true);
    }

    @Override
    public void disposeLogs() {
        dispose();
    }

    public static void main(String[] args) {
        LogWindow lw = new LogWindow(100, "Log Console");
        lw.setVisible(true);
        lw.setExtendedState(NORMAL);
    }
}
