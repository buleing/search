package com.symbol.search;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;  
  
/** 
 * Solr server for indexes operations. 
 *  
 */  
public class SolrPostServer {  
  
    private static final Logger LOG = Logger.getLogger(SolrPostServer.class);  
    private HttpSolrClient client;   
    private ResponseParser responseParser;  
      
    private MongoConfig mongoConfig;  
    private String[] collectionNames;  
    private  int maxCommitCount = 100;  
    private boolean manualOptimize = true;  
  
    private boolean manualCommit = false;  
    private Collection<SolrInputDocument> docContainer = new ArrayList<SolrInputDocument>();  
    private static int totalCount = 0;  
      
    public SolrPostServer(String url, HttpClient httpClient, MongoConfig mongoConfig) throws MalformedURLException {  
        if(httpClient==null) {  
		    client = new HttpSolrClient(url);  
		    client.setSoTimeout(500000);  // socket read timeout  
		    client.setConnectionTimeout(5000);    
		    client.setDefaultMaxConnectionsPerHost(10);    
		    client.setMaxTotalConnections(100);  
		    client.setAllowCompression(true);    
		    //client.setMaxRetries(1); // defaults to 0.  > 1 not recommended.   
		} else {  
		    client = new HttpSolrClient(url, httpClient);  
		}  
        this.mongoConfig = mongoConfig;  
        initialize();  
    }  
  
    /** 
     * Initialize the {@link CommonsHttpSolrServer}'s basic parameters. 
     */  
    private void initialize() {  
        if(responseParser!=null) {  
            client.setParser(responseParser);  
        } else {  
            client.setParser(new XMLResponseParser());  
        }  
    }  
      
