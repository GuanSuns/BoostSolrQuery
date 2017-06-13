package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
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
            termInfo.TFIDF = termInfo.TFIDF * Double.valueOf(Redis.getBoost(termInfo.term));
            termInfos.add(termInfo);
        }

        return termInfos;
    }

    public static Comparator<TermInfo> termInfoComparator = new Comparator<TermInfo>(){
        @Override
        public int compare(TermInfo t1, TermInfo t2) {
            return (int) (t2.getTFIDF() - t2.getTFIDF());
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

    public SolrDocumentList doQuery(String queryString) throws Exception{
        if(client == null){
            throw new Exception("Uninitialized Client");
        }

        Analysis analysis = new Analysis(client);
        ArrayList<String> terms = analysis.getQueryTerms(queryString);
        ArrayList<Double> boosts = analysis.getTermsBoost(terms);
        if(terms.size() != boosts.size()){
            throw new Exception("Different length of terms and boosts");
        }

        String newQueryString = SolrConfig.engineSearchField + ":(";
        for(int i=0; i<terms.size(); i++){
            if(i==0){
                newQueryString = newQueryString
                        + terms.get(i)
                        + "^"
                        + boosts.get(i);
            }else{
                newQueryString = " "
                        + newQueryString
                        + terms.get(i)
                        + "^"
                        + boosts.get(i);
            }
        }
        newQueryString = newQueryString + ")";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setStart(0);
        solrQuery.setRows(SolrConfig.rowSize);
        solrQuery.setQuery(newQueryString);
        QueryResponse response = client.query(solrQuery);

        return response.getResults();
    }
}
