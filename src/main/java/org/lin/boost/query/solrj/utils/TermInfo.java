package org.lin.boost.query.solrj.utils;

/**
 * Created by guanl on 6/12/2017.
 */
public class TermInfo {
    public String term;
    public double TFIDF;

    public TermInfo(String term, double TFIDF){
        this.term = term;
        this.TFIDF = TFIDF;
    }

    public String getTerm(){
        return term;
    }

    public double getTFIDF(){
        return TFIDF;
    }
}
