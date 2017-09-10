package org.lin.boost.query.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.redis.Redis;
import org.lin.boost.query.stemmer.PorterStemmer;

import javax.sound.sampled.Port;
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

    public void doBoost(ArrayList<SolrDocumentWithScore> docList
            , List<Integer> selectedItem, ArrayList<String> queryTerms) throws Exception{

        if(client == null || selectedItem == null || docList == null){
            throw new Exception("Uninitialized Variables");
        }
        if(docList.size() == 0 || selectedItem.size() == 0){
            return;
        }

        Set<String> boostedTerms = new HashSet<>();
        for(String queryTerm : queryTerms){
            boostTerm(queryTerm);
            boostedTerms.add(PorterStemmer.stem(queryTerm));
        }

        int smallestIndex = Integer.MAX_VALUE;
        for(int i=0; i<selectedItem.size(); i++){
            int j = selectedItem.get(i);
            if(j < smallestIndex) smallestIndex = j;

            String[] keywords = docList.get(j).getKeywords();

            for (String keyword : keywords){
                String stemWord = PorterStemmer.stem(keyword);
                if(!boostedTerms.contains(stemWord)){
                    boostTerm(stemWord);
                    boostedTerms.add(stemWord);
                }
            }
        }

        doDegrade(docList, smallestIndex, boostedTerms);
    }

    private void doDegrade(ArrayList<SolrDocumentWithScore> docList
            , int indexDegrade, Set<String> boostedTerms) throws Exception{

        if(client == null){
            throw new Exception("Uninitialized Client");
        }
        if(indexDegrade == 0) return;

        Set<String> degradedTerms = new HashSet<>();

        for(int i=0; i<indexDegrade; i++){
            SolrDocumentWithScore doc = docList.get(i);
            String[] keywords = doc.getKeywords();

            for (String keyword : keywords){
                String stemWord = PorterStemmer.stem(keyword);
                if(!degradedTerms.contains(stemWord)
                        && !boostedTerms.contains(stemWord)){
                    degradeTerm(stemWord);
                    degradedTerms.add(stemWord);
                }
            }
        }
    }

    private void boostTerm(String term){
        double k = Math.log(SolrConfig.C - 1.0);
        double boost = Double.valueOf(Redis.getBoostValue(term));
        double x = (k - Math.log(SolrConfig.C/boost - 1.0))/SolrConfig.m + SolrConfig.gradient;
        boost = SolrConfig.C/(1+Math.exp(-SolrConfig.m*x+k));
        Redis.setBoostValue(term, boost+"");
    }

    private void degradeTerm(String term){
        double k = Math.log(SolrConfig.C - 1.0);
        double boost = Double.valueOf(Redis.getBoostValue(term));
        double x = (k - Math.log(SolrConfig.C/boost - 1.0))/SolrConfig.m - SolrConfig.gradient;
        if(x < 0){
            x = 0;
        }
        boost = SolrConfig.C/(1+Math.exp(-SolrConfig.m*x+k));
        Redis.setBoostValue(term, boost+"");
    }

    private static void printSetTerms(Set<String> Terms){
        for(String term: Terms){
            double boost = Double.valueOf(Redis.getBoostValue(term));
            System.out.println("Term: " + term + ", Boost: " + boost);
        }
    }

}
