package org.shunya.server;

import org.markdown4j.Markdown4jProcessor;
import org.shunya.kb.model.Document;

import java.io.File;
import java.io.IOException;

import static org.shunya.server.LocalTemporaryFileUtils.createZipFile;
import static org.shunya.server.LocalTemporaryFileUtils.write;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/6/11
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
public enum PunterWebDocumentHandler {
    EXTERNAL_DOC_HANDLER(Document.DocumentType.EXTERNAL_DOC) {
        @Override
        public File handle(Document document) throws IOException {
            return write(document.getContent(), new File("" + document.getId() + document.getExt()), new File("Temp"));
        }
    },
    PUNTER_DOC_WITH_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITH_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            return createZipFile(document, new File("Temp"));
        }
    },
    PUNTER_DOC_WITHOUT_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITHOUT_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            final Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();
            return write(markdown4jProcessor.process(new String(document.getContent())).getBytes(), new File("" + document.getId() + ".html"), new File("Temp"));
        }
    };

    private Document.DocumentType documentType;

    PunterWebDocumentHandler(Document.DocumentType documentType) {
        this.documentType = documentType;
    }

    public abstract File handle(Document document) throws IOException;

    public Document.DocumentType getDocumentType() {
        return documentType;
    }

    public static File process(Document document) throws IOException {
        PunterWebDocumentHandler[] values = values();
        for (PunterWebDocumentHandler punterWebDocumentHandler : values) {
            if (punterWebDocumentHandler.getDocumentType() == document.getDocumentType()) {
                return punterWebDocumentHandler.handle(document);
            }
        }
        throw new RuntimeException("File handler for this type of document not implemented yet.");
    }
}