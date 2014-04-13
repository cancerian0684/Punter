package com.shunya.kb.utils;

import java.net.URISyntaxException;

public class WordServiceTest {

    @org.junit.Test
    public void test() throws URISyntaxException {
        WordService test = new WordService();
        System.out.println(test.count());
    }
}
