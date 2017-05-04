package eu.dnetlib;

import com.google.gson.Gson;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreFile;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreService;
import eu.dnetlib.data.objectstore.rmi.ObjectStoreServiceException;
import eu.dnetlib.domain.EPR;
import eu.dnetlib.elasticsearch.ElasticSearchConfiguration;
import eu.dnetlib.elasticsearch.ElasticSearchConnection;
import eu.dnetlib.elasticsearch.MyJestResultHandler;
import eu.dnetlib.elasticsearch.entities.Publication;
import eu.dnetlib.enabling.is.lookup.rmi.ISLookUpException;
import eu.dnetlib.enabling.resultset.rmi.ResultSetException;
import eu.dnetlib.enabling.resultset.rmi.ResultSetService;
import eu.dnetlib.utils.EPRUtils;
import eu.dnetlib.utils.ExtensionResolver;
import eu.openminted.omtdcache.CacheDataIDMD5;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created by antleb on 11/8/14.
 */
public class FerePdfRe {

    public static void main(String[] args) throws ISLookUpException, ObjectStoreServiceException, ResultSetException, NoSuchAlgorithmException, KeyManagementException, IOException, Exception {
    	// Connect to the Elastic Search via Jest Client
    	ElasticSearchConfiguration esConfig =  ElasticSearchConfiguration.getInstance();
        ElasticSearchConnection configES = new ElasticSearchConnection(esConfig.getHost(), esConfig.getPort());
        final JestClient client = configES.client();
	
                
        final String indexES = esConfig.getIndex();
        final String documentType = esConfig.getDocumentType();
        final String pathToFiles = FerePdfRe.getPathToFiles();
        final String urlDomain = FerePdfRe.getDomainURL();
        final CacheDataIDMD5 md5Calculator = new CacheDataIDMD5();
    	  
        // Multithread
        ExecutorService service = Executors.newFixedThreadPool(Integer.parseInt(args[0]));

       	// Openaire services
        String objectStoreAddress = "http://services.openaire.eu:8280/is/services/objectStore";
        String rsAddress = "http://services.openaire.eu:8280/is/services/resultSet";

        ObjectStoreService storeService;
        ResultSetService rsService;

        Gson gson = new Gson();

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ObjectStoreService.class);
        factory.setAddress(objectStoreAddress);
        storeService = (ObjectStoreService) factory.create();

        factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ResultSetService.class);
        factory.setAddress(rsAddress);
        rsService = (ResultSetService) factory.create();
       

        List<String> stores = storeService.getListOfObjectStores();

        for (String store : stores) {
            W3CEndpointReference w3cEpr = storeService.deliverObjects(store, 0L, new Date().getTime());


            EPR epr = EPRUtils.createEPR(w3cEpr);
            String rsId = epr.getParameter("ResourceIdentifier");
            int count = rsService.getNumberOfElements(rsId);

            for (int i = 0; i < count; i += 100) {
                List<String> objects = null;
                int counter =0;

                while (objects == null && counter < 5) {
                    try {
                        objects = rsService.getResult(rsId, i, i + 100, "waiting");
                    } catch (Exception e) {
                        counter++;
                    }
                }

                if (objects == null)
                    continue;



                for (int j = 0; j < objects.size(); j++) {
                    final ObjectStoreFile md = gson.fromJson(objects.get(j), ObjectStoreFile.class);

                    if (md.getFileSizeKB() < 20000) {                         
                           service.submit(new Runnable() {
                            @Override
                            public void run() {
                                                     	
                                String filename = md.getObjectID().substring(0, md.getObjectID().lastIndexOf("::"));
                                String metadataFilename = pathToFiles + "metadata/" + filename +".json";

                                if (!new File(metadataFilename).exists()) {
                                    String extension = ExtensionResolver.getExtension(md.getMimeType());
//                                String url = md.getURI().replace("http://services.openaire.eu:8280", "http://localhost:8888");
                                    String url = md.getURI();
                                    System.out.println(Thread.currentThread().getName() + " - " + filename);
                                    FileOutputStream fos = null;
                                    try {
                                        // Get publication file
                                        fos = new FileOutputStream(pathToFiles + filename + extension);
                                        IOUtils.copyLarge(new URL(url).openStream(), fos);
                                        fos.close();

                                        // Create Publication document for Elastic Search
                                        Publication pub = new Publication();
                                        // openaireId
                                        pub.setOpenaireId(filename);
                                        // mimeType
                                        pub.setMimeType(md.getMimeType());
                                        // path to file
                                        String pathToFile = pathToFiles + filename + extension;
                                        pub.setPathToFile(pathToFile);
                                        // hash value
                                        byte[] file = FileUtils.readFileToByteArray(new File(pathToFile));
                                        String hashValue = md5Calculator.getID(file);
                                        pub.setHashValue(hashValue);
                                        // URL to file
                                        pub.setUrl(urlDomain + filename + extension);
                                        System.out.println(Thread.currentThread().getName() + " " + pub);

                                        // Add publication to Elastic Search index
                                        Index index = new Index.Builder(pub).index(indexES).type(documentType).id(filename).build();
                                        client.executeAsync(index, new MyJestResultHandler());

                                        // Export Publication in json and save to disk
                                        Gson gson = new Gson();
                                        FileWriter fw = new FileWriter(pathToFiles + "metadata/" + filename + ".json");
                                        gson.toJson(pub, fw);
                                        fw.close();


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        IOUtils.closeQuietly(fos);
                                    }
                                }
                            }
                        });
                    }
                }
            }

        }
     	service.shutdown();
     	client.shutdownClient();
    }
    
    private static String getDomainURL() throws IOException {
         
         Properties configFile = new Properties();
         configFile.load(FerePdfRe.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
		
         String domainURL = configFile.getProperty("domainURL");
         if (domainURL != null) {
              domainURL = domainURL.trim();
         } else {
	    	domainURL = "http://localhost/";
         }	   
         return domainURL;
    }
    
    private static String getPathToFiles() throws IOException {
         Properties configFile = new Properties();
         configFile.load(ElasticSearchConfiguration.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
         
         String pathToFiles = configFile.getProperty("pathToFiles");
         if (pathToFiles != null) {
              pathToFiles = pathToFiles.trim();
         } else {
	    	pathToFiles = "/media/pdfs";
         }	   

         return pathToFiles;
    }
}
