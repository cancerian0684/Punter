package com.shunya.server;

import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.punter.utils.Stopwatch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LuceneIndexDao {
    static final File INDEX_DIR = new File("LuceneIndex");
    private Directory FSDirectory;
    private IndexWriter FSWriter;
    private IndexSearcher isearcher;
    private IndexReader ireader;
    private final Lock writerWriteLock = new ReentrantLock();
    private ReentrantReadWriteLock readerReadWriteLock = new ReentrantReadWriteLock();
    private Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30, "English", PunterAnalyzer.stopWords);
    private static LuceneIndexDao luceneIndexDao;
    private final QueryParser parser1;
    private final QueryParser parser2;

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

    public static LuceneIndexDao getInstance() {
        if (luceneIndexDao == null) {
            luceneIndexDao = new LuceneIndexDao();
        }
        return luceneIndexDao;
    }

    public static org.apache.lucene.document.Document createLuceneDocument(Document pDoc) {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new Field("id", "" + pDoc.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("title", getPunterParsedText2(pDoc.getTitle()), Field.Store.NO, Field.Index.ANALYZED));
        doc.add(new Field("titleS", pDoc.getTitle(), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("author", pDoc.getAuthor() != null ? pDoc.getAuthor() : "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("category", pDoc.getCategory(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("created", DateTools.timeToString(pDoc.getDateCreated().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("updated", DateTools.timeToString(pDoc.getDateUpdated().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
        try {
            String contents = PunterTextExtractor.getText(pDoc.getContent(), "", pDoc.getExt());
            doc.add(new Field("contents", itrim(getPunterParsedText2(contents)), Field.Store.NO, Field.Index.ANALYZED));
            int len = contents.length();
            doc.add(new Field("content", contents.substring(0, len > 20000 ? 20000 : len), Field.Store.YES, Field.Index.NO));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collection<Attachment> attchmts = pDoc.getAttachments();
        StringBuilder attchs = new StringBuilder();
        if (attchmts != null)
            for (Attachment attachment : attchmts) {
                attchs.append(PunterTextExtractor.getText(attachment.getContent(), attachment.getTitle(), attachment.getExt()));
            }
//		System.out.println(attchs.toString());
        doc.add(new Field("attachment", itrim(getPunterParsedText2(attchs.toString())), Field.Store.NO, Field.Index.ANALYZED));
        if (pDoc.getTag() != null) {
            StringTokenizer stk = new StringTokenizer(pDoc.getTag(), " ;,");
            StringBuilder tags = new StringBuilder();
            while (stk.hasMoreTokens()) {
                tags.append(stk.nextToken() + " ");
            }
            doc.add(new Field("tags", itrim(getPunterParsedText(tags.toString())), Field.Store.NO, Field.Index.ANALYZED));
        }
        return doc;
    }

    public static String getPunterParsedText2(String inText) {
        StringTokenizer stk = new StringTokenizer(inText, " ,");
        Set<String> words = new HashSet<String>(1000);
        while (stk.hasMoreTokens()) {
            String token = stk.nextToken();
            words.add(token);
            words.addAll(getPunterParsedSubText(token));
        }
        StringBuilder sb = new StringBuilder(10000);
        for (String word : words) {
            sb.append(word + " ");
        }
        return sb.toString();
    }

    public static List<String> getPunterParsedSubText(String inText) {
        StringBuilder sb = new StringBuilder();
        List<String> wordsList = new ArrayList<String>(5);
        char curr;
        char prev = '\0';
        for (int i = 0; i < inText.length(); i++) {
            curr = inText.charAt(i);
            if (!Character.isLetter(curr) && Character.isLetter(prev)) {
//				System.err.println("boundary .. "+curr);
//				sb.append(' ');
                if (sb.length() > 0)
                    wordsList.add(sb.toString());
                sb.setLength(0);
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else if (!Character.isLowerCase(curr) && Character.isLowerCase(prev)) {
//              System.err.println("boundary .. "+curr); 
                if (sb.length() > 0)
                    wordsList.add(sb.toString());
                sb.setLength(0);
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else if (Character.isLetter(curr) && !Character.isLetter(prev)) {
//				System.err.println("boundary .. "+curr);
//				sb.append(' ');
                if (sb.length() > 0)
                    wordsList.add(sb.toString());
                sb.setLength(0);
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else {
                if (Character.isLetterOrDigit(curr)) {
                    sb.append(curr);
                } else {
//					sb.append(' ');
                    if (sb.length() > 0)
                        wordsList.add(sb.toString());
                    sb.setLength(0);
                }
            }
            prev = curr;
        }
        if (sb.length() > 0)
            wordsList.add(sb.toString());
        return wordsList;
    }

    public static String getPunterParsedText(String inText) {
        StringBuilder sb = new StringBuilder();
        char curr;
        char prev = '\0';
        for (int i = 0; i < inText.length(); i++) {
            curr = inText.charAt(i);
            if (!Character.isLetter(curr) && Character.isLetter(prev)) {
//				System.err.println("boundary .. "+curr);
                sb.append(' ');
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else if (!Character.isLowerCase(curr) && Character.isLowerCase(prev)) {
//              System.err.println("boundary .. "+curr); 
                sb.append(' ');
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else if (Character.isLetter(curr) && !Character.isLetter(prev)) {
//				System.err.println("boundary .. "+curr);
                sb.append(' ');
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
            } else {
                if (Character.isLetterOrDigit(curr))
                    sb.append(curr);
                else
                    sb.append(' ');

            }
            prev = curr;
        }
        return sb.toString();
    }

    public void deleteIndex() {
        writerWriteLock.lock();
        try {
            System.err.println("Num of Docs after index deletion :" + FSWriter.numDocs());
            FSWriter.deleteAll();
            FSWriter.expungeDeletes();
            FSWriter.commit();
            System.err.println("Num of Docs after index deletion :" + FSWriter.numDocs());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writerWriteLock.unlock();
        }
    }

    public void indexDocs(Document doc) {
        writerWriteLock.lock();
        QueryParser parser = new QueryParser(Version.LUCENE_30, "id", analyzer);
        try {
            Query query = parser.parse(QueryParser.escape("" + doc.getId()));
            FSWriter.deleteDocuments(query);
            FSWriter.expungeDeletes();
            FSWriter.addDocument(createLuceneDocument(doc));
            FSWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writerWriteLock.unlock();
        }
    }

    public void deleteIndexForDoc(Document doc) {
        writerWriteLock.lock();
        QueryParser parser = new QueryParser(Version.LUCENE_30, "id", new SnowballAnalyzer(Version.LUCENE_30, "English"));
        try {
            Query query = parser.parse(QueryParser.escape("" + doc.getId()));
            FSWriter.deleteDocuments(query);
            FSWriter.expungeDeletes();
            FSWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writerWriteLock.unlock();
        }
    }

    private LuceneIndexDao() {
        try {
            FSDirectory = NIOFSDirectory.open(INDEX_DIR);
            FSDirectory.clearLock("write.lock");
            boolean alreadyExists = IndexReader.indexExists(FSDirectory);
            if (!alreadyExists) {
                System.out.println("Index directory does not exists. Creating one.");
            }
            FSWriter = new IndexWriter(FSDirectory, analyzer, !alreadyExists,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            FSWriter.setRAMBufferSizeMB(20);
            FSWriter.maybeMerge();
            FSWriter.optimize();
            ireader = IndexReader.open(FSDirectory, true);
            isearcher = new IndexSearcher(ireader);

            Runtime rt = Runtime.getRuntime();
            rt.addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("Lucene shutdown initiated.");
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
        Map<String, Float> boostMap = new HashMap<String, Float>();
        boostMap.put("title", 4.0f);
        boostMap.put("contents", 3.0f);
        boostMap.put("id", 1.0f);
        boostMap.put("attachment", 2.0f);
        boostMap.put("tags", 5.0f);
        parser1 = new MultiFieldQueryParser(Version.LUCENE_30, new String[]{"title", "contents", "id", "attachment", "tags"}, analyzer, boostMap);
        parser2 = new QueryParser(Version.LUCENE_30, "category", analyzer);
    }

    public void optimizeIndex() {
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

    public List<Document> search(String searchString, String category, boolean isAND, int start, int batch) {
        Stopwatch sw = new Stopwatch();
        sw.start();
        try {
            if (!ireader.isCurrent()) {
                System.out.println("Refreshing IndexSearcher version to :" + ireader.getCurrentVersion(FSDirectory));
                refreshIsearcher();
            }
            sw.reset();

            searchString = searchString.trim().toLowerCase();
            if (searchString.equals("**"))
                searchString = "*";
            readerReadWriteLock.readLock().lock();
            if (searchString.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            parser1.setAllowLeadingWildcard(true);
            if (isAND)
                parser1.setDefaultOperator(QueryParser.AND_OPERATOR);
            else
                parser1.setDefaultOperator(QueryParser.OR_OPERATOR);
            Query query1 = parser1.parse(searchString + " " + itrim(getPunterParsedText(searchString)));
            Query query2 = parser2.parse(category);
//			MultiPhraseQuery leaningTower = new MultiPhraseQuery();
//			leaningTower.add(new Term("content", "tower"));
//			leaningTower.add(new Term("content", "tower"));
            BooleanQuery query = new BooleanQuery();
            query.add(query1, BooleanClause.Occur.MUST);
            query.add(query2, BooleanClause.Occur.MUST);
            BooleanQuery.setMaxClauseCount(100000);
            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span style='color:red;'>", "</span>"), new QueryScorer(query1));
            TopDocs hits = null;
//			CachingWrapperFilter cwf=new CachingWrapperFilter();
            hits = isearcher.search(query, start + batch);
            int numTotalHits = hits.totalHits;
            System.out.println(query);
            List<Document> resultDocs = new ArrayList<Document>(50);
            for (int i = start; i < numTotalHits && i < (start + batch); i++) {
//				Explanation exp = isearcher.explain(query, i);
//				System.err.println(exp.getDescription());
                org.apache.lucene.document.Document doc = isearcher.doc(hits.scoreDocs[i].doc);                    //get the next document
                Document document = new Document();
                try {
                    document.setDateCreated(DateTools.stringToDate(doc.get("created")));
                    document.setDateUpdated(DateTools.stringToDate(doc.get("updated")));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                document.setAuthor(doc.get("author"));
                document.setCategory(doc.get("category"));
                document.setId(Long.parseLong(doc.get("id")));
                String title = doc.get("titleS");
                String contents = doc.get("content");
                int maxNumFragmentsRequired = 2;
                String fragmentSeparator = "...";
                  /*Source source;
 				 source = new Source(new StringReader(title));
 				 TextExtractor te=new TextExtractor(source);
 				 title = te.toString();*/
                TokenStream tokenStream = analyzer.tokenStream("titleS", new StringReader(title));
                CachingTokenFilter filter = new CachingTokenFilter(tokenStream);
                String result = highlighter.getBestFragments(filter, title, maxNumFragmentsRequired, fragmentSeparator);
                if (result.length() > 0) {
                    result = result.replaceAll("\n", "<Br>");
                    document.setTitle(result);
                } else {
                    document.setTitle(title);
                }
                document.setScore(hits.scoreDocs[i].score);
				/*source = new Source(new StringReader(contents));
				te=new TextExtractor(source);
 				contents = te.toString();*/

                if (!searchString.equals("*")) {
                    tokenStream = analyzer.tokenStream("content", new StringReader(contents));
                    filter = new CachingTokenFilter(tokenStream);
                    result = highlighter.getBestFragments(filter, contents, maxNumFragmentsRequired, fragmentSeparator);
                    if (result.length() > 0) {
                        result = result.replace('\n', ' ');
                        result = itrim(result);
                        document.setContent(result.getBytes());
                    } else {
                        document.setContent("".getBytes());
                    }
                } else {
                    document.setContent(contents.substring(0, contents.length() > 120 ? 120 : contents.length()).getBytes());
                }
                resultDocs.add(document);
            }
//			System.out.print(sw.getElapsedTime()+" \n");
            sw.reset();
            return resultDocs;
        } catch (InvalidTokenOffsetsException | ParseException | IOException e1) {
            e1.printStackTrace();
        } finally {
            readerReadWriteLock.readLock().unlock();
        }
        return Collections.EMPTY_LIST;
    }

    public List<String> listAllTermsForTitle() throws IOException {
        System.err.println("Listing all the terms ");
        Set<String> uniqueTerms = new HashSet<>(1000);
        TermEnum terms = ireader.terms(new Term("title", "content"));
        while (terms.next())
            uniqueTerms.add(terms.term().text());
        terms.close();
        System.err.println("Listing all the terms done.."+uniqueTerms);
        return new ArrayList<>(uniqueTerms);
    }

    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }
}