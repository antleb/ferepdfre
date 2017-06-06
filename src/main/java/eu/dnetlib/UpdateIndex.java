package eu.dnetlib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import eu.openminted.content.index.IndexConfiguration;
import eu.openminted.content.index.IndexPublication;
import eu.openminted.content.index.entities.Publication;
import eu.openminted.content.index.entities.utils.ExtensionResolver;
import eu.openminted.omtdcache.CacheDataIDSHA1;

/**
 * Updates the data stored in json format, and reloades them to index
 * @author gkirtzou
 *
 */
public class UpdateIndex {

	private static final Logger log = LoggerFactory.getLogger(UpdateIndex.class);
	
	public static void main(String[] args) throws IOException, Exception {
			
    	// Connect to the Index 
		IndexConfiguration indexConfig =  IndexConfiguration.getInstance();
		IndexPublication index = new IndexPublication(indexConfig);
        	              
        String indexName = indexConfig.getIndex();
        String documentType = indexConfig.getDocumentType();
        String pathToFiles = UpdateIndex.getPathToFiles();
        String urlDomain = UpdateIndex.getDomainURL();
        CacheDataIDSHA1 sha1Calc = new CacheDataIDSHA1();
        log.info("Connect to index <" + indexName + "> with documents <" + documentType + ">");
        
        // Read json files
        log.info("Read documents from json files");
        int counter = 0;
        ObjectMapper mapper = new ObjectMapper();
        Path p = FileSystems.getDefault().getPath(pathToFiles + "metadata/");
        Stream<Path> walk = Files.walk(p);     
        Iterator<Path> iterPath = walk.iterator();
        
        while(iterPath.hasNext()) {
        		Path filePath = iterPath.next();
		        if (Files.isRegularFile(filePath)) {
		            //Do something with filePath
			        log.info("Loading publication from json file " + filePath);
			        File workingFile = filePath.toFile();
			        Publication pub;
			        pub = mapper.readValue(workingFile, Publication.class);
						
					String pubID = pub.getOpenaireId();
					log.info("Loaded publication :: " + pub.toString());
										
					
					// Recalculate hash value
					File fileOld = new File(pub.getPathToFile());				
					String hashValueSHA1;
						
					hashValueSHA1 = sha1Calc.getID(FileUtils.readFileToByteArray(fileOld));
					pub.setHashValue(hashValueSHA1);
					
					
					
					// Optimize layout in directories
					// Create store folder(s)
					String storePrefix = pubID.substring(0, pubID.lastIndexOf("::"));
					String id = pubID.substring(pubID.lastIndexOf("::")+2); 
					File storeFolder = new File(pathToFiles + storePrefix + "/" + id.substring(0, 3));
					if (!storeFolder.exists()) {
						storeFolder.mkdirs();
					}
					// Move to new store folder
					String extension = ExtensionResolver.getExtension(pub.getMimeType());
						
					String filenameNew = storeFolder.getPath() + "/" + pubID + extension;
					File fileNew = new File(filenameNew);
					try {			
						FileUtils.copyFile(fileOld, fileNew);
						fileOld.delete();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// Update new paths
					pub.setPathToFile(fileNew.getCanonicalPath());
					pub.setUrl(urlDomain + filenameNew);
					
					log.info("Updated publication ::" + pub.toString());
					// Update publication in index
					boolean success = index.addPublication(pub, pubID); 
					log.info("Succeded? " + success);
					assert(success);
						
					// Export Publication in json and save to disk
					Gson gson = new Gson();
					FileWriter fw = new FileWriter(pathToFiles + "metadata_new/" + pubID + ".json");
					gson.toJson(pub, fw);
					fw.close();
					counter++;
					    
				
			        
		        }
		}
        
        /*
		
		File folder = new File(pathToFiles + "metadata/");
		File[] listOfFiles = folder.listFiles();

	
		
		//listOfFiles.length
		for (int i = 0; i < listOfFiles.length; i++) {
			File workingFile = listOfFiles[i];
			if (workingFile.isFile()) {
				
				// Create publication from JSON from file
				log.info("Loading publication from json file " + workingFile.getName());				
				Publication pub = mapper.readValue(workingFile, Publication.class);
				String pubID = pub.getOpenaireId();
				log.info("Loaded publication :: " + pub.toString());
								
				
				// Recalculate hash value
				File fileOld = new File(pub.getPathToFile());				
				String hashValueSHA1 = sha1Calc.getID(FileUtils.readFileToByteArray(fileOld));
				pub.setHashValue(hashValueSHA1);
				
				// Optimize layout in directories
				// Create store folder(s)
				String storePrefix = pubID.substring(0, pubID.lastIndexOf("::"));
				String id = pubID.substring(pubID.lastIndexOf("::")+2); 
				File storeFolder = new File(pathToFiles + storePrefix + "/" + id.substring(0, 3));
				if (!storeFolder.exists()) {
					storeFolder.mkdirs();
				}
				// Move to new store folder
				String extension = ExtensionResolver.getExtension(pub.getMimeType());
				
				String filenameNew = storeFolder.getPath() + "/" + pubID + extension;
				File fileNew = new File(filenameNew);
				try {			
				    FileUtils.copyFile(fileOld, fileNew);
				    fileOld.delete();
				} catch (IOException e) {
				    e.printStackTrace();
				}
				// Update new paths
				pub.setPathToFile(fileNew.getCanonicalPath());
				pub.setUrl(urlDomain + filenameNew);
							
				log.info("Updated publication ::" + pub.toString());
				// Update publication in index
				boolean success = index.addPublication(pub, pubID); 
				log.info("Succeded? " + success);
				assert(success);
				
				// Export Publication in json and save to disk
			    Gson gson = new Gson();
			    FileWriter fw = new FileWriter(pathToFiles + "metadata_new/" + pubID + ".json");
			    gson.toJson(pub, fw);
			    fw.close();
			    counter++;
				
			}											
		}
        */
		log.info("Updated " + counter + " documents.");
        index.disconnect();
	}
	
	private static String getDomainURL() throws IOException {
         
         Properties configFile = new Properties();
         configFile.load(UpdateIndex.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
		
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
         configFile.load(UpdateIndex.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
         
         String pathToFiles = configFile.getProperty("pathToFiles");
         if (pathToFiles != null) {
              pathToFiles = pathToFiles.trim();
         } else {
	    	pathToFiles = "/media/pdfs/";
         }	   

         return pathToFiles;
    }
}
