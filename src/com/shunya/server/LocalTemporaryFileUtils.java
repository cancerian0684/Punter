package com.shunya.server;

import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: mchan2
 * Date: 10/8/11
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalTemporaryFileUtils {
    public static final File TEMP_DIRECTORY = new File(ServerSettings.getInstance().getTempDirectory());

    static {
        TEMP_DIRECTORY.mkdirs();
    }

    public static File write(byte[] contents, File file) throws IOException {
        file = new File(TEMP_DIRECTORY, file.getName());
        IOUtils.write(contents, new FileOutputStream(file));
        return file;
    }

    public static File createZipFile(Document doc) throws IOException {
        Collection<Attachment> attachments = doc.getAttachments();
        File zipFile = new File(TEMP_DIRECTORY, doc.getId() + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        out.putNextEntry(new ZipEntry(doc.getId() + ".htm"));
        out.write(doc.getContent());
        out.closeEntry();
        for (Attachment attachment : attachments) {
            out.putNextEntry(new ZipEntry(attachment.getId() + attachment.getExt()));
            out.write(attachment.getContent());
            out.closeEntry();
        }
        out.close();
        return zipFile;
    }
}
