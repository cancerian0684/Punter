package com.sapient.server;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            return extractFileFromDocument(document, new File("" + document.getId() + document.getExt()));
        }
    },
    PUNTER_DOC_WITH_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITH_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            return createZipFromDocument(document);
        }
    },
    PUNTER_DOC_WITHOUT_ATTACHMENT_HANDLER(Document.DocumentType.PUNTER_DOC_WITHOUT_ATTACHMENT) {
        @Override
        public File handle(Document document) throws IOException {
            return extractFileFromDocument(document, new File("" + document.getId() + ".html"));
        }
    };

    public static File createZipFromDocument(Document doc) throws IOException {
        // These are the files to include in the ZIP file
        Collection<Attachment> attachments = doc.getAttachments();
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];
        // Create the ZIP file
        String outFilename = doc.getId() + ".zip";
        File tmpDir = new File("Temp");
        if (!tmpDir.exists())
            tmpDir.mkdirs();
        File resultFile = new File(tmpDir, outFilename);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultFile));

        out.putNextEntry(new ZipEntry(doc.getId() + ".htm"));
        // Transfer bytes from the file to the ZIP file
        out.write(doc.getContent());
        // Complete the entry
        out.closeEntry();
        // Compress the files
        for (Attachment attachment : attachments) {
            out.putNextEntry(new ZipEntry(attachment.getId() + attachment.getExt()));
            out.write(attachment.getContent());
            out.closeEntry();
        }
        // Complete the ZIP file
        out.close();
        return resultFile;
    }

    private static File extractFileFromDocument(Document document, File nf) throws IOException {
        File temp = new File("Temp");
        temp.mkdir();
        nf = new File(temp, nf.getName());
        if (!nf.exists()) {
            FileOutputStream fos = new FileOutputStream(nf);
            fos.write(document.getContent());
            fos.close();
        }
        return nf;
    }

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
        throw new RuntimeException("handler for this type of document not implemented.");
    }
}
