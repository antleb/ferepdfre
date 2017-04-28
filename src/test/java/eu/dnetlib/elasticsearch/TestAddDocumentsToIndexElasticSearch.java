package eu.dnetlib.elasticsearch;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import eu.dnetlib.elasticsearch.entities.Publication;
import eu.dnetlib.elasticsearch.entities.PublicationGenerator;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;

public class TestAddDocumentsToIndexElasticSearch {

	public static void main(String[] args) throws Exception {
		
		ElasticSearchConfiguration esConfig = ElasticSearchConfiguration.getInstance();
		ElasticSearchConnection configES = new ElasticSearchConnection(esConfig.getHost(), esConfig.getPort());
		final JestClient client = configES.client();
		
		final String pathToPdf = "/home/gkirtzou/Desktop/tmp/pdfs/";
		final String urlDomain = "http://adonis.athenarc.gr/pdfs/";
		final String indexES = esConfig.getIndex();
		final String documentType = esConfig.getDocumentType();

		ExecutorService service = Executors.newFixedThreadPool(2);
		
		// Add publications
		final PublicationGenerator pubGenerator = new PublicationGenerator(pathToPdf, urlDomain, 9);
		for (int i = 0; i < 10; i++) {
			service.submit(new Runnable() {
            //       @Override
                   public void run() {
                	   try {
                		   // Create publication
	                	   System.out.println(Thread.currentThread().getName());
	                	   Publication doc = pubGenerator.generatePublication();
	                	   String id = doc.getOpenaireId();
	       	                	
	                	   // Add publication to Elastic Search
	                	   Index index = new Index.Builder(doc).index(indexES).type(documentType).id(id).build();
	                	   client.executeAsync(index, new MyJestResultHandler());
	                	   	                	 
	                	   // Export Publication in json and save to disk
	                	   Gson gson = new Gson();	                	  
	                	   FileWriter fw = new FileWriter(pathToPdf + "metadata/" + id +".json");
	                	   gson.toJson(doc,fw);
	                	   fw.close();

	                   }
                	   catch(IOException e) {
                           e.printStackTrace();
                       } 
                   }
			});			
		}
		
		service.shutdown();
		client.shutdownClient();
	/*	Gson gson = new Gson();
		Publication pub = gson.fromJson(new FileReader(pathToPdf + "metadata/53a1d962-5b3e-4bba-b8c1-0f425af53252.json"), Publication.class);
		System.out.println("Publication from json::" + pub.toString());
		*/
	}
	
}
