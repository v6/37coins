package com._37coins;

import javax.inject.Singleton;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;

@Singleton
public class EhCacheWrapper implements Cache {
    
    private net.sf.ehcache.Cache cache;
    
    public EhCacheWrapper(net.sf.ehcache.Cache cache) {
        this.cache = cache;
    }

    @Override
    public void put(Element element) {
        cache.put(new net.sf.ehcache.Element(element.getKey(),element.getObjectValue()));
    }

    @Override
    public Element get(String key) {
        net.sf.ehcache.Element e = cache.get(key); 
        if (null==e)
            return null;
        Element rv = new Element(e.getObjectKey().toString(), e.getObjectValue());
        rv.setExpired(e.isExpired());
        rv.setCreationTime(e.getCreationTime());
        return rv;
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public Element getQuiet(String key) {
        net.sf.ehcache.Element e = cache.getQuiet(key);
        if (null==e)
            return null;
        Element rv = new Element(e.getObjectKey().toString(), e.getObjectValue());
        rv.setExpired(e.isExpired());
        rv.setCreationTime(e.getCreationTime());
        return rv;
    }

    @Override
    public void flush() {
        cache.flush();
    }

    @Override
    public long incr(String key) {
        net.sf.ehcache.Element e = cache.get(key);
        if (null==e)
            return 0;
        return e.getHitCount();
    }

}
