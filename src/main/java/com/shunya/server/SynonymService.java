package com.shunya.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynonymService {
    private Map<String, String> synonyms = new ConcurrentHashMap<>(100);

    private static SynonymService service = new SynonymService();

    public static SynonymService getService(){
        return service;
    }

    public void addWords(String words) {
        if(words==null || words.isEmpty())
            return;
        final String[] split = words.split("[;,]");
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                synonyms.put(s, words);
            }
        }
    }

    public String[] getSynonym(String word) {
        final String key = word.toLowerCase();
        if (!synonyms.containsKey(key))
            return null;
        return synonyms.get(key).split("[;,]");
    }

}
