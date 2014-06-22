package com._37coins.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.ws.rs.core.MultivaluedMap;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;

public class ResourceBundle {
    public final static String CACHE_TAG = "com.37coins.resBundle";
    private Locale locale;
    private List<Locale> activeLocales;
    private String className;
    private final Cache cache;
    private final ResourceBundleClient client;
    
    public ResourceBundle (Locale locale, Cache cache, List<Locale> activeLocales, ResourceBundleClient client, String className){
        this.locale = locale;
        this.cache = cache;
        this.activeLocales = activeLocales;
        this.client = client;
        this.className = className;
    }
    
    public Locale getLocale() {
        if (locale.getCountry()!="" && activeLocales.contains(locale))
            return locale;
        Locale lang = new Locale(locale.getLanguage());
        if (activeLocales.contains(lang))
            return lang;
        return activeLocales.get(0);
    }
    
    public String getString(String key){
        List<String> rv = getStringList(key);
        return (null!=rv)?getStringList(key).get(0):null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key){
        if (null==key)
            throw new NullPointerException("key is null");
        List<Locale> toCheck = new ArrayList<>();
        toCheck.add(new Locale(""));
        Locale lang = new Locale(locale.getLanguage());
        if (activeLocales.contains(lang))
            toCheck.add(lang);
        if (locale.getCountry()!="" && activeLocales.contains(locale))
            toCheck.add(locale);
        List<MultivaluedMap<String,String>> bundles = new ArrayList<>();
        for (Locale l: toCheck){
            Element el = null;
            if (null!=cache){
                el = cache.get(CACHE_TAG+l.toString());
            }
            if (el!=null && !el.isExpired()){
                bundles.add((MultivaluedMap<String,String>)el.getObjectValue());
            }else{
                MultivaluedMap<String,String> map = null;
                try{
                    map = client.fetchBundle(l.toString(), className);
                }catch(Exception ex){
                    ex.printStackTrace();
                    throw new MissingResourceException(ex.getMessage(), className, key);
                }
                if (null!=cache){
                    cache.put(new Element(CACHE_TAG+l.toString(),map));
                }
                bundles.add(map);
            }
        }
        Collections.reverse(bundles);
        for (MultivaluedMap<String,String> bundle: bundles){
            if (bundle.containsKey(key)){
                return bundle.get(key);
            }
        }
        return null;
    }

}
