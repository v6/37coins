package com._37coins.resources;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.RandomStringUtils;

import com._37coins.web.Product;

@Path(ProductResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {
	public final static String PATH = "/product";

	private final Cache cache;
	
	@Inject
	public ProductResource(Cache cache){
		this.cache = cache;
	}
	
	@POST
	public Product create(Product charge){
		if (null == charge || null == charge.getAmount()||null == charge.getSource()){
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		String token = null;
		int i = 2;
		while (null==token){
			token = RandomStringUtils.random(i, "abcdefghijkmnopqrstuvwxyz123456789");
			//check collision, if yes, increase length
			if (null==cache.get("charge"+token)){
				break;
			}
			i++;
		}
		cache.put(new Element("charge"+token,charge));
		return new Product().setToken(token);
	}
	
	@GET
	public Product get(@QueryParam("token") String token){
		Element e = cache.get("charge"+token);
		if (null!=e){
			return (Product)e.getObjectValue();
		}else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
	
	@DELETE
	public void delete(@QueryParam("token") String token){
		Element e = cache.get("charge"+token);
		if (null!=e){
			cache.remove("charge"+token);
		}else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

}
