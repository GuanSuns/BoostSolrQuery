package org.lin.boost.query.stemmer;

import org.lin.boost.query.config.SolrConfig;
import org.tartarus.martin.Stemmer;

public class PorterStemmer {
    public static String stem(String term){
        Stemmer stemmer = new Stemmer();
        stemmer.add(term.toLowerCase().toCharArray(), term.length());
        stemmer.stem();
        String stemWord = stemmer.toString();

        if(SolrConfig.debug){
            System.out.println("Stem " + term + " to " + stemWord);
        }

        return stemWord;
    }
}
