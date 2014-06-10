package com._37coins.resources;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.CryptoUtils;
import com._37coins.MerchantServletConfig;
import com._37coins.persistence.dao.Gateway;


@Path(AmqpResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AmqpResource {
    public final static String PATH = "/amqp";
    
    private final GenericRepository dao;
    
    @Inject public AmqpResource(ServletRequest request) {
        HttpServletRequest httpReq = (HttpServletRequest)request;
        dao = (GenericRepository)httpReq.getAttribute("gr");
    }
    
    @GET
    @Path("/user")
    public String user(@QueryParam("username") String username,@QueryParam("password") String password){
        if (username.equals(MerchantServletConfig.amqpUser))
            return (password.equals(MerchantServletConfig.amqpPassword))?"allow [administrator]":"deny";
        Gateway g = dao.queryEntity(new RNQuery().addFilter("cn", username), Gateway.class);
        try {
            return (CryptoUtils.verifySaltedPassword(password.getBytes(),g.getPassword()))?"allow":"deny";
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return "deny";
        }
    }

    @GET
    @Path("/vhost")
    public String vhost(@QueryParam("username") String username, @QueryParam("vhost") String vhost){
        return "allow";
    }
    
    @GET
    @Path("/res")
    public String res(@QueryParam("username") String username,
            @QueryParam("vhost") String vhost, 
            @QueryParam("resource") String resource, 
            @QueryParam("name") String name, 
            @QueryParam("permission") String permission){
        if (username.equals(MerchantServletConfig.amqpUser))
            return "allow";
        return (resource.equals("queue") && username.equals(name))?"allow":"deny";
    }

}
