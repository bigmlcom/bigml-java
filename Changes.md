# Changes in the BigML.io Java bindings

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

