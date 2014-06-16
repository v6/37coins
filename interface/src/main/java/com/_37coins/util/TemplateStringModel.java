package com._37coins.util;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateStringModel implements TemplateHashModel, TemplateMethodModelEx, TemplateModel {
    private final ResourceBundle resourceBundle;
    
    public TemplateStringModel(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        return new StringModel(resourceBundle.getString(key), new BeansWrapper());
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // Must have at least one argument - the key
        if(arguments.size() < 1)
            throw new TemplateModelException("No message key was specified");
        // Read it
        Iterator it = arguments.iterator();
        String key = it.next().toString();
        String val = resourceBundle.getString(key);
        MessageFormat mf = new MessageFormat(val);
        mf.setLocale(resourceBundle.getLocale());
        int args = arguments.size() - 1;
        Object[] params = new Object[args];
        for(int i = 0; i < args; ++i)
            params[i] = it.next();
        return mf.format(params);
    }

}
