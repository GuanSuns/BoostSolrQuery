package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.AnalysisResponseBase;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.common.SolrDocument;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.redis.Redis;
import org.lin.boost.query.solrj.utils.TermInfo;

import java.util.*;

/**
 * Created by guanl on 6/12/2017.
 */
public class Analysis {
    private SolrClient client;

    public Analysis(){
        client = null;
    }

    @SuppressWarnings("all")
    public Analysis(SolrClient client){
        this.client = client;
    }

    public ArrayList<Double> getTermsBoost(ArrayList<String> terms){
        List<Double> results = new ArrayList<Double>();

        for(int i=0; i<terms.size(); i++){
            Double boost = Double.valueOf(Redis.getBoostValue(terms.get(i)));
            results.add(boost);
        }

        return (ArrayList<Double>)results;
    }

    public static ArrayList<String> getQueryTerms(String queryString) throws Exception{
        if(queryString == null){
            throw new Exception("Uninitialized QueryString");
        }

        ArrayList<String> result = new ArrayList<>();
        String[] splitStrings = queryString.split("[^a-zA-Z]+");
        if(splitStrings.length > 0){
            result.addAll(Arrays.asList(splitStrings));
        }
        return result;
    }

    public static String[] getKeywords(SolrDocument solrDocument){

        String keywordString = (String)(((ArrayList)solrDocument.getFieldValue(SolrConfig.fieldKeywords)).get(0));
        if(keywordString == null || keywordString.equals("")){
            return new String[0];
        }

        String[] keywords = keywordString.split("[^a-zA-Z]+");
        if(keywords.length >= SolrConfig.maxKeywords){
            return Arrays.copyOfRange(keywords, 0, SolrConfig.maxKeywords-1);
        }

        return keywords;
    }

    @SuppressWarnings("all")
    public ArrayList<String> getSolrProcessedQueryTerms(String queryString) throws Exception{
        if(client == null || queryString == null){
            throw new Exception("Uninitialized Client and QueryString");
        }

        FieldAnalysisRequest request = new FieldAnalysisRequest("/analysis/field");
        request.addFieldName(SolrConfig.engineSearchField);
        request.setFieldValue("");
        request.setQuery(queryString);

        FieldAnalysisResponse response = request.process(client);

        List<String> results = new ArrayList<String>();

        Iterator<AnalysisResponseBase.AnalysisPhase> it = response.getFieldNameAnalysis(SolrConfig.engineSearchField)
                .getQueryPhases().iterator();

        while(it.hasNext()) {
            AnalysisResponseBase.AnalysisPhase pharse = (AnalysisResponseBase.AnalysisPhase)it.next();
            if( pharse.getClassName().equals(SolrConfig.solrAnalysisFilterName)){
                List<AnalysisResponseBase.TokenInfo> list = pharse.getTokens();
                for (AnalysisResponseBase.TokenInfo info : list) {
                    results.add(info.getText());
                }
            }
        }

        return (ArrayList<String>)results;
    }

    public ArrayList<String> extractKeywordsByTf(int docID) throws Exception{
        if(client == null){
            throw new Exception("Uninitialized Client");
        }

        ArrayList<String> keywords = new ArrayList<>();
        Query query = new Query(client);

        PriorityQueue<TermInfo> terms = query.getAllTermsById(docID + "");

        int pqSize = terms.size();
        for(int i=0; i<SolrConfig.maxKeywords && i<pqSize; i++){
            TermInfo termInfo = terms.poll();
            keywords.add(termInfo.getTerm());
        }

        return keywords;
    }
}

