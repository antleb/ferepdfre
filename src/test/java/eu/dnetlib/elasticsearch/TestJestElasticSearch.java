package eu.dnetlib.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.RootObjectMapper;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.mapping.PutMapping;

import eu.dnetlib.*;
import eu.dnetlib.elasticsearch.entities.Publication;
import eu.dnetlib.elasticsearch.entities.PublicationGenerator;
import eu.openminted.omtdcache.CacheDataIDMD5;

public class TestJestElasticSearch {
	public static void main(String[] args) throws IOException {
		// Create a Jest Client and connect to Elastic search
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder("http://localhost:9200")
		                        .multiThreaded(false)		                      
		                        .build());
		 JestClient client = factory.getObject();
		 
		/* PutMapping putMapping = new PutMapping.Builder(
			        "my_index",
			        "my_type",
			        "{ \"my_type\" : { \"properties\" : { \"message\" : {\"type\" : \"string\", \"store\" : \"yes\"} } } }"
			).build();
		 client.execute(putMapping); */
		 
		 /*
		 // Create a map 
		 RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder("my_mapping_name").add(
			        new StringFieldMapper.Builder("message").store(true)
				 );
		 System.out.println(rootObjectMapperBuilder.toString());
		 DocumentMapper documentMapper = new DocumentMapper.Builder(rootObjectMapperBuilder, null).build(null);
		 String expectedMappingSource = documentMapper.mappingSource().toString();
		 System.out.println(expectedMappingSource);
		 */
		 /*
		ObjectMapper mapper = new ObjectMapper();
		// configure mapper, if necessary, then create schema generator
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
		com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaGen.generateSchema(Pet.class);
		mapper.
		
	    System.out.println(schema.toString());
	    */
		/* 
		 // Get a document by id
		 Get get = new Get.Builder("openaire", "AVuK-stEM4esZmzfz7dn").type("docMeta").build();
		 JestResult resultJest = client.execute(get);
		 System.out.println(resultJest.getJsonString());
		 
		 // Search documents
		 SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		 searchSourceBuilder.query(QueryBuilders.matchQuery("mimeType", "mimeType2"));

		 Search search = new Search.Builder(searchSourceBuilder.toString())
		                                 // multiple index or types can be added.
		                                 .addIndex("openaire")
		                                 .addType("docMeta")
		                                 .build();

		 SearchResult resultSearch = client.execute(search);
		 System.out.println(resultSearch.getJsonString());
		 */
		 // Add document
		 /*
		 String doc = jsonBuilder()
				    .startObject()
				    .field("openID", "3")
				    .field("hash", "thisIsAnotherHash")
				    .field("mimeType", "mimeType2")
				    .field("pathToFile", "/path/To/The/File2")
				    .endObject().string();
		 */
		
		 
	}

}
