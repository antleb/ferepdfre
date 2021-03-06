package eu.dnetlib.elasticsearch.entities;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import eu.dnetlib.elasticsearch.entities.Publication;
import eu.openminted.omtdcache.CacheDataIDMD5;

public class PublicationGenerator {
	
	private String pathToFiles;
	private String urlDomain;
	private int range;
	private Random rand;
	private CacheDataIDMD5 md5Calculator;
	
	public PublicationGenerator(String pathToFiles, String urlDomain, int range) {
		this.pathToFiles = pathToFiles;
		this.urlDomain = urlDomain;
		this.range = range;
		md5Calculator = new CacheDataIDMD5();
		rand = new Random();
	} 
	
	public Publication generatePublication() throws IOException {
		Publication pub = new Publication();
		int id = rand.nextInt(this.range);
		// openaireId
		String openaireID = UUID.randomUUID().toString();
		pub.setOpenaireId(openaireID);
		// MimeType
		pub.setMimeType("application/pdf");
	    // pathToFile
		String pathToFile = pathToFiles +  id +  ".pdf";
		pub.setPathToFile(pathToFile);
		
		// hash value
		byte[] file = FileUtils.readFileToByteArray(new File(pathToFile));
		String hashValue = md5Calculator.getID(file); 
		pub.setHashValue(hashValue);		
		
		// url
		String url = urlDomain + id + ".pdf";
		pub.setUrl(url);
		return pub;
	}

}
