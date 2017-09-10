package test;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.junit.Test;
import org.lin.boost.query.solrj.Solr;
import org.lin.boost.query.solrj.SolrDocumentWithScore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanl on 6/12/2017.
 */
public class SolrTest {

    @Test
    public void main() throws Exception{
        Solr solr = new Solr();
        SolrClient client = solr.getClient();

        ArrayList<String> queryTerms = new ArrayList<>();
        ArrayList<String> suggestions = new ArrayList<>();
        ArrayList<SolrDocumentWithScore> results = solr.doQuery("Los Angle", queryTerms, suggestions);

        System.out.println();
        for(SolrDocumentWithScore solrDocumentWithScore : results){
            printDoc(solrDocumentWithScore.getSolrDocument());
        }

        ArrayList<Integer> selectedItem = new ArrayList<>();
        selectedItem.add(2);
        selectedItem.add(3);
        solr.boostDocs(results, selectedItem, queryTerms);
    }

    private static void printDoc(SolrDocument doc){
        List<String> fieldNames = new ArrayList<>(doc.getFieldNames());
        for (String field : fieldNames) {
            if(field.equals("content")){
                continue;
            }

            System.out.println(String.format("%s: %s    ",
                    field, doc.getFieldValue(field)));
        }
        System.out.println();
    }
}
