/**
 * 
 */
package eu.dnetlib.elasticsearch;

import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;

/**
 * @author gkirtzou
 *
 */
public class MyJestResultHandler implements JestResultHandler<JestResult> {
	  @Override
	  public void completed(JestResult result) {
		  System.out.println("Result completed::" + result.getJsonString());	                		    	
	  }
	  
	  @Override
	  public void failed(Exception ex) {
		  System.out.println("Result failed::" + ex.getMessage());
	  }

}
