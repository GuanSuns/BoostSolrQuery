package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.config.SolrConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanl on 6/12/2017.
 */
public class Solr {
    private static final SolrClient client = new HttpSolrClient.Builder(SolrConfig.solrURL).build();

    /** The ArrayList queryTerms and  recommendation should be
     * empty ArrayList used to receive result from Solr Query,
     * so that relative information could be printed out somewhere.
     *
     * Currently, only support space-separated query terms
     **/
    public ArrayList<SolrDocumentWithScore> doQuery(String queryString, ArrayList<String> queryTerms, ArrayList<String> recommendation){
        Query query = new Query(client);
        ArrayList<SolrDocumentWithScore> results;
        try{
            results = query.doQuery(queryString, queryTerms, recommendation);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return results;
    }

    public void boostDocs(SolrDocumentList docList, List<Integer> selectedItem, ArrayList<String> queryTerms){
        Boost boost = new Boost(client);
        try{
            boost.doBoost(docList, selectedItem, queryTerms);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public SolrClient getClient(){
        return Solr.client;
    }
}
