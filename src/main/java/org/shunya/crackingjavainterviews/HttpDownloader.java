package org.shunya.crackingjavainterviews;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class HttpDownloader {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HttpDownloader httpDownloader = new HttpDownloader();
        List<String> urls = new ArrayList<>();
        urls.add("http://mysite/test1");
        urls.add("http://mysite/test2");
        urls.add("http://mysite/test3");
        urls.add("http://mysite/test4");
        httpDownloader.downloadAll(urls);
    }

    public void downloadAll(List<String> urls) throws ExecutionException, InterruptedException {
        AtomicInteger fileCounter = new AtomicInteger(0);
        ForkJoinPool pool = new ForkJoinPool(5);
        ForkJoinTask<?> task = pool.submit(() -> urls.parallelStream().forEach(url -> download(url, "test-download-" + fileCounter.incrementAndGet())));
        task.get();
        pool.shutdown();
    }

    public void download(String rootUrl, String fileName) {
        try {
            Path path = Paths.get(fileName);
            long totalBytesRead = 0L;
            HttpURLConnection con = (HttpURLConnection) new URL(rootUrl).openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);
            try (ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                 FileChannel fileChannel = FileChannel.open(path, EnumSet.of(CREATE, WRITE));) {
                totalBytesRead = fileChannel.transferFrom(rbc, 0, 1 << 22); // download file with max size 4MB
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
