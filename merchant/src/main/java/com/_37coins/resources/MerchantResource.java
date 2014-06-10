package com._37coins.resources;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.RandomStringUtils;

import com._37coins.workflow.pojo.Withdrawal;

@Path(MerchantResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MerchantResource {
	public final static String PATH = "/charges";

	private final Cache hourCache;
	private final Cache dayCache;
	
	@Inject
	public MerchantResource(@Named("hour") Cache hourCache,
			@Named("day") Cache dayCache){
		this.hourCache = hourCache;
		this.dayCache = dayCache;
	}
	
	@POST
	@Path("/{type}")
	public Map<String,String> create(Withdrawal charge, @PathParam("type")String type){
		Cache cache = type.equals("product")?dayCache:hourCache;
		if (null == charge || null == charge.getAmount()||null == charge.getPayDest()){
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		String token = null;
		int i = 2;
		while (null==token){
			token = RandomStringUtils.random(i, "abcdefghijkmnopqrstuvwxyz123456789");
			//check collision, if yes, increase length
			if (null==dayCache.get("charge"+token)
			        &&null==hourCache.get("charge"+token)){
				break;
			}
			i++;
		}
		cache.put(new Element("charge"+token,charge));
		Map<String,String> rv = new HashMap<String,String>();
		rv.put("token", token);
		return rv;
	}
	
	@GET
	@Path("/{type}")
	public Withdrawal get(@QueryParam("token") String token, @PathParam("type")String type){
		Cache cache = type.equals("product")?dayCache:hourCache;
		Element e = cache.get("charge"+token);
		if (null!=e){
			return (Withdrawal)e.getObjectValue();
		}else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
	
	@DELETE
	@Path("/{type}")
	public void delete(@QueryParam("token") String token, @PathParam("type")String type){
		Cache cache = type.equals("product")?dayCache:hourCache;
		Element e = cache.get("charge"+token);
		if (null!=e){
			cache.remove("charge"+token);
		}else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

}
