package com.symbol.search;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
@SuppressWarnings("resource")
public class SolrCloudTest {
	@Test
	public void testAddDocument1() throws Exception {
		
		SolrClient solrCient = new HttpSolrClient("http://127.0.0.1:8080/solr");
		// 创建一个文档对象
		SolrInputDocument document = new SolrInputDocument();
		// 向文档中添加域
		document.addField("id", "test001");
		document.addField("item_title", "测试商品");
		// 把文档添加到索引库
		solrCient.add(document);
		// 提交
		solrCient.commit();
	}
	
	@Test
	public void deleteDocument1() throws SolrServerException, IOException {
		// 创建一个和solr集群的连接
		// 参数就是zookeeper的地址列表，使用逗号分隔
		SolrClient solrServer = new HttpSolrClient("http://127.0.0.1:8080/solr");

		solrServer.deleteByQuery("*:*");
		solrServer.commit();
	}

	@Test
	public void testAddDocument() throws Exception {
		// 创建一个和solr集群的连接
		// 参数就是zookeeper的地址列表，使用逗号分隔
		String zkHost = "192.168.25.154:2181,192.168.25.154:2182,192.168.25.154:2183";
		CloudSolrClient solrServer = new CloudSolrClient(zkHost);
		// 设置默认的collection
		solrServer.setDefaultCollection("collection2");
		// 创建一个文档对象
		SolrInputDocument document = new SolrInputDocument();
		// 向文档中添加域
		document.addField("id", "test001");
		document.addField("item_title", "测试商品");
		// 把文档添加到索引库
		solrServer.add(document);
		// 提交
		solrServer.commit();
	}

	@Test
	public void deleteDocument() throws SolrServerException, IOException {
		// 创建一个和solr集群的连接
		// 参数就是zookeeper的地址列表，使用逗号分隔
		String zkHost = "192.168.25.154:2181,192.168.25.154:2182,192.168.25.154:2183";
		CloudSolrClient solrServer = new CloudSolrClient(zkHost);
		// 设置默认的collection
		solrServer.setDefaultCollection("collection2");

		solrServer.deleteByQuery("*:*");
		solrServer.commit();
	}
}
