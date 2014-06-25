package com._37coins;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.util.ResourceBundle;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.util.TemplateStringModel;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.Signup;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MessageFactory {
	public static Logger log = LoggerFactory.getLogger(MessageFactory.class);
	// where to find the templates when running in web container
	public static final String RESOURCE_PATH = "/WEB-INF/templates/";
	// where to find the templates when outside web container
	public static final String LOCAL_RESOURCE_PATH = "src/main/webapp/WEB-INF/templates/";
	public static final String CT_TEXT_HTML = "text/html";
	public static final String CT_PLAIN_TEXT = "text/plain";
	public static final String TEXT_FOLDER = "text/";
	public static final String HTML_FOLDER = "email/";
	

	private final Configuration cfg;
	private final ResourceBundleFactory resourceBundleFactory;
	private ResourceBundle rb;
	private int unitFactor;
	private String unitName;
	private String unitFormat;

	public MessageFactory(ServletContext servletContext, ResourceBundleFactory resourceBundleFactory, int unitFactor, String unitName, String unitFormat) {
	    this.resourceBundleFactory = resourceBundleFactory;
	    this.unitFactor = unitFactor;
	    this.unitName = unitName;
	    this.unitFormat = unitFormat;
		cfg = new Configuration();
		if (servletContext == null) {
			try {
				cfg.setDirectoryForTemplateLoading(new File(LOCAL_RESOURCE_PATH));
			} catch (IOException e) {
				log.error("message factory exception",e);
				e.printStackTrace();
			}
		} else {
			cfg.setServletContextForTemplateLoading(servletContext,
					RESOURCE_PATH);
		}
	}
    
    private void prepare(DataSet rsp) throws MalformedURLException {
		if (null == rsp.getResBundle()) {
		    rsp.setUnitFactor(unitFactor);
		    rsp.setUnitName(unitName);
		    rsp.setUnitFormat(unitFormat);
			rb = resourceBundleFactory.getBundle(rsp.getLocale(), ResourceBundleFactory.CLASS_NAME);
			rsp.setResBundle(new TemplateStringModel(rb));
		}
	}
	
	public Locale getLocale(DataSet rsp) throws MalformedURLException{
		prepare(rsp);
		return rb.getLocale();
	}

	public String constructHtml(DataSet rsp)
			throws IOException, TemplateException {
		prepare(rsp);
		return processTemplate(rsp, HTML_FOLDER);
	}
	
	public String constructTxt(DataSet rsp)
			throws IOException, TemplateException {
	    if (rsp.getAction()==Action.SIGNUP && rsp.getPayload()!=null && rsp.getPayload() instanceof Signup){
	        Signup signup = ((Signup)rsp.getPayload());
	        if (signup.getWelcomeMessage()!=null && signup.getWelcomeMessage().length()>10){
	            return signup.getWelcomeMessage();
	        }
	    }
		prepare(rsp);
		return processTemplate(rsp, TEXT_FOLDER);
	}

	public String constructSubject(DataSet rsp) throws IOException, TemplateException {
		prepare(rsp);
		String subjectPrefix= rsp.getAction().getText();
		Template template = new Template("name", rb.getString(subjectPrefix+"Subject"),cfg); 
		Writer out = new StringWriter(); 
		template.process(rsp, out); 
		return out.toString();
	}
	
	public String getText(String key, DataSet data) throws IOException, TemplateException{
		prepare(data);
		Template template = new Template("name", rb.getString(key),cfg); 
		Writer out = new StringWriter(); 
		template.process(data, out);
		return out.toString();
	}

	public String processTemplate(DataSet rsp, String folder) throws IOException,
			TemplateException {
		String filePath = null;
		if (null!=folder){
			filePath = folder + rsp.getAction().getText() + ((folder.contains(HTML_FOLDER))?".html":".txt");
		}else{
			filePath = rsp.getService();
		}
		// make email template
		Template template = cfg.getTemplate(filePath);

		Writer stringWriter = null;

		// create html mail part
		stringWriter = new StringWriter();
        rsp.setUnitFactor(unitFactor);
        rsp.setUnitName(unitName);
        rsp.setUnitFormat(unitFormat);
		template.process(rsp, stringWriter);

		stringWriter.flush();
		return stringWriter.toString();
	}

}
