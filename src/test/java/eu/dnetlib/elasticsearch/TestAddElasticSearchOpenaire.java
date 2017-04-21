package eu.dnetlib.elasticsearch;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import eu.dnetlib.elasticsearch.entities.Publication;
import eu.dnetlib.elasticsearch.entities.PublicationGenerator;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;

public class TestAddElasticSearchOpenaire {

	public static void main(String[] args) throws IOException, NoSuchFieldException {
		
		final String host = "http://localhost";
		final String port = "9200";
		final String indexES = "openaire";
		final String documentType = "docMeta";
		final String pathToPdf = "/home/gkirtzou/Desktop/tmp/pdfs/";

		
		ElasticSearchConfiguration configES = new ElasticSearchConfiguration(host, port);
		final JestClient client = configES.client();

		ExecutorService service = Executors.newFixedThreadPool(2);
		
		// Add publications
		final PublicationGenerator pubGenerator = new PublicationGenerator(pathToPdf, 9);
		for (int i = 0; i < 10; i++) {
			service.submit(new Runnable() {
                   @Override
                   public void run() {
                	   try {
	                	   System.out.println(Thread.currentThread().getName());
	                	   Publication doc = pubGenerator.generatePublication();
	                	   String id = doc.getOpenaireId();
				
	                	   Index index = new Index.Builder(doc).index(indexES).type(documentType).id(id).build();
	                	   client.execute(index);
	                	   Get get = new Get.Builder(indexES, id).type(documentType).build();
	                	   DocumentResult resultJest = client.execute(get);
	                	   System.out.println(Thread.currentThread().getName() + "-->"+resultJest.getJsonString());
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
