package com.shunya.kb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WordService {
    private final List<String> words;

    public WordService() throws URISyntaxException {
        words = new ArrayList<>(80000);
        init();
    }

    private void init() throws URISyntaxException {
        InputStream url = WordService.class.getClassLoader().getResourceAsStream("resources/brit-a-z.txt");
        try {
            Scanner sc = new Scanner(url);
            while (sc.hasNext()) {
                words.add(sc.next());
            }
            sc.close();
            url.close();
//        words.add("chandel");
//        words.add("munish");
//        Collections.sort(words);
            System.out.println("WordService Initialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        return words.size();
    }

    public List<String> getWords() {
        return words;
    }
}
