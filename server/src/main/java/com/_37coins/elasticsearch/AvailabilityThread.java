package com._37coins.elasticsearch;

import java.util.Date;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;

public class AvailabilityThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(AvailabilityThread.class);
	final Cache cache;
	final String hashKey;
	final String cn;
	final Client client;

	public AvailabilityThread(Client elasticSearch, Cache cache, String hashKey, String cn) {
		this.cache = cache;
		this.hashKey = hashKey;
		this.cn = cn;
		this.client = elasticSearch;
	}

	@Override
	public void run() {
		try {
			// hostname query
			QueryBuilder hostQuery = QueryBuilders.matchQuery("_hostName", cn);
			// event type query
			QueryBuilder eventQuery = QueryBuilders.matchQuery("_event",
					"check");
			// date range query
			long DAY_IN_MS = 1000 * 60 * 60 * 24;
			RangeQueryBuilder dateRangeQuery = QueryBuilders
					.rangeQuery("histogram_time")
					.from(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS))
							.getTime()).to(new Date().getTime());
			// put them all together
			BoolQueryBuilder bq = QueryBuilders.boolQuery().must(hostQuery)
					.must(eventQuery).must(dateRangeQuery);
			TermsFacetBuilder tf = FacetBuilders.termsFacet("f")
					.field("_Online").size(2);
			SearchResponse response = client.prepareSearch("graylog2_*")
					.setQuery(bq).addFacet(tf).execute().actionGet();
			TermsFacet f = (TermsFacet) response.getFacets().facetsAsMap()
					.get("f");

			long total = f.getTotalCount();
			float online = 0.0f;
			for (TermsFacet.Entry entry : f) {
				if (entry.getTerm().equalsIgnoreCase("true")) {
					online = entry.getCount() / (float) total;
				}
			}
			cache.put(new Element(hashKey + cn, online));
		} catch (Exception e) {
			log.error("availability thread failed", e);
			e.printStackTrace();
		}
	}

}
