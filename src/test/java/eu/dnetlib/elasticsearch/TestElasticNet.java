package eu.dnetlib.elasticsearch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilders.*;


public class TestElasticNet {

	public static void main(String[] args) throws IOException {			
		// on startup		
		Settings settings = Settings.builder()
		        .put("cluster.name", "docker-cluster").build();
		
		TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(
                                    InetAddress.getByName("127.0.0.1"),
                                    9300
                            )
                    );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
	
      /*  
        // Admin index
        //IndicesAdminClient indicesAdminClient = client.admin().indices();       
        
        
		// Get 
		GetResponse getResponse = client.prepareGet("openaire", "docMeta", "AVtIbewjeuE7nKGUq6xv").get();
		System.out.println(getResponse.getSourceAsString());
		
		// Put
		XContentBuilder builder = jsonBuilder()
			    .startObject()
			        .field("openID", "2")
			        .field("hash", "thisIsAnotherHash")
			        .field("mimeType", "mimeType2")
			        .field("pathToFile", "/path/To/The/File2")
			    .endObject();
			  
		
		
		IndexResponse response = client.prepareIndex("openaire", "docMeta")
		        .setSource(builder.string())
		        .get();
		// Index name
		String _index = response.getIndex();
		// Type name
		String _type = response.getType();
		// Document ID (generated or not)
		String _id = response.getId();
		// Version (if it's the first time you index this document, you will get: 1)
		long _version = response.getVersion();
		// status has stored current instance statement.
		RestStatus status = response.status();
		System.out.println("index::" + _index );
		System.out.println("type::" + _type );
		System.out.println("id::" + _id );
		System.out.println("version::" + _version );
		System.out.println("status::" + status.toString() );
		
        */
		
		// Search
		SearchResponse responseSearch = client.prepareSearch("openaire")
		        .setTypes("docMeta")
		        //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        //.setQuery(QueryBuilders.termQuery("pathToFile", "/home/gkirtzou/Desktop/tmp/pdfs/3.pdf"))                 // Query		  		  
		        .get();
		
		System.out.println(responseSearch.toString());
		SearchHit[] hits = responseSearch.getHits().hits();
		
		
		// on shutdown
		client.close();

	}

}
