package eu.dnetlib.elasticsearch;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import eu.dnetlib.elasticsearch.entities.Publication;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

public class TestSearchElasticSearchOpenaire {

	public static void main(String[] args) throws IOException {
		String host = "http://localhost";
		String port = "9200";
		String indexES = "openaire";
		String documentType = "docMeta";

		ElasticSearchConfiguration configES = new ElasticSearchConfiguration(host, port);
		JestClient client = configES.client();
		
		// Search publications with Jest 		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("pathToFile", "/home/gkirtzou/Desktop/tmp/pdfs/3.pdf"));
		
		Search search = new Search.Builder(searchSourceBuilder.toString())
				// multiple index or types can be added.
				.addIndex(indexES)
				.addType(documentType)				
						.build();
		
		SearchResult resultSearch = client.execute(search);
		List<SearchResult.Hit<Publication, Void>> hits = resultSearch.getHits(Publication.class);
		System.out.println(hits.size());
		
		System.out.println(resultSearch.getTotal());
		
		Iterator<Hit<Publication, Void>> itPub = hits.iterator();	
		while (itPub.hasNext()) {
			Hit<Publication, Void> hit = itPub.next();
			System.out.println(hit.source);				
		}
		
		System.out.println(resultSearch.getJsonString());
		
		
	}

}
