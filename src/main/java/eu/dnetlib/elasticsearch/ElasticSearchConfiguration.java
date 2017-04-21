package eu.dnetlib.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

public class ElasticSearchConfiguration {
	
	private String host;
	private String port;
	
	public ElasticSearchConfiguration(String host, String port) {
		this.host = host;
		this.port = port;
	}
	
	public JestClient client() {

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(host+":" + port)
                .readTimeout(60000)
                .multiThreaded(true)
                .build());
        JestClient jestClient = factory.getObject();
        return jestClient;
}
	
	

}
