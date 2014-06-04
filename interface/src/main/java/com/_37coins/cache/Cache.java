package com._37coins.cache;


public interface Cache {
    
    public void put(Element element);

    public Element get(String key);

    public void remove(String key);

    public Element getQuiet(String key);

    public void flush(); 
    
    public long incr(String key);

}
