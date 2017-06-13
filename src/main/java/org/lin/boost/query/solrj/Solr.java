package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.config.SolrConfig;

import java.util.List;

/**
 * Created by guanl on 6/12/2017.
 */
public class Solr {
    private static final SolrClient client = new HttpSolrClient.Builder(SolrConfig.solrURL).build();

    public SolrDocumentList doQuery(String queryString){
        Query query = new Query(client);
        SolrDocumentList results;
        try{
            results = query.doQuery(queryString);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return results;
    }

    public void boostDocs(SolrDocumentList docList, List<Integer> selectedItem){
        Boost boost = new Boost(client);
        try{
            boost.doBoost(docList, selectedItem);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