    @SuppressWarnings("unchecked")  
    public void postUpdate() {  
        DBCursor cursor = null;  
        try {  
            for (String c : collectionNames) {  
                LOG.info("MongoDB collection name: " + c);  
                DBCollection collection = MongoHelper.newHelper(mongoConfig).getCollection(c);  
                DBObject q = new BasicDBObject();  
                cursor = collection.find(q);  
                while(cursor.hasNext()) {  
                    try {  
                        Map<Object, Object> m = cursor.next().toMap();  
                        if(manualCommit) {  
                            add(m, true);  
                        } else {  
                            add(m, false);  
                        }  
                        ++totalCount;  
                        LOG.info("Add fragment: _id = " + m.get("_id").toString());  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                cursor.close();  
            }  
            LOG.info("Add totalCount: " + totalCount);  
            finallyCommit();  
            optimize(manualOptimize);  
        } catch (MongoException e) {  
            e.printStackTrace();  
        } catch (SolrServerException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * Detele lucene {@link Document} by IDs. 
     * @param strings 
     */  
    public void deleteById(List<String> strings) {  
        try {  
            client.deleteById(strings);  
        } catch (SolrServerException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * Detele lucene {@link Document} by query. 
     * @param query 
     */  
    public void deleteByQuery(String query) {  
        try {  
            client.deleteByQuery(query);  
        } catch (SolrServerException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * Query. 
     * @param params 
     * @param fields 
     * @return 
     */  
    public List<Map<String, Object>> query(SolrParams params, String[] fields) {  
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();  
        try {  
            SolrDocumentList documents = client.query(params).getResults();  
            Iterator<SolrDocument> iter = documents.iterator();  
            while(iter.hasNext()) {  
                SolrDocument doc = iter.next();  
                Map<String, Object> map = new HashMap<String, Object>();  
                for(String field : fields) {  
                    map.put(field, doc.getFieldValue(field));  
                }  
                results.add(map);  
            }  
        } catch (SolrServerException | IOException e) {  
            e.printStackTrace();  
        }  
        return results;  
    }  
      
    /** 
     * When controlling the committing action at client side, finally execute committing. 
     * @throws SolrServerException 
     * @throws IOException 
     */  
    private void finallyCommit() throws SolrServerException, IOException {  
        if(!docContainer.isEmpty()) {  
            client.add(docContainer);  
            commit(false, false);  
        }  
    }  
      
    /** 
     * Commit. 
     * @param waitFlush 
     * @param waitSearcher 
     * @throws SolrServerException 
     * @throws IOException 
     */  
    public void commit(boolean waitFlush, boolean waitSearcher) {  
        try {  
            client.commit(waitFlush, waitSearcher);  
        } catch (SolrServerException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * When controlling the optimizing action at client side, finally execute optimizing. 
     * @param waitFlush 
     * @param waitSearcher 
     * @throws SolrServerException 
     * @throws IOException 
     */  
    public void optimize(boolean waitFlush, boolean waitSearcher) {  
        try {  
            client.optimize(waitFlush, waitSearcher);  
            commit(waitFlush, waitSearcher);  
        } catch (Exception e) {  
            LOG.error("Encounter error when optimizing.",  e);  
            try {  
                client.rollback();  
            } catch (SolrServerException e1) {  
                e1.printStackTrace();  
            } catch (IOException e1) {  
                e1.printStackTrace();  
            }  
        }  
    }  
      
    /** 
     * Optimize. 
     * @param optimize 
     * @throws SolrServerException 
     * @throws IOException 
     */  
    private void optimize(boolean optimize) {  
        if(optimize) {  
            optimize(true, true);  
        }  
    }  
  
    /** 
     * Add a {@link SolrInputDocument} or collect object and add to the a collection for batch updating 
     * from a mongodb's recored, a Map object. 
     * @param m 
     * @param oneByOne 
     * @throws SolrServerException 
     * @throws IOException 
     */  
    private void add(Map<Object, Object> m, boolean oneByOne) throws SolrServerException, IOException {  
        SolrInputDocument doc = createDocument(m);  
        if(oneByOne) {  
            client.add(doc);  
        } else {  
            docContainer.add(doc);  
            if(docContainer.size()>maxCommitCount) {  
                client.add(docContainer);  
                client.commit(false, false);  
                docContainer = new ArrayList<SolrInputDocument>();  
            }  
        }  
    }  
      
    /** 
     * Create a {@link SolrInputDocument} object. 
     * @param record 
     * @return 
     */  
    private SolrInputDocument createDocument(Map<Object, Object> record) {  
        String id = record.get("_id").toString();  
        String articleId = (String) record.get("articleId");  
        String title = (String) record.get("title");  
        String url = (String) record.get("url");  
        String spiderName = (String) record.get("spiderName");  
        String fragment = makeFragment((BasicDBObject) record.get("fragment"));  
        String word = (String) record.get("word");  
        int pictureCount = (Integer) record.get("pictureCount");  
        int selectedCount = (Integer) record.get("selectedCount");  
        int fragmentSize = (Integer) record.get("fragmentSize");  
          
        SolrInputDocument doc = new SolrInputDocument();  
        doc.addField("_id", id, 1.0f);  
        doc.addField("articleId", articleId, 1.0f);  
        doc.addField("title", title, 1.0f);  
        doc.addField("url", url, 1.0f);  
        doc.addField("spiderName", spiderName, 1.0f);  
        doc.addField("fragment", fragment, 1.0f);  
        doc.addField("word", word, 1.0f);  
        // Additional processing for lucene payload metadata.  
        doc.addField("pictureCount", word + "|" + pictureCount);  
        doc.addField("coverage", word + "|" + (float)selectedCount/fragmentSize);  
        return doc;  
    }  
      
    @SuppressWarnings("unchecked")  
    private String makeFragment(BasicDBObject fragment) {  
        StringBuilder builder = new StringBuilder();  
        Iterator<Map.Entry<Integer, String>> iter = fragment.toMap().entrySet().iterator();  
        while(iter.hasNext()) {  
            Map.Entry<Integer, String> entry = iter.next();  
            builder.append(entry.getValue().trim()).append("<br>");  
        }  
        return builder.toString();  
    }  
      
    /** 
     * Set {@link ResponseParser}, default value is {@link XMLResponseParser}. 
     * @param responseParser 
     */  
    public void setResponseParser(ResponseParser responseParser) {  
        this.responseParser = responseParser;  
    }  
  
    /** 
     * Pulling document resource from multiple collections of MongoDB. 
     * @param collectionNames 
     */  
    public void setCollectionNames(String[] collectionNames) {  
        this.collectionNames = collectionNames;  
    }  
      
    public void setMaxCommitCount(int maxCommitCount) {  
        this.maxCommitCount = maxCommitCount;  
    }  
  
    public void setManualCommit(boolean manualCommit) {  
        this.manualCommit = manualCommit;  
    }  
  
    public void setManualOptimize(boolean manualOptimize) {  
        this.manualOptimize = manualOptimize;  
    }  
  
    /** 
     * Mongo database configuration. 
     *  
     * @author shirdrn 
     * @date   2011-12-20 
     */  
    public static class MongoConfig implements Serializable {  
        private static final long serialVersionUID = -3028092758346115702L;  
        private String host;  
        private int port;  
        private String dbname;  
        private String collectionName;  
        public MongoConfig(String host, int port, String dbname, String collectionName) {  
            super();  
            this.host = host;  
            this.port = port;  
            this.dbname = dbname;  
            this.collectionName = collectionName;  
        }  
        @Override  
        public boolean equals(Object obj) {  
            MongoConfig other = (MongoConfig) obj;  
            return host.equals(other.host) && port==other.port  
                && dbname.equals(other.dbname) && collectionName.equals(other.collectionName);  
        }  
    }  
      
    /** 
     * Mongo database utility. 
     *  
     * @author shirdrn 
     * @date   2011-12-20 
     */  
    static class MongoHelper {  
        private static Mongo mongo;  
        private static MongoHelper helper;  
        private MongoConfig mongoConfig;  
        private MongoHelper(MongoConfig mongoConfig) {  
            super();  
            this.mongoConfig = mongoConfig;  
        }  
        public synchronized static MongoHelper newHelper(MongoConfig mongoConfig) {  
            try {  
                if(helper==null) {  
                    helper = new MongoHelper(mongoConfig);  
                    //mongo = new Mongo(mongoConfig.host, mongoConfig.port);  
                    mongo = new MongoClient(mongoConfig.host, mongoConfig.port);
                    Runtime.getRuntime().addShutdownHook(new Thread() {  
                        @Override  
                        public void run() {  
                            if(mongo!=null) {  
                                mongo.close();  
                            }  
                        }  
                    });  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            return helper;  
        }             
        @SuppressWarnings("deprecation")
		public DBCollection getCollection(String collectionName) {  
            DBCollection c = null;  
            try {  
                c = mongo.getDB(mongoConfig.dbname).getCollection(collectionName);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            return c;  
        }     
    }  
} 