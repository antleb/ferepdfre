package eu.dnetlib.elasticsearch.entities;

import io.searchbox.annotations.JestId;

public class Publication {
	
	@JestId
	private String openaireId;
	private String hashValue;
	private String mimeType;
	private String pathToFile;
	private String url;
	
	public Publication() {
		this.openaireId = null;
		this.hashValue = null;
		this.mimeType = null;
		this.pathToFile = null;
	}
	
	public Publication(String id, String hash, String mimeType, String pathToFile, String url) {
		this.openaireId = id;
		this.hashValue = hash;
		this.mimeType = mimeType;
		this.pathToFile = pathToFile;
	}

	public String getOpenaireId() {
		return openaireId;
	}

	public void setOpenaireId(String openaireId) {
		this.openaireId = openaireId;
	}

	public String getHashValue() {
		return hashValue;
	}

	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getPathToFile() {
		return pathToFile;
	}

	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "[Publication " + this.openaireId + 
				" hash::" + this.hashValue +
				" mimeType::" + this.mimeType +
				" path::" + this.pathToFile +
				" url::" + this.url +
				"]";
		
	}
}
