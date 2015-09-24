package org.shunya.server;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pegdown.PegDownProcessor;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.*;

public class PunterTextExtractor {

    public static String getText(byte[] contents, String title, String ext) {
        final PegDownProcessor markdown4jProcessor = new PegDownProcessor(10000L);
        StringBuilder text = new StringBuilder();
        ByteArrayInputStream bais = new ByteArrayInputStream(contents);
        String tt = title.toLowerCase();
        text.append(tt + " ");
        try {
            if (ext.isEmpty()) {
                Source source = new Source(new StringReader(markdown4jProcessor.markdownToHtml(new String(contents, "UTF-8"))));
                TextExtractor te = new TextExtractor(source);
                text.append(te.toString());
            } else if (ext.equalsIgnoreCase(".txt")) {
                text.append(new String(contents));
            } else if (ext.equalsIgnoreCase(".rtf")) {
                RTFEditorKit kit = new RTFEditorKit();
                Document doc = kit.createDefaultDocument();
                kit.read(bais, doc, 0);
                text.append(doc.getText(0, doc.getLength()));
            } else if (ext.equalsIgnoreCase(".html") || ext.equalsIgnoreCase(".htm")) {
                Source source = new Source(new StringReader(new String(contents, "UTF-16")));
                TextExtractor te = new TextExtractor(source);
                text.append(te.toString());
            } else if (ext.equalsIgnoreCase(".pdf")) {
                PdfReader reader = new PdfReader(bais);
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                TextExtractionStrategy strategy;
                int maxPagesToRead = reader.getNumberOfPages() > 10 ? 10 : reader.getNumberOfPages();
                for (int i = 1; i <= maxPagesToRead; i++) {
                    strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                    text.append(strategy.getResultantText());
                }
                reader.close();
            }
            else if(ext.equalsIgnoreCase(".docx") ) {
                final String wordText = getWordText(bais);
                text.append(wordText.substring(0, wordText.length() > 20000 ? 20000 : wordText.length()));
            }
            //APache POI
            else if (ext.equalsIgnoreCase(".doc") || ext.equalsIgnoreCase(".xls") || ext.equalsIgnoreCase(".xlsx") || ext.equalsIgnoreCase(".ppt") || ext.equalsIgnoreCase(".pptx")) {
                POIFSFileSystem fileSystem = new POIFSFileSystem(bais);
                // Firstly, get an extractor for the Workbook
                POIOLE2TextExtractor oleTextExtractor = ExtractorFactory.createExtractor(fileSystem);
                // Then a List of extractors for any embedded Excel, Word, PowerPoint
                // or Visio objects embedded into it.
                POITextExtractor[] embeddedExtractors = ExtractorFactory.getEmbededDocsTextExtractors(oleTextExtractor);

                String ta = oleTextExtractor.getText();
                text.append(ta.substring(0, ta.length() > 20000 ? 20000 : ta.length()));
            } else if (ext.equalsIgnoreCase(".msg")) {
                MAPIMessage msg = new MAPIMessage(bais);
                text.append(msg.getSubject() + " ");
                text.append(msg.getTextBody() + " ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static String getWordText(ByteArrayInputStream bais) {
        try {
            XWPFDocument document =new XWPFDocument(bais);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String fileData = extractor.getText();
            return fileData;
        } catch (Exception exep) {
            exep.printStackTrace();
        }
        return "";
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

    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }
}