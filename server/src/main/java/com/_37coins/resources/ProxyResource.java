package com._37coins.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessagingServletConfig;


@Path(ProxyResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ProxyResource {
    public final static String PATH = "/fonts";
    public static Logger log = LoggerFactory.getLogger(ProxyResource.class);
    static private RequestConfig config = RequestConfig.custom().
            setConnectTimeout(1000).
            setConnectionRequestTimeout(1000).
            setSocketTimeout(1000).build();
    private final HttpClient httpClient;
    
    public static boolean isSucceed(HttpResponse response) {
        return response.getStatusLine().getStatusCode() >= 200
                && response.getStatusLine().getStatusCode() < 300;
    }
    
    public ProxyResource() {
        this.httpClient = HttpClients.custom().disableContentCompression().setDefaultRequestConfig(config).build();
    }
    
    @GET
    @Path("{path: .*}")
    public Response requestResource(@Context UriInfo uriInfo){
        HttpGet req = new HttpGet(MessagingServletConfig.s3Path + uriInfo.getPath());
        HttpResponse response;
        try{
            response = httpClient.execute(req);
            if (isSucceed(response)) {
                String type = response.getFirstHeader("Content-Type").getValue();
                type = (type.contains("x-font"))?"application/font-woff":type;
                ResponseBuilder rb = Response.ok().entity(response.getEntity().getContent());
                for (Header h: response.getAllHeaders())
                    rb.header(h.getName(), h.getValue());
                rb.header("Access-Control-Allow-Origin","*");
                rb.header("Access-Control-Allow-Methods","GET, OPTIONS, HEAD");
                rb.header("Access-Control-Allow-Headers","X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Authorization");
                rb.header("Access-Control-Max-Age","1728000");
                return rb.build();
            }else if (response.getStatusLine().getStatusCode()==404){
                throw new WebApplicationException("not found", Response.Status.NOT_FOUND);
            }else {
                throw new IOException("no result");
            }
        }catch (IOException ex){
            ex.printStackTrace();
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
}
