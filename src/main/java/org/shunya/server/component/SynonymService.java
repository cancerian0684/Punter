package org.shunya.server.component;

import org.shunya.kb.model.SynonymWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SynonymService {
    private Map<String, String> synonyms = new ConcurrentHashMap<>(100);

    @Autowired
    private DBService dbService;

    public void loadFromDB(){
        System.out.println("Loading Synonym Cache from the Database");
        List<SynonymWord> synonymWords = dbService.getSynonymWords();
        for (SynonymWord synonymWord : synonymWords) {
            addWordsToCache(synonymWord.getWords());
        }
    }

    public void addWordsToCache(String words) {
        if(words==null || words.isEmpty())
            return;
        final String[] split = words.split("[;,]");
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                synonyms.put(s.trim(), words);
            }
        }
    }

    public void saveWord(String words){
        SynonymWord synonymWord = new SynonymWord();
        synonymWord.setWords(words);
        dbService.create(synonymWord);
    }

    public String[] getSynonymArray(String word) {
        final String key = word.toLowerCase();
        if (!synonyms.containsKey(key))
            return null;
        return synonyms.get(key).split("[;,]");
    }

    public String getSynonym(String word) {
        final String key = word.toLowerCase();
        if (!synonyms.containsKey(key))
            return null;
        return synonyms.get(key);
    }
}
