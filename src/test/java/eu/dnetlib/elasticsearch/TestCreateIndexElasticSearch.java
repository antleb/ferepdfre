package eu.dnetlib.elasticsearch;

import java.io.IOException;

import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.RootObjectMapper;
import org.elasticsearch.index.mapper.StringFieldMapper;

import io.searchbox.client.JestClient;
import io.searchbox.indices.mapping.PutMapping;

public class TestCreateIndexElasticSearch {

	public static void main(String[] args) throws IOException, Exception {
	
		ElasticSearchConfiguration esConfig =  new ElasticSearchConfiguration();
		ElasticSearchConnection configES = new ElasticSearchConnection(esConfig.getHost(), esConfig.getPort());
		JestClient client = configES.client();
		
	
		String mapping =  "{ \"publication\" : { \"properties\" : { " +
							" \"openaireId\" : {\"type\" : \"string\", \"store\" : \"yes\"}, " +
							" \"hashValue\" : {\"type\" : \"string\", \"store\" : \"yes\"}, " +
							" \"mimeType\" : {\"type\" : \"string\", \"store\" : \"yes\"}, " +
							" \"pathToFile\" : {\"type\" : \"string\", \"store\" : \"yes\"} " +
							" } } }";
		PutMapping putMapping = new PutMapping.Builder(
		        esConfig.getIndex(),
		        esConfig.getDocumentType(),
		        mapping
		).build();
		
		System.out.println(putMapping.toString());
		System.out.println(mapping);
		client.execute(putMapping);
		client.shutdownClient();

	}

}
