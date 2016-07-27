package com.symbol.search.service;

import java.util.List;

import com.symbol.common.pojo.ReturnResult;

public interface SolrService<T> {
	/**
	 * 初始化索引库
	 * 
	 * @return
	 */
	ReturnResult initIndexs();
	
	/**
	 * 新增或者更新一条索引
	 * @param bean
	 * @return
	 */
	ReturnResult saveOrUpdate(T bean);
	
	/**
	 * 新增或者更新索引
	 * @param list
	 * @return
	 */
	ReturnResult saveOrUpdate(List<T> list);
	
	
	ReturnResult deleteIndexbyId(String id);
	
	ReturnResult deleteIndexbyId(List<String> ids);
	
	ReturnResult deleteIndex(List<T> list);
	ReturnResult deleteIndex(T bean);
}
