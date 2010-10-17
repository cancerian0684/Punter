package com.sapient.kb.gui;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

public class LuceneIndexDao {
	static final File INDEX_DIR = new File("LuceneIndex");
	private Directory FSDirectory;
	private IndexWriter FSWriter;
	private IndexSearcher isearcher;
	private IndexReader ireader;
	private final Lock writerWriteLock = new ReentrantLock();
	private ReentrantReadWriteLock readerReadWriteLock = new ReentrantReadWriteLock();
	public static final String[] STOP_WORDS =
    {
       /* "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "000", "$",*/
        "about", "after", "also", "an", "and",
        "another", "any", "are", "as", "at", "be",
        "because", "been", "before", "being", "between",
        "both", "but", "by", "came", "can", "come",
        "could", "did", "do", "does", "each", "else",
        "for", "from", "get", "got", "has", "had",
        "he", "have", "her", "here", "him", "himself",
        "his", "how","if", "in", "into", "is", "it",
        "its", "just", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my",
        "never", "now", "of", "on", "only", "or",
        "other", "our", "out", "over", "re", "said",
        "same", "see", "should", "since", "so", "some",
        "still", "such", "take", "than", "that", "the",
        "their", "them", "then", "there", "these",
        "they", "this", "those", "through", "to", "too",
        "under", "up", "use", "very", "want", "was",
        "way", "we", "well", "were", "what", "when",
        "where", "which", "while", "who", "will",
        "with", "would", "you", "your",
        "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z",
        "a", "an", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "such",
        "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with","/"
    };
	final CharArraySet stopSet = new CharArraySet(STOP_WORDS.length, false);
    {
    	stopSet.addAll(Arrays.asList(STOP_WORDS));  
    }
	Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30,"English", CharArraySet.unmodifiableSet(stopSet));
	
	private static LuceneIndexDao luceneIndexDao;
	private final QueryParser parser1;
	public void refreshIsearcher() {
		try {
			IndexReader tempReader = IndexReader.open(FSDirectory, true);
			IndexSearcher tempSearcher = new IndexSearcher(tempReader);
			IndexSearcher temp1 = isearcher;
			IndexReader temp2 = ireader;
			readerReadWriteLock.writeLock().lock();
			ireader = tempReader;
			isearcher = tempSearcher;
			readerReadWriteLock.writeLock().unlock();
			temp1.close();
			temp2.close();
			System.out.println("Total Docs in the index :" + isearcher.maxDoc());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static LuceneIndexDao getInstance(){
		if(luceneIndexDao==null){
			luceneIndexDao=new LuceneIndexDao();
		}
		return luceneIndexDao;
	}
	public static org.apache.lucene.document.Document Document(Document pDoc) {
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
		doc.add(new Field("id",""+ pDoc.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("title", pDoc.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("category", pDoc.getCategory(), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("created",DateTools.timeToString(pDoc.getDateCreated().getTime(), DateTools.Resolution.MINUTE),
		Field.Store.YES, Field.Index.NOT_ANALYZED));
		Source source;
		try {
			source = new Source(new StringReader(pDoc.getContent()));
			TextExtractor te=new TextExtractor(source);
//			System.err.println(te);
			String contents=te.toString().toLowerCase();
			doc.add(new Field("contents", contents.substring(0, contents.length()>10000?10000:contents.length()), Field.Store.YES, Field.Index.ANALYZED));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collection<Attachment> attchmts = pDoc.getAttachments();
		StringBuilder attchs=new StringBuilder();
		for (Attachment attachment : attchmts) {
			attchs.append(PunterTextExtractor.getText(attachment.getContent(), attachment.getTitle()));
		}
		System.out.println(attchs.toString());
		doc.add(new Field("attachment", attchs.toString(), Field.Store.NO, Field.Index.ANALYZED));
		return doc;
	}
	public void deleteIndex(){
		writerWriteLock.lock();
		try {
			System.err.println("Num of Docs after index deletion :"+FSWriter.numDocs());
			FSWriter.deleteAll();
			FSWriter.expungeDeletes();
			FSWriter.commit();
			System.err.println("Num of Docs after index deletion :"+FSWriter.numDocs());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			writerWriteLock.unlock();
		}
	}
	public void indexDocs(Document doc){
		writerWriteLock.lock();
		QueryParser parser = new QueryParser(Version.LUCENE_30,"id", new SnowballAnalyzer(Version.LUCENE_30,"English"));
		try {
			Query query = parser.parse(QueryParser.escape(""+doc.getId()));
			FSWriter.deleteDocuments(query);
			FSWriter.expungeDeletes();
			FSWriter.addDocument(Document(doc));
			FSWriter.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		writerWriteLock.unlock();
		}
	 }
	public void deleteIndexForDoc(Document doc){
		writerWriteLock.lock();
		QueryParser parser = new QueryParser(Version.LUCENE_30,"id", new SnowballAnalyzer(Version.LUCENE_30,"English"));
		try {
			Query query = parser.parse(QueryParser.escape(""+doc.getId()));
			FSWriter.deleteDocuments(query);
			FSWriter.expungeDeletes();
			FSWriter.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			writerWriteLock.unlock();
		}
	 }
	private LuceneIndexDao(){
		  try {
			    FSDirectory = NIOFSDirectory.open(INDEX_DIR);
				FSDirectory.clearLock("write.lock");
				boolean alreadyExists = IndexReader.indexExists(FSDirectory);
				if (!alreadyExists) {
					System.out.println("Index directory does not exists. Creating one.");
				}
				FSWriter = new IndexWriter(FSDirectory, analyzer, !alreadyExists,
						IndexWriter.MaxFieldLength.UNLIMITED);
				FSWriter.setRAMBufferSizeMB(50);
				FSWriter.maybeMerge();
				FSWriter.optimize();
				ireader = IndexReader.open(FSDirectory, true);
				isearcher = new IndexSearcher(ireader);
				
				Runtime rt = Runtime.getRuntime();
				rt.addShutdownHook(new Thread() {
					public void run() {
						try {
							System.out.println("System shutdown initiated.");
							readerReadWriteLock.writeLock().lock();
							writerWriteLock.lock();
							ireader.close();
							FSWriter.optimize();
							FSWriter.close();
							FSDirectory.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						} finally {
							readerReadWriteLock.writeLock().unlock();
							writerWriteLock.unlock();
							System.out.println("Lucene shutdown completed.");
						}
					}
				});
		    } catch (IOException e) {
		      System.out.println(" caught a " + e.getClass() +
		       "\n with message: " + e.getMessage());
		    }
		Map <String,Float> boostMap=new HashMap<String, Float>();
		boostMap.put("title", 5.0f);
		boostMap.put("contents", 3.0f);
		boostMap.put("id", 5.0f);
		boostMap.put("attachment", 1.0f);
		parser1 = new MultiFieldQueryParser(Version.LUCENE_30, new String []{"title","contents","id","attachment"}, analyzer, boostMap);
	}
	public void optimizeIndex(){
		readerReadWriteLock.writeLock().lock();
		writerWriteLock.lock();
		try {
			isearcher.close();
			ireader.close();
			FSWriter.expungeDeletes();
			FSWriter.commit();
			FSWriter.maybeMerge();
			FSWriter.optimize();
			FSWriter.close();
			ireader = IndexReader.open(FSDirectory, true);
			isearcher = new IndexSearcher(ireader);
			FSWriter = new IndexWriter(FSDirectory, analyzer,
					false, IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		readerReadWriteLock.writeLock().unlock();
		writerWriteLock.unlock();
	}
	public List<Document> search(String q,String category,int start, int batch){
		try {
			if (!ireader.isCurrent()) {
				System.out.println("Refreshing IndexSearcher version to :"+ ireader.getCurrentVersion(FSDirectory));
				refreshIsearcher();
			} else {
				 System.err.println("Index not modified yet.");
			}
//			TermEnum terms = ireader.terms(new Term("title", ""));
//			System.out.println(terms.);
			q = q.trim().toLowerCase();
			// Parse a simple query that searches for "text":
			readerReadWriteLock.readLock().lock();
			if(q.isEmpty()){
				return Collections.EMPTY_LIST;
			}
			parser1.setAllowLeadingWildcard(true);
			parser1.setDefaultOperator(QueryParser.OR_OPERATOR);
			/*if (false) {
				parser.setDefaultOperator(QueryParser.AND_OPERATOR);
			} else {
			}*/
			QueryParser parser2 = new QueryParser(Version.LUCENE_30,"category",analyzer);
			Query query1 = parser1.parse(q);
			Query query2 = parser2.parse(category);
			BooleanQuery query = new BooleanQuery();
			query.add(query1,  BooleanClause.Occur.MUST);
			query.add(query2, BooleanClause.Occur.MUST);
			BooleanQuery.setMaxClauseCount(100000);
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(
					"<font color=red>", "</font>"), new QueryScorer(query));
			TopDocs hits = null;
			hits = isearcher.search(query, 100);
			int numTotalHits = hits.totalHits;
			System.out.println(query);
			List<Document> resultDocs=new ArrayList<Document>(100);
			for (int i = start; i < numTotalHits && i < (start + batch); i++) {
			     org.apache.lucene.document.Document doc = isearcher.doc(hits.scoreDocs[i].doc);                    //get the next document 
                 Document document=new Document();
                 try {
					document.setDateCreated(DateTools.stringToDate(doc.get("created")));
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				
				 document.setCategory(doc.get("category"));
                 document.setId(Long.parseLong(doc.get("id")));
                 String title=doc.get("title");
                 String contents=doc.get("contents");
                 int maxNumFragmentsRequired = 2;
 				 String fragmentSeparator = "...";
 				 Source source;
 				 source = new Source(new StringReader(title));
 				 TextExtractor te=new TextExtractor(source);
 				 title = te.toString();
 				 TokenStream tokenStream = analyzer.tokenStream("title",new StringReader(title));
 				 CachingTokenFilter filter = new CachingTokenFilter(tokenStream);
 				 String result = highlighter.getBestFragments(filter, title,
 						maxNumFragmentsRequired, fragmentSeparator);
 				// System.out.println(result);
 				if (result.length() > 0) {
 					result = result.replaceAll("\n", "<Br>");
 					document.setTitle(result+"<sup>"+hits.scoreDocs[i].score+"</sup>");
 				}else{
 					document.setTitle(title+"<sup>"+hits.scoreDocs[i].score+"</sup>");
 				}
				source = new Source(new StringReader(contents));
				te=new TextExtractor(source);
 				contents = te.toString();
				tokenStream = analyzer.tokenStream("contents",new StringReader(contents));
				filter = new CachingTokenFilter(tokenStream);
				result = highlighter.getBestFragments(filter, contents,maxNumFragmentsRequired, fragmentSeparator);
				if (result.length() > 0) {
					result = result.replace('\n', ' ');
					result=itrim(result);
					document.setContent(result);
				}else{
					document.setContent(contents.substring(0, contents.length()>100?100:contents.length()));
				}
                 resultDocs.add(document);
			}
			return resultDocs;
		} catch (CorruptIndexException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			e.printStackTrace();
		}finally{
			readerReadWriteLock.readLock().unlock();
		}
		return Collections.EMPTY_LIST;
	}
	 public static String itrim(String source) {
	        return source.replaceAll("\\b\\s{2,}\\b", " ");
	    }
}