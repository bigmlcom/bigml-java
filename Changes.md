# Changes in the BigML.io Java bindings

## 1.8.8 version

### Bugs

* Fixed bug in LocalCluster parsing critical_value property.
* Fixed bug parsing tags in Source creation.

## 1.8.7 version

### Bugs

* Fixed bug in LocalDeepnet predict probability.

## 1.8.6 version

### Bugs

* Fixed bug in computing text and categorical field expansion.
* Fixed test.

## 1.8.5 version

* Adding PCA REST call methods.
* Adding local PCAs and Projections.

### Bugs

* Changed ModelFields to use static logger


## 1.8.4 version

### Bugs

* Fixed bug in LocalPredictiveModel predicting with model using weights.

## 1.8.3 version

### Bugs

* Fixed bug in LocalPredictiveModel predicting with model using weights.


## 1.8.2 version

### Improvements

* Upgraded dependencies due to potential security vulnerabilities:
	spring-core, spring-context, sprint-tx: 4.3.20

## 1.8.1 version

### Improvements

* Upgraded dependencies due to potential security vulnerabilities:
	lucene-core: 7.1.0


## 1.8.0 version

### Improvements

* Added support for Local Time Series.
* DevMode removed
* OptiML Resource
* Fusion Resource
* Removing seed as a connection attribute.
* Local Clusters. 
	- Support for optype items
	- New utility methods: closestInCluster, sortedCentroids.
	- Fixed bugs.
* Added support for Local Topic Models

	In order to give support to java 6 users the binding uses version 
	4.7.0 for lucene-core and lucene-analyzers-common. If java is 1.8 or 
	greater set the version of these libraries to 7.3.1 in pom.xml
	
    		<dependency>
		    <groupId>org.apache.lucene</groupId>
		    <artifactId>lucene-core</artifactId>
		    <version>7.3.1</version>
		</dependency>
		<dependency>
		    <groupId>org.apache.lucene</groupId>
		    <artifactId>lucene-analyzers-common</artifactId>
		    <version>7.3.1</version>
		</dependency>
* Added support for Local Logistic Regression
* Added support for Local Deepnet
* LocalPredictiveModel: support for operating_point, operating_kind
* LocalEnsemble: support for operating_point, operating_kind
* Added support for Local Fusions
* Adding organizations support for all the API calls.
* Deprecating param byName in Local objects
* Support for shared/public remote resources

### Bugs

* Fixed test suite
* Fixed bug in predictions for Deepnets.


## 1.7.0 version

### Improvements

* Deepnet Resource
* TimeSeries/Forecast Resource
* Support for name or column for field ID in predictions and anomaly scores
* Local Association object

### Bugs

* Fixed test suite


## 1.6.2 version

### Improvements

* BigML-Sample-Client extended with new examples and inproved the documentation

### Bugs

* Execution creation was ignoring arguments passed by the user.


## 1.6.0 version

### Improvements

* Association Resource
* AssociationSet Resource
* TopicModel Resource
* TopicDistribution Resource
* BatchTopicDistribution Resource
* Configuration Resource

### Bugs

* Fixed bug creating prediction from ensemble, input data was no being used for the prediction.

* Fixed typos and updated document URLs.

* Fixed test suite.

## 1.5.1 version

### Improvements

* Create evaluation from Logistic Regression.

## 1.5 version

### Improvements

* Create dataset from list of Datasets
* Correlation Resource
* StatisticalTest Resource
* Logistic Regression Resource
* Tests renaming and fixing
* Made Local Ensemble and dependencies serializable
* Preditcion/BatchPrediction: create from LogisticRegression
* Create model from cluster
* Whizzml Resources


## 1.4.2 version

### Bugs

* Fixed a LocalEnsemble prediction bug with the comparability option.

* Fixed invalid samples and errors/typos.

* Provided a workaround for JSON escaping forward slashes (/).

### Improvements

* Source didn't accept arguments other than name and source_parser. It now accepts all other arguments.

* An initial version of BigML Java binding document is created and published at http://bigml-java.readthedocs.org.


## 1.4.1 version

### Bugs

* Fixed bug in the constructor of the LocalEnsemble. The JSON path used to access objects was wrong.

## 1.4 version

### New Features

