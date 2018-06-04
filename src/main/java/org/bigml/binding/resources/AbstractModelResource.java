package org.bigml.binding.resources;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point to create, retrieve, list, update, and delete model based
 * resources.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api
 *
 */
public abstract class AbstractModelResource extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(AbstractModelResource.class);
    
    
    /**
     * Creates a new resource.
     *
     * POST /andromeda/xxxxx?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the resource.
     * @param args
     *            set of parameters for the new resource. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the resource. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(this.resourceUrl, requestObject.toJSONString());
    }
    

    /**
     * Creates a resource from a list of `datasets`.
     *
     * POST /andromeda/xxxxx?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            resource.
     * @param args
     *            set of parameters for the new resource. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the resource. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        
        return createResource(this.resourceUrl, requestObject.toJSONString());
    }
    
    
    
    // ################################################################
    // #
    // # Protected methods
    // #
    // ################################################################

    /**
     * Builds args dictionary for the create call from a `dataset` or a list of
     * `datasets`
     */
    protected JSONObject createFromDatasets(final String[] datasets,
            JSONObject args, Integer waitTime, Integer retries, String key) {

        JSONObject createArgs = new JSONObject();
        if (args != null) {
            createArgs = args;
        }

        List<String> datasetsIds = new ArrayList<String>();

        for (String datasetId : datasets) {
            // Checking valid datasetId
            if (datasetId == null || datasetId.length() == 0
                    || !(datasetId.matches(DATASET_RE))) {
                logger.info("Wrong dataset id");
                return null;
            }

            // Checking status
            try {
                waitForResource(datasetId, "datasetIsReady", 
                		waitTime, retries);
                datasetsIds.add(datasetId);
            } catch (Throwable e) {
                logger.error("Error creating object");
                return null;
            }

        }

        if (datasetsIds.size() == 1) {
            key = (key == null || key.equals("") ? "dataset" : key);
            createArgs.put(key, datasetsIds.get(0));
        } else {
            key = (key == null || key.equals("") ? "datasets" : key);
            createArgs.put(key, datasetsIds);
        }
        return createArgs;
    }
}
