package org.lin.boost.query.config;

/**
 * Created by guanl on 6/12/2017.
 */
public class SolrConfig {
    public final static String solrURL = "http://localhost:8983/solr/news_core";
    public final static String solrAnalysisFilterName = "org.apache.lucene.analysis.core.LowerCaseFilter";
    public final static String engineSearchField = "search_all";
    public final static int rowSize = 50;

    public final static int maxKeywords = 60;
    public final static String fieldID = "id";
    public final static String fieldScore = "score";
    public final static String fieldKeywords = "keywords";

    public final static double gradient = 0.5;
    public final static double C = 16;
    public final static double m = 0.5;

    public final static String solrSuggestURL = "http://localhost:8983/solr/suggestion_core";
    public final static String solrSuggestSearchField = "terms";
    public final static int suggestRowSize = 20;

    public static boolean debug = true;
}
