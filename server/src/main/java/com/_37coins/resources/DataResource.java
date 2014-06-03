package com._37coins.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.elasticsearch.AvailabilityThread;
import com._37coins.elasticsearch.TransactionsThread;
import com._37coins.web.GatewayUser;

@Path(DataResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class DataResource {
	public final static String PATH = "/data";
	public static Logger log = LoggerFactory.getLogger(DataResource.class);
	final private Cache cache;
	final private Client elasticSearch;
	
	@Inject public DataResource(Cache cache, Client elasticSearch) {
		this.cache = cache;
		this.elasticSearch = elasticSearch;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/gateways")
	public Set<GatewayUser> getGateways(){
		Element e = cache.get("gateways");
		if (null!=e && !e.isExpired()){
			Map<String,GatewayUser> gateways = (Map<String,GatewayUser>)e.getObjectValue();
			return new HashSet<GatewayUser>(gateways.values());
		}
		throw new WebApplicationException("cache miss", Response.Status.EXPECTATION_FAILED);
	}
	
	@GET
	@Path("/serviceQuality/{cn}")
	public Map<String,Object> getService(@PathParam("cn")String cn){
		Map<String,Object> rv = new HashMap<>();
		for (int i = 0;i<15;i++){
			Element avail = cache.get("availability"+cn);
			Element turn = cache.get("turnover"+cn);
			if (null==cache.get("availabilityReq"+cn)){
				Thread t1 = new AvailabilityThread(elasticSearch, cache, "availability", cn);
				t1.start();
				Thread t2 = new TransactionsThread(elasticSearch, cache, "turnover", cn);
				t2.start();
				cache.put(new Element("availabilityReq"+cn,true));
			}
			if (null==avail || null==turn){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					log.error("data resource exception",e1);
					e1.printStackTrace();
					throw new WebApplicationException(e1,Response.Status.INTERNAL_SERVER_ERROR);
				}
			}else{
				rv.put("cn", cn);
				rv.put("time", avail.getCreationTime());
				rv.put("availability", avail.getObjectValue());
				rv.put("transactionCount", turn.getObjectValue());
				return rv;
			}
		}
		throw new WebApplicationException("query time exceeded", Response.Status.EXPECTATION_FAILED);
	}
}
