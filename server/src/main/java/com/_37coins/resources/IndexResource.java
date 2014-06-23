package com._37coins.resources;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.util.ResourceBundle;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.workflow.pojo.DataSet;
import com.maxmind.geoip.LookupService;

import freemarker.template.TemplateException;

@Path(IndexResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {
    public final static String WILDCARDS = "wildcards";
    public final static String RES_PATH = "resPath";
    public final static String BASE_PATH = "basePath";
    public final static String TRACK_ID = "gaTrackingId";
    public final static String SRVC_PATH = "srvcPath";
    public final static String CAPT_KEY = "captchaPubKey";
    public final static String COUNTRY = "country";
    public final static String ACT_LOC = "activeLocales";
    public final static String LANG = "lng";
    public final static String DESC = "desc";
    public final static String TITLE = "title";
	public final static String PATH = "/";
	public static Logger log = LoggerFactory.getLogger(IndexResource.class);
	
	public static Map<String,Object> prepare(String lng, 
	        UriInfo uriInfo, 
	        LookupService lookupService, 
	        HttpServletRequest httpReq, 
	        ResourceBundleFactory rbf){
	    Map<String,Object> data = new HashMap<>();
        data.put(RES_PATH, MessagingServletConfig.resPath);
        data.put(BASE_PATH, MessagingServletConfig.basePath);
        data.put(TRACK_ID, MessagingServletConfig.gaTrackingId);
        data.put(SRVC_PATH, MessagingServletConfig.srvcPath);
        data.put(CAPT_KEY, MessagingServletConfig.captchaPubKey);
        data.put(ACT_LOC, rbf.getActiveLocales());
        
        //handle country
        String country = null;
        try{
            country = lookupService.getCountry(TicketResource.getRemoteAddress(httpReq)).getCode();
            country = (country.equals("--"))?null:country;
        }catch(Exception e){
            log.error("geoip exception",e);
            e.printStackTrace();
        }
        data.put(COUNTRY, country);
        
        //deal with language
        List<PathSegment> segments = uriInfo.getPathSegments();
        Locale locale = null;
        int segmentCount = 0;
        //check language by url
        if (null!=segments 
                && segments.size()>0 
                && segments.get(0).getPath().length()==2){
            locale = new Locale(segments.get(0).getPath());
            segmentCount++;
        }
        //check by request header
        if ((null == locale || locale.getLanguage().length()<2) && lng!=null){
            locale = Locale.forLanguageTag(lng);
        }
        //go by config
        if (null == locale || locale.getLanguage().length()<2){
            locale = rbf.getActiveLocales().get(0);
        }
        data.put(LANG, locale.toString());
        ResourceBundle rb = rbf.getBundle(locale, "labels");
        //deal with description
        String key = "";
        String parentSegment = null;
        List<String> wildcardSegments = rb.getStringList(WILDCARDS);
        for (int i = segmentCount; i < segments.size(); i++){
            String segment = (segments.get(i).toString().length()>0)?segments.get(i).toString():null;
            if (null!=parentSegment && null!=wildcardSegments){
                for (String wildcard : wildcardSegments){
                    if (wildcard.equals(parentSegment)){
                        segment = "*";
                    }
                }
            }
            key = (null!=segment)?key + "-" + segment:key;
            parentSegment = segment;
        }
        key = (key.length()<2)?"-index":key;
        String desc = rb.getString("d"+key);
        if (null==desc)
            desc = rb.getString("d-index");
        String title = rb.getString("t"+key);
        if (null==title)
            title = rb.getString("t-index");
        data.put(DESC, desc);
        data.put(TITLE, title);
        return data;
	}
	
	final private MessageFactory htmlFactory;
    final private HttpServletRequest httpReq;
    final private LookupService lookupService;
    final private ResourceBundleFactory rbf;
	
	   
    @Inject public IndexResource(ServletRequest request,
            MessageFactory htmlFactory, 
            ResourceBundleFactory rbf,
            LookupService lookupService) {
        this.httpReq = (HttpServletRequest)request;
        this.rbf = rbf;
        this.htmlFactory = htmlFactory;
        this.lookupService = lookupService;
    }

	@GET
	public Response index(@HeaderParam("Accept-Language") String lng, @Context UriInfo uriInfo){
	    Map<String,Object> payload = prepare(lng, uriInfo, lookupService, httpReq, rbf);
		DataSet ds = new DataSet()
			.setService("index.html")
			.setPayload(payload);
		String rsp;
		try {
			rsp = htmlFactory.processTemplate(ds, null);
		} catch (IOException | TemplateException e) {
		    e.printStackTrace();
			throw new WebApplicationException("template not loaded",
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		return Response.ok(rsp, MediaType.TEXT_HTML_TYPE).build();
	}
	
	@GET
	@Path("robots.txt")
	public Response robots(){
		String response = "User-agent: *\n" +
				"Disallow: /ticket/\n" +
				"Disallow: /account/\n" +
				"Disallow: /envayaTest.html\n" +
				"Sitemap: "+MessagingServletConfig.resPath+"/sitemap.xml";
		return Response.ok(response, MediaType.TEXT_PLAIN_TYPE).build();
	}
	
	@GET
	@Path("sitemap.xml")
	public Response sitemap(){
		return Response.seeOther(URI.create(MessagingServletConfig.resPath+"/sitemap.xml")).build();
	}
	
	@GET
	@Path("favicon.ico")
	public Response favicon(){
	    return Response.seeOther(URI.create(MessagingServletConfig.resPath+"/images/faveicon32.png")).build();
	}
	
	@GET
	@Path("{path: .*}")
	public Response fullindex(@HeaderParam("Accept-Language") String lng, @Context UriInfo uriInfo){
		return index(lng, uriInfo);
	}
	
	/*
	 * ###################### TEST SCOPE
	 */
	
	@GET
	@Path("envayaTest.html")
	public Response envayaTest(){
		Map<String,String> data = new HashMap<>();
		data.put("lng", "en-US");
		DataSet ds = new DataSet()
			.setService("envayaTest.html")
			.setPayload(data);
		String rsp;
		try {
			rsp = htmlFactory.processTemplate(ds, null);
		} catch (IOException | TemplateException e) {
			throw new WebApplicationException("template not loaded",
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		return Response.ok(rsp, MediaType.TEXT_HTML_TYPE).build();
	}
	
}