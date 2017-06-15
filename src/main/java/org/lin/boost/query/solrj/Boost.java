package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.redis.Redis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by guanl on 6/12/2017.
 */
public class Boost {
    private SolrClient client;

    public Boost(SolrClient client){
        this.client = client;
    }

    public void doBoost(SolrDocumentList docList, List<Integer> selectedItem, ArrayList<String> queryTerms) throws Exception{
        if(client == null || selectedItem == null || docList == null){
            throw new Exception("Uninitialized Variables");
        }
        if(docList.size() == 0 || selectedItem.size() == 0){
            return;
        }

        Set<String> boostedTerms = new HashSet<>();
        Analysis analysis = new Analysis(client);

        int smallestIndex = Integer.MAX_VALUE;
        for(int i=0; i<selectedItem.size(); i++){
            int j = selectedItem.get(i);
            if(j < smallestIndex) smallestIndex = j;

            SolrDocument doc = docList.get(j);
            String id = doc.getFieldValue(SolrConfig.fieldID).toString();

            ArrayList<String> keywords = analysis.extractKeywords(Integer.valueOf(id));

            for (int k=0; k<keywords.size(); k++){
                String term = keywords.get(k);
                if(!boostedTerms.contains(term)){
                    boostedTerms.add(term);
                    boostTerm(term);
                }
            }
        }

        /*
        if(queryTerms != null){
            for(String term: queryTerms){
                if(!boostedTerms.contains(term)){
                    boostedTerms.add(term);
                    boostTerm(term);
                }
            }
        }
        */

        doDegrade(docList, smallestIndex);
    }

    private void doDegrade(SolrDocumentList docList, int iDegrade) throws Exception{
        if(client == null){
            throw new Exception("Uninitialized Client");
        }
        if(iDegrade == 0) return;

        Set<String> degradedTerms = new HashSet<>();
        Analysis analysis = new Analysis(client);

        for(int i=0; i<iDegrade; i++){
            SolrDocument doc = docList.get(i);
            String id = doc.getFieldValue(SolrConfig.fieldID).toString();
            ArrayList<String> keywords = analysis.extractKeywords(Integer.valueOf(id));

            for (int j=0; j<keywords.size(); j++){
                String term = keywords.get(j);
                if(!degradedTerms.contains(term)){
                    degradedTerms.add(term);
                    degradeTerm(term);
                }
            }
        }
    }

    private void boostTerm(String term){
        double k = Math.log(SolrConfig.C - 1.0);
        double boost = Double.valueOf(Redis.getBoost(term));
        double x = (k - Math.log(SolrConfig.C/boost - 1.0))/SolrConfig.m + SolrConfig.gradient;
        boost = SolrConfig.C/(1+Math.exp(-SolrConfig.m*x+k));
        Redis.setValue(term, boost+"");
    }

    private void degradeTerm(String term){
        double k = Math.log(SolrConfig.C - 1.0);
        double boost = Double.valueOf(Redis.getBoost(term));
        double x = (k - Math.log(SolrConfig.C/boost - 1.0))/SolrConfig.m - SolrConfig.gradient;
        if(x < 0){
            x = 0;
        }
        boost = SolrConfig.C/(1+Math.exp(-SolrConfig.m*x+k));
        Redis.setValue(term, boost+"");
    }

    private static void printSetTerms(Set<String> Terms){
        for(String term: Terms){
            double boost = Double.valueOf(Redis.getBoost(term));
            System.out.println("Term: " + term + ", Boost: " + boost);
        }
    }

}
