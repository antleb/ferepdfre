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
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
public class UpdateIndex2 {

	private static final Logger log = LoggerFactory.getLogger(UpdateIndex2.class);
	
	
	public static void main(String[] args) throws IOException, Exception {
			
	   	// Connect to the Index 
		IndexConfiguration indexConfig =  IndexConfiguration.getInstance();
		IndexPublication index = new IndexPublication(indexConfig);
        	              
        String indexName = indexConfig.getIndex();
        String documentType = indexConfig.getDocumentType();     
        String pathToFiles = UpdateIndex2.getPathToFiles();
        String urlDomain = UpdateIndex2.getDomainURL();
        CacheDataIDSHA1 sha1Calc = new CacheDataIDSHA1();
        log.info("Connect to index <" + indexName + "> with documents <" + documentType + ">");
        
        // Read json files
        log.info("Find out the pdf files in " + pathToFiles);       
        ObjectMapper mapper = new ObjectMapper();
        Path p = FileSystems.getDefault().getPath(pathToFiles);
        Stream<Path> walk = Files.walk(p, 1);     
        Iterator<Path> iterPath = walk.iterator();
    	int count = 0;
        int limit = 2;
        Vector<Publication> publicationList = new Vector<Publication>();                  
        
        while(iterPath.hasNext()) {
        		Path filePath = iterPath.next();        		
        		if (Files.isRegularFile(filePath)) {
        				
		        	try {		    			    				    		
		    			Publication pub = new Publication();				
		    			File workingPublication = filePath.toFile();
		    					    			
		    			// Set publication's id
		    			String fileName = workingPublication.getName();
		    			log.info("Working on publication file " + fileName);		
		    			String pubID = fileName.substring(0, fileName.indexOf("."));
		    			log.info("pubID  " + pubID);	
		    			pub.setOpenaireId(pubID);
		    			
		    			// Set mimetype
		    			pub.setMimeType("application/pdf");
		    					    					    				    			    						
		    			// Calculate new path to file
		    			String storePrefix = pubID.substring(0, pubID.lastIndexOf("::"));
		    			String id = pubID.substring(pubID.lastIndexOf("::")+2);		    			
		    			String extension = ExtensionResolver.getExtension(pub.getMimeType());		    			
		    			String relativeFileNew = storePrefix + "/" + id.substring(0, 3) +  "/" + pubID + extension;
		    			File publicationFileNew = new File(workingPublication.getParentFile() + "/" + relativeFileNew);
		    			
		    			log.info("Copy file to new location  ::" + publicationFileNew.getAbsolutePath());
		    			// Create store folder(s)
		    			File storeFolder = new File(pathToFiles + storePrefix + "/" + id.substring(0, 3));
		    			if (!storeFolder.exists()) {
		    				storeFolder.mkdirs();
		    			}
		    			// Move to new store folder													
		    			try {			
		    				FileUtils.copyFile(workingPublication, publicationFileNew);
		    				workingPublication.delete();
		    			} catch (IOException e) {
		    				e.printStackTrace();
		    			}																		
		    			
		    			// Set location to file
		    			pub.setPathToFile(publicationFileNew.getCanonicalPath());
		    			
		    			// Calculate hash value
		    			String hashValueSHA1;
		    			hashValueSHA1 = sha1Calc.getID(FileUtils.readFileToByteArray(publicationFileNew));
		    			pub.setHashValue(hashValueSHA1);
		    			
		    			// Set url
		    			pub.setUrl(urlDomain + relativeFileNew);
		    			
		    			// Add publication to bulk add action list
		    			log.info("Add publication " + pub.getOpenaireId() + " for bulk add action");
		    			publicationList.add(pub);		    
		    			count ++;
		    					    		
		    	    	// Export Publication in json and save to disk
		    			log.info("Save publication new metadata file to " + pathToFiles + "metadata_new/" + pubID + ".json");		    			
		    			Gson gson = new Gson();
		    			FileWriter fw = new FileWriter(pathToFiles + "metadata_new/" + pubID + ".json");
		    			gson.toJson(pub, fw);
		    			fw.close();
		    					    			
		            }
		            catch (Exception e) {
		            	e.printStackTrace();
		            }
		           
		        }
        		if (count == limit && !publicationList.isEmpty()) {
        			try {
        				// Add publications to index
        				log.info("Bulk add action now :: ");
        				index.addBulkPublications(publicationList);        				      				
        				// Reset counter and lists
        				count = 0;
        				publicationList = new Vector<Publication>();               			
        			}catch (Exception e) {
        				e.printStackTrace();
     		        }
        		}
        		
		}
    	if (count > 0 && !publicationList.isEmpty()) {
    		try {
    			// Add publications to index
    			log.info("Bulk add action now :: ");
    			index.addBulkPublications(publicationList);    			       				
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
	
    	}
        walk.close();	
        index.disconnect();       
        log.info("No more pdfs");
	}
		
	
	private static String getDomainURL() throws IOException {
         
         Properties configFile = new Properties();
         configFile.load(UpdateIndex2.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
		
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
         configFile.load(UpdateIndex2.class.getClassLoader().getResourceAsStream("publicationConfig.properties"));
         
         String pathToFiles = configFile.getProperty("pathToFiles");
         if (pathToFiles != null) {
              pathToFiles = pathToFiles.trim();
         } else {
	    	pathToFiles = "/media/pdfs/";
         }	   

         return pathToFiles;
    }
}
