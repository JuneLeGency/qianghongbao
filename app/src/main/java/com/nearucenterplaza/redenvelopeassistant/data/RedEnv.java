package com.nearucenterplaza.redenvelopeassistant.data;

import com.orm.SugarRecord;

/**
 * Created by Administrator on 2015/9/29.
 */
public class RedEnv extends SugarRecord<RedEnv> {

    long hash;
    long time;

    public RedEnv(){
    }

    public RedEnv(long hash, long time) {
        this.hash = hash;
        this.time = time;
    }
}
