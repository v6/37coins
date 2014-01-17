package com._37coins.resources;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com._37coins.web.GatewayUser;

@Path(DataResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class DataResource {
	public final static String PATH = "/data";
	final private Cache cache;
	
	@Inject public DataResource(Cache cache) {
		this.cache = cache;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/gateways")
	public Set<GatewayUser> getGateways(){
		Element e = cache.get("gateways");
		if (null!=e && !e.isExpired()){
			Set<GatewayUser> gateways = (Set<GatewayUser>)e.getObjectValue();
			return gateways;
		}
		return new HashSet<GatewayUser>();
	}
}
