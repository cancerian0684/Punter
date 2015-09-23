package org.shunya.server.component;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.shunya.kb.model.Attachment;
import org.shunya.kb.model.Document;
import org.shunya.server.PunterTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.util.*;

@Service
public class LuceneIndexService {
    static final File INDEX_DIR = FileSystems.getDefault().getPath(System.getProperty("user.home")).resolve("LuceneIndex").toFile();
    private Directory directory;
    private IndexWriter indexWriter;
    private Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);
    private final QueryParser parser1;
    private final QueryParser parser2;

    private SearcherManager searcherManager;
    @Autowired
    private SynonymService synonymService;

    public org.apache.lucene.document.Document createLuceneDocument(Document pDoc) {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new StringField("id", "" + pDoc.getId(), Field.Store.YES));
        doc.add(new StringField("md5", "" + pDoc.getMd5(), Field.Store.YES));
        doc.add(new TextField("title", getPunterParsedText2(pDoc.getTitle()), Field.Store.NO));
        doc.add(new Field("titleS", pDoc.getTitle(), StringField.TYPE_STORED));
        doc.add(new StringField("author", pDoc.getAuthor() != null ? pDoc.getAuthor() : "", Field.Store.YES));
        doc.add(new TextField("category", pDoc.getCategory(), Field.Store.YES));
        doc.add(new StringField("created", DateTools.timeToString(pDoc.getDateCreated().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES));
        doc.add(new StringField("updated", DateTools.timeToString(pDoc.getDateUpdated().getTime(), DateTools.Resolution.MINUTE), Field.Store.YES));
        if (pDoc.getContent() != null)
            try {
                String contents = PunterTextExtractor.getText(pDoc.getContent(), "", pDoc.getExt());
                doc.add(new TextField("contents", itrim(getPunterParsedText2(contents)), Field.Store.NO));
                int len = contents.length();
                doc.add(new Field("content", contents.substring(0, len > 20000 ? 20000 : len), StringField.TYPE_STORED));
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
        doc.add(new TextField("attachment", itrim(getPunterParsedText2(attchs.toString())), Field.Store.YES));
        if (pDoc.getTag() != null) {
            StringTokenizer stk = new StringTokenizer(pDoc.getTag(), " ;,");
            StringBuilder tags = new StringBuilder();
            while (stk.hasMoreTokens()) {
                final String s = stk.nextToken();
                tags.append(s + " ");
                final String[] synonym = synonymService.getSynonymArray(s);
                if (synonym != null) {
                    for (String s1 : synonym) {
                        tags.append(s1 + " ");
                    }
                }
            }
            doc.add(new TextField("tags", itrim(getPunterParsedText(tags.toString())), Field.Store.NO));
        }
        return doc;
    }

    public String getPunterParsedText2(String inText) {
        StringTokenizer stk = new StringTokenizer(inText, " ,");
        Set<String> words = new HashSet<>(1000);
        while (stk.hasMoreTokens()) {
            String token = stk.nextToken();
            final String[] synonym = synonymService.getSynonymArray(token);
            if (synonym != null) {
                for (String s : synonym) {
                    words.add(s);
                }
            }
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
        try {
            System.err.println("Num of Docs after index deletion :" + indexWriter.numDocs());
            indexWriter.deleteAll();
            indexWriter.deleteUnusedFiles();
            indexWriter.commit();
            System.err.println("Num of Docs after index deletion :" + indexWriter.numDocs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void indexDocs(Document doc) {
        try {
            indexWriter.updateDocument(new Term("id", "" + doc.getId()), createLuceneDocument(doc));
            indexWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteIndexForDoc(long id) {
        try {
            indexWriter.deleteDocuments(new Term("id", QueryParser.escape("" + id)));
            indexWriter.deleteUnusedFiles();
            indexWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LuceneIndexService() {
        try {
            directory = NIOFSDirectory.open(INDEX_DIR);
            directory.clearLock("write.lock");
            Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            iwc.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
            iwc.setMergePolicy(new TieredMergePolicy());
            iwc.setRAMBufferSizeMB(64.0);
            indexWriter = new IndexWriter(directory, iwc);
            Runtime rt = Runtime.getRuntime();
            rt.addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("Lucene shutdown initiated.");
                        indexWriter.commit();
                        indexWriter.forceMerge(1);
                        indexWriter.forceMergeDeletes(true);
                        indexWriter.deleteUnusedFiles();
                        indexWriter.close(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        System.out.println("Lucene shutdown completed.");
                    }
                }
            });
            searcherManager = new SearcherManager(indexWriter, true, null);
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        Map<String, Float> boostMap = new HashMap<>();
        boostMap.put("title", 3.5f);
        boostMap.put("contents", 3.0f);
        boostMap.put("id", 1.0f);
        boostMap.put("attachment", 2.0f);
        boostMap.put("tags", 3.8f);
        parser1 = new MultiFieldQueryParser(Version.LUCENE_48, new String[]{"title", "contents", "id", "attachment", "tags"}, analyzer, boostMap);
        parser1.setAllowLeadingWildcard(true);
        parser1.setAnalyzeRangeTerms(true);
        parser1.setDefaultOperator(QueryParser.Operator.OR);

        parser2 = new QueryParser(Version.LUCENE_48, "category", analyzer);
        parser2.setDefaultOperator(QueryParser.Operator.AND);
    }

    public List<Document> search(String searchString, String category, boolean isAND, int start, int batch) throws IOException {
        long t1 = System.currentTimeMillis();
        searcherManager.maybeRefresh();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            searchString = searchString.trim().toLowerCase();
            if (searchString.equals("**"))
                searchString = "*";
            if (searchString.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            parser1.setAllowLeadingWildcard(true);
            if (isAND)
                parser1.setDefaultOperator(QueryParser.AND_OPERATOR);
            else
                parser1.setDefaultOperator(QueryParser.OR_OPERATOR);
            Query query1;
            try {
                String punterText = itrim(getPunterParsedText(searchString)).trim();
                StringBuilder queryText = new StringBuilder();
                queryText.append(searchString);
                if (!punterText.equalsIgnoreCase(searchString))
                    queryText.append(" ").append(punterText);
                String[] words = searchString.split("[,\\s*]");
                for (String word : words) {
                    String synonym = synonymService.getSynonym(word);
                    if (synonym != null)
                        queryText.append(" ").append(synonym);
                }
                System.out.println("Final Query = " + queryText.toString());
                query1 = parser1.parse(queryText.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Parsing of input query failed, trying with the escaped syntax now.");
                query1 = parser1.parse(itrim(getPunterParsedText(searchString)));
            }
            Query query2 = parser2.parse(QueryParser.escape(category));
//			MultiPhraseQuery leaningTower = new MultiPhraseQuery();
//			leaningTower.add(new Term("content", "tower"));
//			leaningTower.add(new Term("content", "tower"));
            BooleanQuery query = new BooleanQuery();
            query.add(query1, BooleanClause.Occur.MUST);
            query.add(query2, BooleanClause.Occur.MUST);
            BooleanQuery.setMaxClauseCount(100000);
            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span style='color:red;'>", "</span>"), new QueryScorer(query1));
            highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);
            highlighter.setTextFragmenter(new SimpleFragmenter(200));
            TopDocs hits = null;
            QueryWrapperFilter queryFilter = new QueryWrapperFilter(query);
            CachingWrapperFilter cwf = new CachingWrapperFilter(queryFilter);
            hits = searcher.search(query, cwf, start + batch);
            int numTotalHits = hits.totalHits;
//            System.out.println("time = " + (System.currentTimeMillis() - t1));
            List<Document> resultDocs = new ArrayList<>(50);
            for (int i = start; i < numTotalHits && i < (start + batch); i++) {
//				Explanation exp = isearcher.explain(query, i);
//				System.err.println(exp.getDescription());
                org.apache.lucene.document.Document doc = searcher.doc(hits.scoreDocs[i].doc);                    //get the next document
                Document document = new Document();
                try {
                    document.setDateCreated(DateTools.stringToDate(doc.get("created")));
                    document.setDateUpdated(DateTools.stringToDate(doc.get("updated")));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                document.setAuthor(doc.get("author"));
                document.setMd5(doc.get("md5"));
                document.setCategory(doc.get("category"));
                document.setId(Long.parseLong(doc.get("id")));
                String title = doc.get("titleS");
                String contents = doc.get("content") == null ? "" : doc.get("content");
                int maxNumFragmentsRequired = 2;
                String fragmentSeparator = "...";
                String result = highlighter.getBestFragments(analyzer.tokenStream("titleS", new StringReader(title)), title, maxNumFragmentsRequired, fragmentSeparator);
                if (result.length() > 0) {
                    result = result.replaceAll("\n", "<Br>");
                    document.setTitle(result);
                } else {
                    document.setTitle(title);
                }
                document.setScore(hits.scoreDocs[i].score);
                if (!searchString.equals("*")) {
                    result = highlighter.getBestFragments(analyzer.tokenStream("content", new StringReader(contents)), contents, maxNumFragmentsRequired, fragmentSeparator);
                    if (result.length() <= 0) {
                        String attachment = doc.get("attachment");
                        final String bestFragments = highlighter.getBestFragments(analyzer.tokenStream("attachment", new StringReader(attachment)), attachment, maxNumFragmentsRequired, fragmentSeparator);
                        if (bestFragments != null && !bestFragments.isEmpty())
                            result = "Attachment : " + bestFragments;
                    }
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
            return resultDocs;
        } catch (InvalidTokenOffsetsException | ParseException | IOException e1) {
            System.out.println("Search Query - " + searchString + ", Category - " + category);
            e1.printStackTrace();
        } finally {
            searcherManager.release(searcher);
        }
        return Collections.EMPTY_LIST;
    }

    public List<String> listAllTermsForTitle() throws IOException {
        System.err.println("Listing all the terms ");
        final IndexSearcher indexSearcher = searcherManager.acquire();
        try {
            Set<String> uniqueTerms = new HashSet<>(1000);
            Fields fields = MultiFields.getFields(indexSearcher.getIndexReader());
            Terms terms = fields.terms("title");
            TermsEnum iterator = terms.iterator(null);
            BytesRef byteRef = null;
            while ((byteRef = iterator.next()) != null) {
                String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
//                System.out.println(term);
                uniqueTerms.add(term);
            }
//          System.err.println("Listing all the terms done.." + uniqueTerms);
            return new ArrayList<>(uniqueTerms);
        } finally {
            searcherManager.release(indexSearcher);
        }
    }

    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }
}