package org.shunya.server.component;

import org.shunya.kb.model.Document;

import java.util.List;

/**
 * Created by munichan on 24-09-2015.
 */
public class SearchResult {
    private List<Document> documents;
    private int resultCount;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}
