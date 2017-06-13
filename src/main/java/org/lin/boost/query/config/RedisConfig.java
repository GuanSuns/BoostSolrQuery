package org.lin.boost.query.config;

/**
 * Created by guanl on 6/12/2017.
 */
public class RedisConfig {
    public static final String password = "guanlin25";
    public static final String hostname = "localhost";

    public static final int maxIdle = 5;
    public static final int maxActive = 500;
    public static final int maxWait = 1000 * 60;
}
