package com.sapient.server;

import org.junit.Test;

import static com.sapient.server.LuceneIndexDao.getPunterParsedText;
import static junit.framework.Assert.assertEquals;

public class LuceneIndexDaoTest {
    @Test
    public void shouldBreakdownCamelCaseWords(){
        assertEquals(" Munish Chandel", getPunterParsedText("MunishChandel"));
    }

    @Test
    public void shouldBreakdownCamelCaseWordsContainingNumbers(){
        assertEquals(" cancerian 0684  gmail  com", getPunterParsedText("cancerian0684@gmail.com"));
    }
}
