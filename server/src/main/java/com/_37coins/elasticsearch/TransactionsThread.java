package com._37coins.elasticsearch;

import java.util.Date;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.workflow.pojo.DataSet;

public class TransactionsThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(TransactionsThread.class);
	final Cache cache;
	final String hashKey;
	final String cn;
	final Client client;

	public TransactionsThread(Client elasticSearch, Cache cache, String hashKey, String cn) {
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
					DataSet.Action.WITHDRAWAL_REQ.toString());
			// date range query
			long DAY_IN_MS = 1000 * 60 * 60 * 24;
			RangeQueryBuilder dateRangeQuery = QueryBuilders
					.rangeQuery("histogram_time")
					.from(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS))
							.getTime()).to(new Date().getTime());
			// put them all together
			BoolQueryBuilder bq = QueryBuilders.boolQuery().must(hostQuery)
					.must(eventQuery).must(dateRangeQuery);
			CountResponse response = client.prepareCount("graylog2_*")
					.setQuery(bq).execute().actionGet();
			long txCount = response.count();
			cache.put(new Element(hashKey + cn, txCount));
		} catch (Exception e) {
			log.error("transactions thread failed", e);
			e.printStackTrace();
		}
	}

}
