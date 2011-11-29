package com.sapient.server;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;

import com.sapient.kb.utils.TestEditor;

public class PunterAnalyzer extends Analyzer {
	public static final List<String> STOP_WORDS;
	static
	{
		 STOP_WORDS=new ArrayList<String>(100);
		 Scanner scanner = new Scanner(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords.properties"));
         while (scanner.hasNextLine()) {
             String line = scanner.nextLine();
             StringTokenizer stk=new StringTokenizer(line, ",");
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
