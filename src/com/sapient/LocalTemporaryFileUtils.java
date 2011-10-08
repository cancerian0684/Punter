package com.sapient;

import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.punter.gui.AppSettings;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
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
    public static final File TEMP_DIRECTORY = new File(AppSettings.getInstance().getTempDirectory());

    static {
        TEMP_DIRECTORY.mkdirs();
    }

    public static File write(byte[] contents, File file) throws IOException {
        file = new File(TEMP_DIRECTORY, file.getName());
        if (!file.exists()) {
            IOUtils.write(contents, new FileOutputStream(file));
        }
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
