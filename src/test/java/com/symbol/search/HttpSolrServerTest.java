package com.symbol.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest.ACTION;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.Test;

public class HttpSolrServerTest {
	private static String url = "http://192.168.31.150:8080/solr/core0";
	private static SolrClient solrClient = new HttpSolrClient(url);

	@Test
	public void createIndex() {
		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
		File dir = new File("F:/学习资料");
		try {
			if (dir.exists() && dir.isDirectory()) {

				for (File file : dir.listFiles()) {
					

					up.addFile(file, getContentType(file.getName()));
					up.setParam("literal.id", UUID.randomUUID().toString());
					up.setParam("uprefix", "my_");
					if(getContentType(file.getName()).contains("text")){
						up.setParam("fmap.content", "my_content");
					}
					up.setAction(ACTION.COMMIT, true, true);
					solrClient.request(up);
				}

			} else {
				up.addFile(dir, getContentType(dir.getName()));
				up.setParam("literal.id", UUID.randomUUID().toString());
				// up.setParam("uprefix", "my_");
				//up.setParam("fmap.content", "text");
				up.setAction(ACTION.COMMIT, true, true);
				solrClient.request(up);
			}
			solrClient.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getContentType(String fileName) {
		Path path = Paths.get(fileName);
		path = Paths.get(fileName);
		String contentType = null;
		try {
			contentType = Files.probeContentType(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentType;
	}

	@Test
	public void searchIndex() {

	}

	@Test
	public void deleteIndex() {
		try {
			UpdateResponse deleteByQuery = solrClient.deleteByQuery("*:*");
			System.out.println(deleteByQuery.getStatus());
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
