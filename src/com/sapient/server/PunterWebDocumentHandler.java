package com.sapient.server;

import com.sapient.LocalTemporaryFileUtils;
import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sapient.LocalTemporaryFileUtils.*;

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
            return write(document.getContent(), new File("" + document.getId() + document.getExt()));
        }
    },
    PUNTER_DOC_WITH_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITH_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            return createZipFile(document);
        }
    },
    PUNTER_DOC_WITHOUT_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITHOUT_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            return write(document.getContent(), new File("" + document.getId() + ".html"));
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
            if(punterWebDocumentHandler.getDocumentType()==document.getDocumentType()){
                return punterWebDocumentHandler.handle(document);
            }
        }
        throw new RuntimeException("File handler for this type of document not implemented yet.");
    }
}
