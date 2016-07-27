package com.symbol.search.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.symbol.common.pojo.ReturnResult;
import com.symbol.search.pojo.Item;
import com.symbol.search.service.SolrService;

/**
 * 索引库维护
 */
@Controller
@RequestMapping("/index")
public class ItemController {
	
	@Autowired
	private SolrService<Item> itemService;

	/**
	 * 导入商品数据到索引库
	 */
	@RequestMapping({"/importall","/item/all"})
	@ResponseBody
	public ReturnResult importAllItems() {
		ReturnResult result = itemService.initIndexs();
		return result;
	}
	
	/**
	 * 一条商品数据到索引库
	 */
	@RequestMapping(value="/item", method=RequestMethod.POST)
	@ResponseBody
	public ReturnResult saveOrUpdate(Item bean) {
		ReturnResult result = itemService.saveOrUpdate(bean);
		return result;
	}
	
	/**
	 * 多条商品数据到索引库
	 */
	@RequestMapping(value="/items", method=RequestMethod.POST)
	@ResponseBody
	public ReturnResult saveOrUpdate(List<Item> list) {
		ReturnResult result = itemService.saveOrUpdate(list);
		return result;
	}
	
	/**
	 * 删除索引库中的某个商品的索引
	 */
	@RequestMapping(value="/item", method=RequestMethod.DELETE)
	@ResponseBody
	public ReturnResult deleteIndex(String id) {
		ReturnResult result = itemService.deleteIndexbyId(id);
		return result;
	}
	
	/**
	 * 删除索引库中的某个商品的索引
	 */
	@RequestMapping(value="/items", method=RequestMethod.DELETE)
	@ResponseBody
	public ReturnResult deleteIndex(List<String> ids) {
		ReturnResult result = itemService.deleteIndexbyId(ids);
		return result;
	}
	
}
