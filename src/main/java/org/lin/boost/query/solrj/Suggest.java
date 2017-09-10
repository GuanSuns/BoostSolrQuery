package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.config.SolrConfig;

import java.util.ArrayList;

/**
 * Created by guanl on 6/14/2017.
 */
public class Suggest {
    private static SolrClient suggestClient = null;

    private static void initSolrClient(){
        if(suggestClient == null){
            suggestClient = new HttpSolrClient.Builder(SolrConfig.solrSuggestURL).build();
        }
    }

    public Suggest(){
        initSolrClient();
    }

    public ArrayList<String> getSuggestion(ArrayList<String> queryTerms) throws Exception{
        initSolrClient();

        ArrayList<String> suggestion = new ArrayList<>();

        if(queryTerms.size() == 0){
            return suggestion;
        }

        String newQueryString = SolrConfig.solrSuggestSearchField+ ":(";
        for(int i=0; i<queryTerms.size(); i++){
            if(i==0){
                newQueryString = newQueryString
                        + queryTerms.get(i);
            }else{
                newQueryString = newQueryString
                        + " "
                        + queryTerms.get(i);
            }
        }
        newQueryString = newQueryString + ")";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setStart(0);
        solrQuery.setRows(SolrConfig.suggestRowSize);
        solrQuery.setQuery(newQueryString);
        QueryResponse response = suggestClient.query(solrQuery);

        SolrDocumentList suggestList = response.getResults();

        if(suggestList != null && suggestList.size() > 0){
            SolrDocument doc = suggestList.get(0);
            String terms = ((ArrayList<String>)(doc.getFieldValue(SolrConfig.solrSuggestSearchField))).get(0);
            for(String term : terms.split(" ")){
                suggestion.add(term);
            }
        }

        return suggestion;
    }
}
