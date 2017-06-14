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
public class UpdateIndex implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(UpdateIndex.class);
	

	
	public static void main(String[] args) throws IOException, Exception {
			
		 // Multithread
        ExecutorService service = Executors.newFixedThreadPool(2);

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
        ObjectMapper mapper = new ObjectMapper();
        Path p = FileSystems.getDefault().getPath(pathToFiles + "metadata/");
        Stream<Path> walk = Files.walk(p);     
        Iterator<Path> iterPath = walk.iterator();
        
        
        while(iterPath.hasNext()) {
        		Path filePath = iterPath.next();
		        if (Files.isRegularFile(filePath)) {
		        	service.submit(new UpdateIndex(filePath, mapper, pathToFiles, urlDomain, index, sha1Calc));		        				        
		        }
		}
        walk.close();	
        index.disconnect();
        log.info("Add in index " + counter + " documents");
	}
	
	public UpdateIndex(Path filePath, ObjectMapper mapper, String pathToFiles, String urlDomain, IndexPublication index, CacheDataIDSHA1 sha1Calc) {
		this.filePath = filePath;
		this.mapper = mapper;
		this.pathToFiles = pathToFiles; 
		this.index = index;
		this.urlDomain = urlDomain;
		this.sha1Calc = sha1Calc;
		incrementCounter();
		this.countID = counter.get();
	}

	private int countID;
	private Path filePath;
	private ObjectMapper mapper;
	private String pathToFiles; 
	private IndexPublication index;
	private String urlDomain;
	private CacheDataIDSHA1 sha1Calc;
		
	@Override
	public void run() {
		
		try {
			log.info(countID + " :: Loading publication from json file " + filePath);
			File workingFile = filePath.toFile();
			Publication pub;					        
			pub = mapper.readValue(workingFile, Publication.class);
			
			String pubID = pub.getOpenaireId();
			log.info(countID + " :: Loaded publication :: " + pub.toString());
			
			// Get old path to file
			File fileOld = new File(pub.getPathToFile());					
			// Calculate new path to file
			String storePrefix = pubID.substring(0, pubID.lastIndexOf("::"));
			String id = pubID.substring(pubID.lastIndexOf("::")+2);
			String extension = ExtensionResolver.getExtension(pub.getMimeType());
			String relativeFileNew = storePrefix + "/" + id.substring(0, 3) +  "/" + pubID + extension;
			File fileNew = new File(fileOld.getParentFile() + "/" + relativeFileNew);
			
			if (fileOld.exists()) {				
				log.info(countID + " :: Copy file to new location  ::" + fileNew.getAbsolutePath());
				// Create store folder(s)
				File storeFolder = new File(pathToFiles + storePrefix + "/" + id.substring(0, 3));
				if (!storeFolder.exists()) {
					storeFolder.mkdirs();
				}
				// Move to new store folder													
				try {			
					FileUtils.copyFile(fileOld, fileNew);
					fileOld.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}																		
			}
			// Update to new location
			pub.setPathToFile(fileNew.getCanonicalPath());
			
			// Recalculate hash value
			String hashValueSHA1;
			hashValueSHA1 = sha1Calc.getID(FileUtils.readFileToByteArray(fileNew));
			pub.setHashValue(hashValueSHA1);
			
			// Set url
			pub.setUrl(urlDomain + relativeFileNew);
			
			
			log.info(countID + " :: Updated publication ::" + pub.toString());
			// Update publication in index
			index.addAsyncPublication(pub, pubID); 
			
				// Export Publication in json and save to disk
			Gson gson = new Gson();
			FileWriter fw = new FileWriter(pathToFiles + "metadata_new/" + pubID + ".json");
			gson.toJson(pub, fw);
			fw.close();
			
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
      
    
	}
	
	private static final AtomicInteger counter = new AtomicInteger(0); // a global counter
	
	private static void incrementCounter() {
          counter.getAndIncrement();
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
