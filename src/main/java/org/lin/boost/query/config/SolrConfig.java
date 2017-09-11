package org.lin.boost.query.config;

/**
 * Created by guanl on 6/12/2017.
 */
public class SolrConfig {
    public static String solrURL = "http://localhost:8983/solr/news_core";
    public static String solrAnalysisFilterName = "org.apache.lucene.analysis.core.LowerCaseFilter";
    public static String engineSearchField = "search_all";
    public static int rowSize = 200;

    public static int maxKeywords = 60;
    public static String fieldID = "id";
    public static String fieldScore = "score";
    public static String fieldKeywords = "keywords";

    public static double gradient = 0.5;
    public static double C = 8;
    public static double m = 0.5;

    public static String solrSuggestURL = "http://localhost:8983/solr/suggestion_core";
    public static String solrSuggestSearchField = "terms";
    public static int suggestRowSize = 20;

    public static boolean debug = true;
    public static boolean info = true;
}
