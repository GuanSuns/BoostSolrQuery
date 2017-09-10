package org.lin.boost.query.solrj;

import org.apache.solr.common.SolrDocument;

public class SolrDocumentWithScore {
    private SolrDocument solrDocument;
    private float score;
    private String[] keywords;

    public SolrDocumentWithScore(SolrDocument solrDocument
            , float score, String[] keywords) {
        this.solrDocument = solrDocument;
        this.score = score;
        this.keywords = keywords;
    }

    public SolrDocumentWithScore() {
        this.solrDocument = null;
        score = 0f;
        keywords = new String[0];
    }

    public SolrDocument getSolrDocument() {
        return solrDocument;
    }

    public void setSolrDocument(SolrDocument solrDocument) {
        this.solrDocument = solrDocument;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }
}
