package com.shunya.server;

import org.junit.Test;

import static com.shunya.server.LuceneIndexDao.getPunterParsedText;
import static com.shunya.server.LuceneIndexDao.getPunterParsedText2;
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

    @Test
    public void checkSimpleParser(){
        SynonymService.getService().addWords("gau,gaye,cow,gou");
        assertEquals("Chandel Munish ", getPunterParsedText2("Munish,Chandel,cow"));
    }
}
