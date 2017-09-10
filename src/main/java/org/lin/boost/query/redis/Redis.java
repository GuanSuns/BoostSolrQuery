package org.lin.boost.query.redis;

import org.lin.boost.query.config.RedisConfig;
import org.lin.boost.query.config.SolrConfig;
import org.lin.boost.query.stemmer.PorterStemmer;
import org.tartarus.martin.Stemmer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by guanl on 6/12/2017.
 */
public class Redis {
    private static JedisPool pool = null;

    private static JedisPool getPool(){
        if(pool == null){
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(RedisConfig.maxIdle);
            config.setMaxWaitMillis(RedisConfig.maxWait);
            pool = new JedisPool(config, RedisConfig.hostname);
        }
        return pool;
    }

    @SuppressWarnings("deprecation")
    private static void returnResource(JedisPool pool, Jedis jedis){
        if(jedis != null){
            pool.returnResource(jedis);
        }
    }

    @SuppressWarnings("deprecation")
    private static String getValue(String key){
        JedisPool pool = null;
        Jedis jedis = null;
        String value = null;
        try{
            pool = getPool();
            jedis = pool.getResource();
            jedis.auth(RedisConfig.password);
            value = jedis.get(key);
        }catch (Exception e){
            try{
                pool.returnBrokenResource(jedis);
            }catch (Exception brokenException){
                brokenException.printStackTrace();
            }
            e.printStackTrace();
        }finally {
            returnResource(pool, jedis);
        }

        return value;
    }

    @SuppressWarnings("deprecation")
    public static void setValue(String key, String value){
        JedisPool pool = null;
        Jedis jedis = null;

        try{
            pool = getPool();
            jedis = pool.getResource();
            jedis.auth(RedisConfig.password);
            jedis.set(key, value);
        }catch (Exception e){
            try{
                pool.returnBrokenResource(jedis);
            }catch (Exception brokenException){
                brokenException.printStackTrace();
            }
            e.printStackTrace();
        }finally {
            returnResource(pool, jedis);
        }
    }


    public static void setBoostValue(String term, String value){
        String stemWord  = PorterStemmer.stem(term);
        setValue(stemWord, value);
    }

    public static String getBoostValue(String term){
        String stemWord  = PorterStemmer.stem(term);

        String boost = getValue(stemWord);
        if(boost == null){
            setBoostValue(term, "1.0");
            return "1.0";
        }else{
            return boost;
        }
    }


}

