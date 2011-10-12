package com.sapient.kb.gui;

public class SearchQuery {
    String query;
    String category;
    boolean specialText;
    boolean andFilter;
    int maxResults;

    public static class SearchQueryBuilder {
        String query;
        String category;
        boolean specialText;
        boolean andFilter;
        int maxResults;

        public SearchQuery build() {
            return new SearchQuery(this);
        }

        public SearchQueryBuilder query(String query) {
            this.query = query;
            return this;
        }

        public SearchQueryBuilder category(String category) {
            this.category = category;
            return this;
        }

        public SearchQueryBuilder specialText(boolean specialText) {
            this.specialText = specialText;
            return this;
        }

        public SearchQueryBuilder andFilter(boolean andFilter) {
            this.andFilter = andFilter;
            return this;
        }

        public SearchQueryBuilder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }
    }

    SearchQuery(SearchQueryBuilder queryBuilder) {
        this.query = queryBuilder.query;
        this.andFilter=queryBuilder.andFilter;
        this.category=queryBuilder.category;
        this.specialText=queryBuilder.specialText;
        this.maxResults=queryBuilder.maxResults;
    }

    public String getQuery() {
        return query;
    }

    public String getCategory() {
        return category;
    }

    public boolean isSpecialText() {
        return specialText;
    }

    public boolean isAndFilter() {
        return andFilter;
    }

    public int getMaxResults() {
        return maxResults;
    }
}
