package com.symbol.search.dao;

import org.apache.solr.client.solrj.SolrQuery;

import com.symbol.search.pojo.SearchResult;

public interface SearchDao {

	SearchResult search(SolrQuery query) throws Exception;
}
