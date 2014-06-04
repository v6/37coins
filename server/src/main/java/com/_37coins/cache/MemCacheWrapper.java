package com._37coins.cache;

import net.spy.memcached.MemcachedClient;

public class MemCacheWrapper implements Cache {
    
    final private MemcachedClient client;
    private int lifetime;
    
    public MemCacheWrapper(MemcachedClient client, int lifetime) {
        this.client =client;
        this.lifetime = lifetime;
    }

    @Override
    public void put(Element element) {
        client.set(element.getKey(), lifetime, element.getObjectValue());
    }

    @Override
    public Element get(String key) {
        Object o = client.get(key);
        if (null==o)
            return null;
        Element rv = new Element(key,o);
        rv.setExpired(false);
        rv.setCreationTime(System.currentTimeMillis());
        return rv;
    }

    @Override
    public void remove(String key) {
        client.delete(key);
        client.delete(key+"counter");
    }

    @Override
    public Element getQuiet(String key) {
        Object o = client.get(key);
        if (null==o)
            return null;
        Element rv = new Element(key,o);
        rv.setExpired(false);
        rv.setCreationTime(System.currentTimeMillis());
        return rv;
    }

    @Override
    public void flush() {
        client.flush();
    }

    @Override
    public long incr(String key) {
        return client.incr(key, 1);
    }  

}