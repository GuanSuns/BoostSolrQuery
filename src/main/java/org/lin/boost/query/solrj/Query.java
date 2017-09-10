package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.redis.Redis;
import org.lin.boost.query.solrj.utils.TermInfo;

import java.util.*;

/**
 * Created by guanl on 6/12/2017.
 */
public class Query {
    private SolrClient client;

    public Query(){
        this.client = null;
    }

    public Query(SolrClient client){
        this.client = client;
    }

    public void setClient(SolrClient client){
        this.client = client;
    }

    public ArrayList<SolrDocumentWithScore> doQuery(String queryString
            , ArrayList<String> queryTerms
            , ArrayList<String> recommendation) throws Exception{

        if(client == null || queryTerms == null || recommendation == null){
            throw new Exception("Uninitialized Client or termList");
        }

        Analysis analysis = new Analysis(client);
        queryTerms.addAll(analysis.getQueryTerms(queryString));

        ArrayList<Double> boosts = analysis.getTermsBoost(queryTerms);
        if(queryTerms.size() != boosts.size()){
            throw new Exception("Different length of query terms and boost values");
        }

        String newQueryString = SolrConfig.engineSearchField + ":(";
        for(int i=0; i<queryTerms.size(); i++){
            if(i==0){
                newQueryString = newQueryString
                        + queryTerms.get(i)
                        + "^"
                        + boosts.get(i);
            }else{
                newQueryString = newQueryString
                        + " "
                        + queryTerms.get(i)
                        + "^"
                        + boosts.get(i);
            }
        }
        newQueryString = newQueryString + ")";

        if(SolrConfig.debug){
            System.out.println("Begin querying - " + newQueryString);
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setStart(0);
        solrQuery.set("fl", "*,score");
        solrQuery.setRows(SolrConfig.rowSize);
        solrQuery.setQuery(newQueryString);
        QueryResponse response = client.query(solrQuery);

        ArrayList<SolrDocument> unorderedDocuments = response.getResults();
        if(unorderedDocuments == null){
            return new ArrayList<>(0);
        }

        if(SolrConfig.debug){
            System.out.println("Begin sorting documents based on keywords, documents size: " + unorderedDocuments.size());
        }

        return getSortedSolrDocumentBaseOnKeywords(unorderedDocuments);
    }

    private ArrayList<SolrDocumentWithScore> getSortedSolrDocumentBaseOnKeywords(ArrayList<SolrDocument> unorderedDocuments){
        ArrayList<SolrDocumentWithScore> solrDocumentsWithScore = new ArrayList<>();

        //Calculate keywords scores and add to the documents' original scores
        for(SolrDocument solrDocument : unorderedDocuments){
            Float solrScore = (Float)solrDocument.getFieldValue(SolrConfig.fieldScore);
            Float cumulativeScore = solrScore;
            String[] keywords = Analysis.getKeywords(solrDocument);

            for(String keyword : keywords){
                Float termBoost = Float.valueOf(Redis.getBoostValue(keyword));
                cumulativeScore = cumulativeScore * termBoost;
            }

            SolrDocumentWithScore solrDocumentWithScore = new SolrDocumentWithScore();
            solrDocumentWithScore.setScore(cumulativeScore);
            solrDocumentWithScore.setSolrDocument(solrDocument);
            solrDocumentWithScore.setKeywords(keywords);

            solrDocumentsWithScore.add(solrDocumentWithScore);

            if(SolrConfig.debug){
                System.out.println("Processing document " + solrDocument.getFieldValue(SolrConfig.fieldID)
                        + ", original score - " + solrScore
                        + ", # of keywords - " + keywords.length
                        + ", cumulative score - " + cumulativeScore);
            }
        }

        solrDocumentsWithScore.sort(solrDocumentScoreComparator);
        return solrDocumentsWithScore;
    }

    private static Comparator<SolrDocumentWithScore> solrDocumentScoreComparator = new Comparator<SolrDocumentWithScore>(){
        @Override
        public int compare(SolrDocumentWithScore doc1, SolrDocumentWithScore doc2) {
            return (int) (doc2.getScore() - doc1.getScore());
        }
    };

    public SolrDocumentList getAllDocs() throws Exception{
        if(client == null){
            return null;
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setStart(0);
        solrQuery.setRows(10);
        solrQuery.setQuery("*");

        QueryResponse response = client.query(solrQuery);
        SolrDocumentList results = response.getResults();

        return results;
    }

    @SuppressWarnings("unchecked")
    public PriorityQueue<TermInfo> getAllTermsById(String id) throws  Exception {
        if(client == null || id == null){
            return null;
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("id:" + id);
        solrQuery.setRequestHandler("/tvrh");
        solrQuery.setParam("tv.tf_idf", true);
        solrQuery.setParam("tv.tf", true);
        solrQuery.setParam("tv.tv", true);
        solrQuery.setParam("tv.tv.fl",SolrConfig.engineSearchField);

        QueryResponse response = client.query(solrQuery);
        PriorityQueue<TermInfo> termInfos = new PriorityQueue<>(termInfoComparator);

        NamedList<Object> termVectorsNL = (NamedList<Object>)((NamedList<Object>)response.getResponse().get("termVectors")).get(id);
        NamedList<Map.Entry<String, Object>> terms = (NamedList<Map.Entry<String, Object>>)termVectorsNL.get("search_all");

        for(int i=0; i < terms.size(); i++){
            TermInfo termInfo = getTermInfo(i, terms);
            termInfo.TFIDF = termInfo.TFIDF * Double.valueOf(Redis.getBoostValue(termInfo.term));
            termInfos.add(termInfo);
        }

        return termInfos;
    }

    private static Comparator<TermInfo> termInfoComparator = new Comparator<TermInfo>(){
        @Override
        public int compare(TermInfo t1, TermInfo t2) {
            return (int) (t2.getTFIDF() - t1.getTFIDF());
        }
    };

    @SuppressWarnings("all")
    private TermInfo getTermInfo(int i, NamedList<Map.Entry<String, Object>> terms){
        Redis redis = new Redis();
        TermInfo termInfo = new TermInfo(null, 0);
        termInfo.term = terms.getName(i);
        NamedList<Double> properties = (NamedList<Double>)terms.getVal(i);
        termInfo.TFIDF = Double.valueOf(properties.get("tf-idf").toString());
        return termInfo;
    }


}
