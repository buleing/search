/*package com.symbol.search;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;

public class SolrRecordHandler implements Runnable {
	// 生产者-消费者 solr doc
	private ArrayBlockingQueue<SolrInputDocument> docs = new ArrayBlockingQueue<SolrInputDocument>(5000);

	public void wrap(ResultSet rs) {
		SolrInputDocument doc = new SolrInputDocument();
		try {
			ResultSetMetaData rsm = rs.getMetaData();
			int numColumns = rsm.getColumnCount();
			for (int i = 1; i < (numColumns + 1); i++) {
				doc.addField(rsm.getColumnName(i), rs.getObject(i));
			}

		} catch (Exception e) {
			e.printStackTrace();
			//return null;
		}
		docs.add(doc);
	}

	@Override
	public void run() {
		//logger.info("solr 写线程启动开始。。。。");
		SolrCore core = cores.getCore("review");
		UpdateRequestProcessorChain chain = core.getUpdateProcessingChain(null);
		SolrParams param = new ModifiableSolrParams();
		SolrQueryRequestBase req = new SolrQueryRequestBase(core, param) {
		};
		SolrQueryResponse rsp = new SolrQueryResponse();
		UpdateRequestProcessor processor = chain.createProcessor(req, rsp);
		// 不停地从队列中读取元素，直到任务结束
		SolrInputDocument doc;
		AddUpdateCommand acmd = new AddUpdateCommand(req);
		while (true) {
			try {
				doc = docs.take();
				// 读取到一个空的doc，则表明任务结束
				if (doc.isEmpty()) {
					break;
				}
				acmd.solrDoc = doc;
				processor.processAdd(acmd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//logger.info("solr index thread finished!");
		// 任务完成，则提交
		try {
			CommitUpdateCommand cmd = new CommitUpdateCommand(req, false);
			processor.processCommit(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				processor.finish();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		isfinished.set(true);
	}
}
*/