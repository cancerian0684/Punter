package com.shunya.server;

import com.shunya.kb.utils.TestEditor;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;

import java.io.Reader;
import java.util.*;

public class PunterAnalyzer extends Analyzer {
    public static final List<String> STOP_WORDS = new ArrayList<String>(100);

    static {
        Scanner scanner = new Scanner(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords.properties"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            StringTokenizer stk = new StringTokenizer(line, ",");
            while (stk.hasMoreTokens()) {
                STOP_WORDS.add(stk.nextToken());
            }
        }
        scanner.close();
    }

    public static Set stopWords = StopFilter.makeStopSet(Collections.unmodifiableList(STOP_WORDS));

    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new WhitespaceTokenizer(reader);
        result = new LowerCaseFilter(result);
        result = new StopFilter(false, result, stopWords);
        result = new SnowballFilter(result, "English");
        return result;
    }
}
