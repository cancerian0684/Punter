package org.shunya.server.component;

import org.apache.commons.io.IOUtils;
import org.shunya.kb.model.Document;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.tasks.Tasks;
import org.shunya.server.PunterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.Map;

import static java.util.Collections.singletonList;

public class RestClient {
    final Logger logger = LoggerFactory.getLogger(RestClient.class);
    protected RestTemplate restTemplate;

    public RestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        restTemplate = new RestTemplate(requestFactory);
    }

    public Map executeRemoteTask(TaskData taskData, String baseUri) {
        String uri = baseUri + "/punter/runTask";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Accept", "application/json; charset=utf-8");
        HttpEntity httpEntity = new HttpEntity<>(taskData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(uri, httpEntity, Map.class);
        Tasks.LOGGER.get().info("response.getBody() = " + response.getBody());
        return response.getBody();
    }

    public Long[] getRemoteDocList(String baseUri){
        String uri = baseUri + "/punter/doc/list";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Accept", "application/json; charset=utf-8");
//        ParameterizedTypeReference<List<Long>> listLong = new ParameterizedTypeReference<List<Long>>() {};
        ResponseEntity<Long[]> response = restTemplate.getForEntity(uri, Long[].class );
        Tasks.LOGGER.get().info("response.getBody() = " + response.getBody());
        return response.getBody();
    }

    public Document getRemoteDoc(String baseUri, long id){
        String uri = baseUri + "/punter/doc/"+id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Accept", "application/json; charset=utf-8");
        ResponseEntity<Document> response = restTemplate.getForEntity(uri, Document.class);
        Tasks.LOGGER.get().info("response.getBody() = " + response.getBody());
        return response.getBody();
    }

    public void fileUpload(String baseUri, String localFile, String name, String remotePath) throws Exception {
        String uri = baseUri + "/punter/upload";
        MultiValueMap<String, Object> mvm = new LinkedMultiValueMap<>();
        mvm.add("file", new FileSystemResource(localFile));
        mvm.add("name", name);
        mvm.add("path", remotePath);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, mvm, String.class);
        Tasks.LOGGER.get().info("upload response from server  - " + responseEntity.getStatusCode());
        String body = responseEntity.getBody();
        Tasks.LOGGER.get().info("file path at target server - " + body);
    }

    public void downloadFile(String baseUri, String id, String localPath) throws Exception {
        String uri = baseUri + "/punter/get/" + id;
        restTemplate.execute(uri, HttpMethod.GET, ACCEPT_CALLBACK, new FileResponseExtractor(new File(localPath, "test file.txt")));
    }

    public void sendClipBoardMessage(String baseUri, PunterMessage punterMessage) {
        logger.info("sending clipboard message : "+ punterMessage);
        String uri = baseUri + "/punter/clipboard";
        restTemplate.postForEntity(uri, punterMessage, String.class);
    }

    public void acceptHeaderUsingHttpEntity2() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.valueOf(String.valueOf(MediaType.ALL))));
        ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity("http://localhost:9999/getAll", ByteArrayResource.class);
        ByteArrayResource body = response.getBody();
    }

    private static final RequestCallback ACCEPT_CALLBACK = request -> request.getHeaders().set("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE);

    private static class FileResponseExtractor implements ResponseExtractor<Object> {
        private final File file;
        private File file() {
            return this.file;
        }

        private FileResponseExtractor(File file) {
            this.file = file;
        }

        @Override
        public Object extractData(ClientHttpResponse response) throws IOException {
            InputStream is = response.getBody();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file()));
            IOUtils.copyLarge(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
            return null;
        }
    }
}
