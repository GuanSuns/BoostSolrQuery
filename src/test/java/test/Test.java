package test;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.lin.boost.query.redis.Redis;
import org.lin.boost.query.solrj.Solr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanl on 6/12/2017.
 */
public class Test {
        @org.junit.Test
        public void main(){
        Solr solr = new Solr();
        ArrayList<String> queryTerms = new ArrayList<>();
        ArrayList<String> recommendation = new ArrayList<>();

        SolrDocumentList docList = solr.doQuery("taylor swift", queryTerms, recommendation);

        if(docList != null){
            for(SolrDocument doc : docList){
                //printDoc(doc);
            }
        }

        List<Integer> selected = new ArrayList<>();
        selected.add(2);
        //solr.boostDocs(docList, selected, queryTerms);
    }

    private static void printDoc(SolrDocument doc){
        List<String> fieldNames = new ArrayList<String>(doc.getFieldNames());
        for (String field : fieldNames) {
            System.out.println(String.format("%s: %s    ",
                    field, doc.getFieldValue(field)));
        }
        System.out.println();
    }
}
