package com.shunya.server;

import org.junit.Test;

import java.util.Map;

public class MyProcessHandlerTest {

    @Test
    public void testHttpURIParsingToMap() {
        Map<String,String> map = MyProcessHandler.parseQueryString("http://www.google.com?name=munish&age=27");
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            System.out.println("stringStringEntry = " + stringStringEntry);
        }

    }
}
