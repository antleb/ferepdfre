package eu.dnetlib.elasticsearch;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		final String indexES = esConfig.getIndex();
		final String documentType = esConfig.getDocumentType();

		ExecutorService service = Executors.newFixedThreadPool(2);
		
		// Add publications
		final PublicationGenerator pubGenerator = new PublicationGenerator(pathToPdf, 9);
		for (int i = 0; i < 10; i++) {
			service.submit(new Runnable() {
            //       @Override
                   public void run() {
                	   try {
	                	   System.out.println(Thread.currentThread().getName());
	                	   Publication doc = pubGenerator.generatePublication();
	                	   String id = doc.getOpenaireId();
	       	                	
	                	   Index index = new Index.Builder(doc).index(indexES).type(documentType).id(id).build();
	                	   client.executeAsync(index, new MyJestResultHandler());	                	
	                   }
                	   catch(IOException e) {
                           e.printStackTrace();
                       } 
                   }
			});			
		}
		
		service.shutdown();
		client.shutdownClient();
		
	}
	
}
