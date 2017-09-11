package org.lin.boost.query.stemmer;

import org.lin.boost.query.config.PorterStemmerConfig;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.solrj.Solr;
import org.tartarus.martin.Stemmer;

import java.util.HashMap;

public class PorterStemmer {
    private static HashMap<String, String> cache = null;

    private static void initCache(){
        cache = new HashMap<>(PorterStemmerConfig.cacheSize);
    }

    private static String readFromCache(String originalWord){
        String word = originalWord.toLowerCase();

        if(cache.containsKey(word)){
            return cache.get(word);
        }

        return null;
    }

    private static void addToCache(String stemWord, String originalWord){
        if(cache.size() < PorterStemmerConfig.cacheSize){
            cache.put(originalWord.toLowerCase(), stemWord);
        }else{
            //Remove One Key
            String firstKey = "";
            for(String key : cache.keySet()){
                firstKey = key;
                break;
            }
            cache.remove(firstKey);

            cache.put(originalWord.toLowerCase(), stemWord);
        }
    }

    public static String stem(String term){
        if(cache == null){
            initCache();
        }

        String cacheStemWord = readFromCache(term);
        if(cacheStemWord != null){
            if(PorterStemmerConfig.PorterCacheDebug){
                System.out.println("Read from the cache : "
                        + term + " - " + cacheStemWord);
            }
            return cacheStemWord;
        }

        Stemmer stemmer = new Stemmer();
        stemmer.add(term.toLowerCase().toCharArray(), term.length());
        stemmer.stem();
        String stemWord = stemmer.toString();

        addToCache(stemWord, term);
        if(PorterStemmerConfig.PorterCacheDebug){
            System.out.println("Add " + term + " - " + stemWord + " to cache");
        }

        return stemWord;
    }
}
