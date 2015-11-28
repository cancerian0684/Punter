package org.shunya.punter.tasks;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Created by munish on 11/28/2015.
 */
public class LogWindowAppender extends AppenderBase<ILoggingEvent> {
    private PatternLayout patternLayout;

    private final LogListener logListener;
    private String pattern;

    public LogWindowAppender(LogListener logListener, String pattern) {
        this.logListener = logListener;
        this.pattern = pattern;
    }

    @Override
    public void start() {
        patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        patternLayout.setPattern(pattern);
        patternLayout.start();

        super.start();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        String formattedMsg = patternLayout.doLayout(iLoggingEvent);
        logListener.log(formattedMsg, iLoggingEvent.getLevel());
    }
}