* Added support for Local Anomalies

    A new LocalAnomaly class to score anomalies in a dataset locally or embedded
    into your application without needing to send requests to BigML.io.

    This class cannot only save you a few credits, but also enormously reduce the
    latency for each prediction and let you use your models offline.

    Example usage (assuming that you have previously set up the BIGML_USERNAME
    and BIGML_API_KEY environment variables and that you own the model/id below):

        // API client
        BigMLClient api = new BigMLClient();

        // Retrieve a remote anomaly by id
        JSONObject jsonAnomaly = api.getAnomaly("anomaly/551aa203af447f5484000ec0");

        // A lightweight wrapper around a Anomaly resurce
        LocalAnomaly localAnomaly = new LocalAnomaly(jsonAnomaly);

        // Input data
        JSONObject inputData = (JSONObject) JSONValue.parse("{\"src_bytes\": 350}");

        // Calculate score
        localAnomaly.score(inputData);


* Added support for Samples resources.

    Full API documentation on the API can be found from BigML at:
    https://bigml.com/developers/samples

* Added support for Project resources.

    Full API documentation on the API can be found from BigML at:
    https://bigml.com/developers/project

* Added support for predictions using Median.

### Improvements

* We improve the predictions responses adding the more information: Distribution,
    DistributionUnit, Count and Median values associated to the prediction.

* Added methods in LocalModel class to get information of the model. Added methods: Summarize,
    Data Distribution and Prediction Distribution.

* Added BaseModel and ModelFields base classes to reuse logic in LocalCluster and LocalPredictiveModel

## 1.3 version

### New Features

* Added support for Anomaly detectors. Predictive model that can help identify the instances within a dataset that
    do not conform to a regular pattern.

    Now you can create new anomaly detectors, retrieve individual anomaly detectors, list your anomaly detectors,
    delete, and also update your anomaly detectors.

* Added support for remote Anomaly Scores. This resources will automatically compute a score between 0 and 1.
    The closer the score is to 1, the more anomalous the instance being scored is.
    The Anomaly Score will also compute the relative importance for each field. That is, how much each value
    in the input data contributed to the score.

    Now you can create new anomaly scores, retrieve individual anomaly scores, list your anomaly scores,
    delete, and also update your anomaly scores.

* Added support for remote Batch Anomaly Scores. A batch anomaly score provides an easy way to compute an
    anomaly score for each instance in a dataset in only one request. To create a new batch anomaly score
    you will need an anomaly/id and a dataset/id.

* Local Ensembles improvements.

** Access to the Field Importance information

** New combiners supported in Local Ensembles:.

    confidence weighted: uses prediction confidences as weight for the combined prediction.

    probability weighted: uses the probability of the class in the distribution of classes in
                the leaf node as weight.

    threshold-based: uses a given threshold k (by default 1) to predict a given class (by default the
                     minority class). You can set up both using the threshold argument. If there are less
                     than k models voting class, the most frequent of the remaining categories is chosen,
                     as in a plurality combination after removing the models that were voting for class.
                     The confidence of the prediction is computed as that of a plurality vote, excluding
                     votes for the majority class when it's not selected.

* Local Predictions. Added support to "last prediction" and "proportional" missing strategies.

* Local Clusters and Local Centroids. You can now instantiate a local version of a remote
    cluster that you can use to make local centroid predictions associated to an input data set.

    You must keep in mind, though, that to obtain a centroid prediction, input data must have values
    for all the numeric fields. No missing values for the numeric fields are allowed.

    As in the local model predictions, producing local centroids can be done independently of BigML
    servers, so no cost or connection latencies are involved.

* Now is possible to create sources using the results of a batch anomaly score.

* Now is possible to create sources using the results of a batch prediction.

* Allow save and load batch predictions to/into local CSV files.

* Added support for two different missing strategies for local predictions: last prediction and proportional.

* Added support for download the content of a dataset.

* Added support for download the result of a batch prediction.

* We can change the BigML Domain programmatically (Ex. https://myprivatedeploy.com)

### Bugs

* Fix bug sending json with UTF-8 characters. We were using the DataOutputStream class to send the data to the
    server instead the OutputStreamWriter class.

* Fix bug in multipart request. The connection was not using the MockTrustManagers.



## 1.2 version

### New Features

* Since version 1.2 we start to publish the release on the Maven Central Repository. Now, you can declare
the library dependency in your project's pom file.

    If you were using the Apache HttpClient library in your own project, now you will need to explicitly declare this
dependency in your own project's pom file, because it's not anymore a dependency of our library.

* We have removed the unnecessary dependencies with third party libraries: Apache HttpClient, SLF4J-Log4j and
    SuperCsv.

* Support for json files in UTF-8 encoding. Now we can send json objects with UTF-8 characters inside.

* Added the system property **BIGML_SEED** to allow clients to generate deterministic models.

* Added a **Seed** to the tests to make them deterministic

* Contact support information has been updated

### Bugs

* Fixed bug in predictions when we were using fields in the input data that had periods in their names.



## 1.1 version

* Since version 1.1 the name of the JAR file is bigml-binding.

