package com.sapient.kb.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class TestAnalyzer {
	public static void main(String[] args) {
		try {
			String str = "INDIADen manu27 An easy ejb3 +91-8010106513 way to write an analyzer for tokens bi-gram (or even tokens n-grams) with lucene";
//			Analyzer analyzer = new NGramAnalyzer();
			Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30,"English");
 
			TokenStream stream = analyzer.tokenStream("content", new StringReader(str));
			while (stream.incrementToken()){
				System.out.println(stream);
			}
 
		} catch (IOException ie) {
			System.out.println("IO Error " + ie.getMessage());
		}
	}
}
class NGramAnalyzer extends Analyzer {
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new StopFilter(true, new LowerCaseFilter(new StandardTokenizer(Version.LUCENE_30, reader)),
				StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	}
}
