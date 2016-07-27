package com.symbol.search.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.symbol.common.pojo.ReturnResult;
import com.symbol.common.utils.ExceptionUtil;
import com.symbol.common.utils.Reflections;
import com.symbol.search.mapper.ItemMapper;
import com.symbol.search.pojo.Item;
import com.symbol.search.service.SolrService;

@Service
public class ItemServiceImpl implements SolrService<Item> {

	@Autowired
	private ItemMapper itemMapper;
	
	@Autowired
	private SolrClient solrClient;
	
	@Override
	public ReturnResult initIndexs() {
		try {
			
			//查询商品列表
			List<Item> list = itemMapper.getItemList();
			//把商品信息写入索引库
			for (Item item : list) {
				//创建一个SolrInputDocument对象
				SolrInputDocument document = new SolrInputDocument();
				document.setField("id", item.getId());
				document.setField("item_title", item.getTitle());
				document.setField("item_sell_point", item.getSell_point());
				document.setField("item_price", item.getPrice());
				document.setField("item_image", item.getImage());
				document.setField("item_category_name", item.getCategory_name());
				document.setField("item_description", item.getDescription());
				//写入索引库
				solrClient.add(document);
			}
			//提交修改
			solrClient.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok();
	}

	/**
	 * 更新或者添加单个索引，
	 */
	@Override
	public ReturnResult saveOrUpdate(Item item) {
		try {
			//创建一个SolrInputDocument对象
			SolrInputDocument document = new SolrInputDocument();
			document.setField("id", item.getId());
			document.setField("item_title", item.getTitle());
			document.setField("item_sell_point", item.getSell_point());
			document.setField("item_price", item.getPrice());
			document.setField("item_image", item.getImage());
			document.setField("item_category_name", item.getCategory_name());
			document.setField("item_description", item.getDescription());
			//写入索引库
			solrClient.add(document);
			//提交修改
			solrClient.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok();
	}


	@Override
	public ReturnResult saveOrUpdate(List<Item> list) {
		try {
			//把商品信息写入索引库
			for (Item item : list) {
				//创建一个SolrInputDocument对象
				SolrInputDocument document = new SolrInputDocument();
				document.setField("id", item.getId());
				document.setField("item_title", item.getTitle());
				document.setField("item_sell_point", item.getSell_point());
				document.setField("item_price", item.getPrice());
				document.setField("item_image", item.getImage());
				document.setField("item_category_name", item.getCategory_name());
				document.setField("item_description", item.getDescription());
				//写入索引库
				solrClient.add(document);
			} 
			//提交修改
			solrClient.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok();
	}

	@Override
	public ReturnResult deleteIndexbyId(String id) {
		try {
			solrClient.deleteById(id);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok();
	}

	@Override
	public ReturnResult deleteIndexbyId(List<String> ids) {
		try {
			solrClient.deleteById(ids);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return ReturnResult.ok();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReturnResult deleteIndex(List<Item> list) {
		List<String> ids = Collections.EMPTY_LIST;
		try {
			ids = Reflections.fetchElementPropertyToList(list,"id");
			
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return deleteIndexbyId(ids);
	}

	@Override
	public ReturnResult deleteIndex(Item bean) {
		String id = bean.getId();
		return deleteIndexbyId(id);
	}
	
	

}
