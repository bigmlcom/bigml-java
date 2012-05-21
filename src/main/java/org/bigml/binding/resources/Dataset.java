package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete
 * datasets.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/dataset
 *
 **/
public class Dataset extends AbstractResource {
	
	// Logging
	Logger logger = LoggerFactory.getLogger(Dataset.class);
		
    
    /**
     *  Constructor
     *
     */
    public Dataset() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
  		this.bigmlApiKey = System.getProperty("BIGML_API_KEY");       
  		bigmlAuth = "?username="+this.bigmlUser+";api_key="+this.bigmlApiKey+";";
    }
    
    /**
     *  Constructor
     *
     */
    public Dataset(final String apiUser, final String apiKey) {
        this.bigmlUser = apiUser!=null ? apiUser: System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey!=null ? apiKey : System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username="+this.bigmlUser+";api_key="+this.bigmlApiKey+";";
    }
	
	/**
	  *  Create a new dataset.
	  *
	  *  POST /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
	  *  Host: bigml.io
	  *  Content-Type: application/json
	  *  
	  *  @param sourceId	a unique identifier in the form source/id where id is a string of 24 alpha-numeric chars for the source to attach the dataset.
	  *	 @param args		set of parameters for the new dataset. Optional
	  *	 @param waitTime	time to wait for next check of FINISHED status for source before to start to create the dataset. Optional
	  *
	  */ 
	public JSONObject create(final String sourceId, String args, Integer waitTime) {
	      if (sourceId==null || sourceId.length()==0 || !sourceId.matches(SOURCE_RE)) {
	          logger.info("Wrong source id");
	          return null;
	      }
	      
	      try {
		      waitTime = waitTime!=null ? waitTime : 3;
		      if (waitTime>0) {
		          while (!BigMLClient.getInstance().sourceIsReady(sourceId)) {
		              Thread.sleep(waitTime);
		          }
		      }
		      
		      JSONObject requestObject = new JSONObject();
		      if (args!=null) {
		    	  requestObject = (JSONObject) JSONValue.parse(args);
		      }
		      requestObject.put("source", sourceId);
		      
		      return createResource(DATASET_URL, requestObject.toJSONString());
	      } catch (Throwable e) {
	            logger.error("Error creating dataset");
	            return null;
	      }
	 }
   
   
   /**
    *  Retrieve a dataset.
    *
    *  GET /andromeda/dataset/4f66a0b903ce8940c5000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
	 *  Host: bigml.io 
    *  
    *  @param datasetId a unique identifier in the form datset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public JSONObject get(final String datasetId) {
       if (datasetId==null || datasetId.length()==0 || !datasetId.matches(DATASET_RE)) {
           logger.info("Wrong dataset id");
           return null;
       }
               
       return getResource(BIGML_URL + datasetId);
   }
   
   
   /**
    *  Retrieve a dataset.
    *
    *  GET /andromeda/dataset/4f66a0b903ce8940c5000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
	*  Host: bigml.io 
    *  
    *  @param dataset a unique identifier in the form datset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public JSONObject get(final JSONObject dataset) {
	   String resourceId = (String) dataset.get("resource");
   	   return get(resourceId);
   }
               
           
   /**
    *  Check whether a dataset' status is FINISHED.
    *
    *  @param datasetId a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public boolean isReady(final String datasetId) {
   	return isResourceReady(get(datasetId));
   }             
     
   
   /**
    *  Check whether a dataset' status is FINISHED.
    *
    *  @param dataset a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public boolean isReady(final JSONObject dataset) {
	   String resourceId = (String) dataset.get("resource");
   	   return isReady(resourceId);
   } 
   
           
   /**
    *  List all your datasources.
    *
    *  GET /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
    *  Host: bigml.io
    *  
    *  @param queryString	query filtering the listing.
    *
    */ 
   public JSONObject list(final String queryString) {
       return listResources(DATASET_URL, queryString);
   }
   
   
   /**
    *  Update a dataset.
    *
    *  PUT /andromeda/dataset/4f64191d03ce89860a000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
	 *  Host: bigml.io
	 *  Content-Type: application/json
	 *  
	 *  @param datasetId a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
	 *  @param json		set of parameters to update the source. Optional
    *
    */ 
   public JSONObject update(final String datasetId, final String json) {
   	if (datasetId==null || datasetId.length()==0 || !datasetId.matches(DATASET_RE)) {
           logger.info("Wrong dataset id");
           return null;
       }
       return updateResource(BIGML_URL+datasetId, json);
   }
   
   
   /**
    *  Update a dataset.
    *
    *  PUT /andromeda/dataset/4f64191d03ce89860a000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
    *  Host: bigml.io
	*  Content-Type: application/json
	*  
	*  @param dataset a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
	*  @param json		set of parameters to update the source. Optional
    *
    */ 
   public JSONObject update(final JSONObject dataset, final JSONObject json) {
	   String resourceId = (String) dataset.get("resource");
   	   return update(resourceId, json.toJSONString());
   }
   
           
   /**
    *  Delete a dataset.
    *  
    *  DELETE /andromeda/dataset/4f66a0b903ce8940c5000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 
    *
    *  @param datasetId a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public JSONObject delete(final String datasetId) {
       if (datasetId==null || datasetId.length()==0 || !datasetId.matches(DATASET_RE)) {
           logger.info("Wrong dataset id");
           return null;
       }
       return deleteResource(BIGML_URL + datasetId);
   }
   
   
   /**
    *  Delete a dataset.
    *  
    *  DELETE /andromeda/dataset/4f66a0b903ce8940c5000000?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 
    *
    *  @param dataset a unique identifier in the form dataset/id where id is a string of 24 alpha-numeric chars.
    *
    */ 
   public JSONObject delete(final JSONObject dataset) {
	   String resourceId = (String) dataset.get("resource");
	   return delete(resourceId);
   }
    
}
