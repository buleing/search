package com.symbol.search.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.symbol.common.pojo.ReturnResult;
import com.symbol.common.utils.ExceptionUtil;
import com.symbol.search.pojo.SearchResult;
import com.symbol.search.service.SearchService;

/**
 * 商品查询Controller
 */
@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;
	
	@RequestMapping(value="/query", method=RequestMethod.GET)
	@ResponseBody
	public ReturnResult search(@RequestParam("q")String queryString, 
			@RequestParam(defaultValue="1")Integer page, 
			@RequestParam(defaultValue="20")Integer rows) {
		//查询条件不能为空
		if (StringUtils.isBlank(queryString)) {
			return ReturnResult.build(400, "查询条件不能为空");
		}
		SearchResult searchResult = null;
		try {
			queryString = new String(queryString.getBytes("iso8859-1"), "utf-8");
			searchResult = searchService.search(queryString, page, rows);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok(searchResult);
		
	}
	
}
