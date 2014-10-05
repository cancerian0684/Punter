package org.shunya.server;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.shunya.server.component.LuceneIndexService.getPunterParsedText;

public class LuceneIndexServiceTest {
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
//        SynonymService.getService().addWordsToCache("gau,gaye,cow,gou");
//        assertEquals("Chandel Munish ", getPunterParsedText2("Munish,Chandel,cow"));
    }
}
