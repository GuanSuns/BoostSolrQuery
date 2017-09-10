package org.lin.boost.query.solrj;

import org.apache.solr.common.SolrDocument;

public class SolrDocumentWithScore {
    private SolrDocument solrDocument;
    private float score;

    public SolrDocumentWithScore(SolrDocument solrDocument, float score) {
        this.solrDocument = solrDocument;
        this.score = score;
    }

    public SolrDocumentWithScore() {
        this.solrDocument = null;
        score = 0f;
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
}
