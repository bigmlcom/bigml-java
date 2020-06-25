Resources
=====================

## Creating Resources
------------

Newly-created resources are returned in a dictionary with the following
keys:

-  **code**: If the request is successful you will get a
   ``HTTP_CREATED`` (201) status code. In asynchronous file uploading
   ``api.createSource`` calls, it will contain ``HTTP_ACCEPTED`` (202)
   status code. Otherwise, it will be one of the standard HTTP error codes [detailed in the documentation](https://bigml.com/api/status_codes).
-  **resource**: The identifier of the new resource.
-  **location**: The location of the new resource.
-  **object**: The resource itself, as computed by BigML.
-  **error**: If an error occurs and the resource cannot be created, it
   will contain an additional code and a description of the error. In
   this case, **location**, and **resource** will be ``None``.

### Statuses
---------

Please, bear in mind that resource creation is almost always
asynchronous (**predictions** are the only exception). Therefore, when
you create a new source, a new dataset or a new model, even if you
receive an immediate response from the BigML servers, the full creation
of the resource can take from a few seconds to a few days, depending on
the size of the resource and BigML's load. A resource is not fully
created until its status is ``FINISHED``. See the
[documentation on status codes](https://bigml.com/api/status_codes) for the listing of potential states and their semantics. So depending on your application you might need to import the following constants:

```
    import org.bigml.binding.resources.AbstractResource;

    AbstractResource.FINISHED
    AbstractResource.QUEUED
    AbstractResource.STARTED
    AbstractResource.IN_PROGRESS
    AbstractResource.SUMMARIZED
    AbstractResource.FINISHED
    AbstractResource.UPLOADING
    AbstractResource.FAULTY
    AbstractResource.UNKNOWN
    AbstractResource.RUNNABLE
```

Usually, you will simply need to wait until the resource is in the ``FINISHED`` state for further processing. If that's the case, the easiest way is calling the ``api.xxxIsReady`` method and passing as first argument the object that contains your resource:

```
    import org.bigml.binding.BigMLClient;
    
    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

    // creates a source object
    JSONObject source = api.createSource("my_file.csv");

    // checks that the source is finished and updates ``source``
    while (!api.sourceIsReady(source)) 
        Thread.sleep(1000);
```


In this code, ``api.createSource`` will probably return a non-finished
``source`` object. Then, ``api.sourceIsReady`` will query its status and update the contents of the ``source`` variable with the retrieved information until it reaches a ``FINISHED`` or ``FAILED`` status.

Remember that, consequently, you will need to retrieve the resources
explicitly in your code to get the updated information.


### Projects
------------

A special kind of resource is ``project``. Projects are repositories
for resources, intended to fulfill organizational purposes. Each project can
contain any other kind of resource, but the project that a certain resource
belongs to is determined by the one used in the ``source`` they are generated from. Thus, when a source is created and assigned a certain ``project_id``, the rest of resources generated from this source will remain in this project.

The REST calls to manage the ``project`` resemble the ones used to manage the
rest of resources. When you create a ``project``:

```
    import org.bigml.binding.BigMLClient;
    
    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

    JSONObject project = api.createProject({"name": "my first project"});
```

the resulting resource is similar to the rest of resources, although shorter:

```
{
    "code": 201,
    "resource": "project/54a1bd0958a27e3c4c0002f0",
    "location": "http://bigml.io/andromeda/project/54a1bd0958a27e3c4c0002f0",
    "object": {
        "category": 0,
        "updated": "2014-12-29T20:43:53.060045",
        "resource": "project/54a1bd0958a27e3c4c0002f0",
        "name": "my first project",
        "created": "2014-12-29T20:43:53.060013",
        "tags": [],
        "private": True,
        "dev": None,
        "description": ""
    },
    "error": None
}
```

and you can use its project id to get, update or delete it:

```
    JSONObject project = api.getProject("project/54a1bd0958a27e3c4c0002f0");
    String resource = (String) Utils.getJSONObject(
        project, "resource");
    api.updateProject(resource,
                      {'description': 'This is my first project'});

    api.deleteProject(resource);
```

**Important**: Deleting a non-empty project will also delete **all resources**
assigned to it, so please be extra-careful when using the ``api.deleteProject`` call.

### External Connectors
-----------------------


To create an external connector to an existing database you need to use the
``createExternalConnector`` method. The only two required parameters are the  the name of the external data source to connect to (allowed types are:
``elasticsearch``, ``postgresql``, ``mysql``, ``sqlserver``) and the dictionary that contains the information needed to connect to the particular database/table. The attributes of the connection dictionary needed for the method to work will depend on the type of database used.

For instance, you can create a connection to an ``Elasticsearch`` database
hosted locally at port ``9200`` by calling:


```
    import org.bigml.binding.BigMLClient;
    
    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

    JSONObject connectionInfo = JSONValue.parse(
        "{\"hosts\": [\"elasticsearch\"]}"
    ); 
    JSONObject externalConnector = api.createExternalConnector(
        elasticsearch, connectionInfo);
```



### Sources
------------

To create a source from a local data file, you can use the ``createSource`` method. The only required parameter is the path to the data file (or file-like object). You can use a second optional parameter to specify any of the
options for source creation described in the [BigML API documentation](https://bigml.com/api/sources).

Here's a sample invocation:

```
    import org.bigml.binding.BigMLClient;
    
    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

    JSONObject args = JSONValue.parse(
        "{\"name\": \"my source\",
           \"source_parser\": {\"missing_tokens\": [\"?\""]}}"
    ); 
    JSONObject source = api.createSource("./data/iris.csv", args);
```

or you may want to create a source from a file in a remote location:

    source = api.createRemoteSource("s3://bigml-public/csv/iris.csv", args)

or using data stored in a local java variable. The following example shows the two accepted formats:

```
    String inline = "[{\"a\": 1, \"b\": 2, \"c\": 3}, 
                      {\"a\": 4, \"b\": 5, \"c\": 6}]";
    JSONObject args = JSONValue.parse("{\"name\": \"inline source\"}");
    JSONObject source = api.createInlineSource(
        inline, {'name': 'inline source'});
```

As already mentioned, source creation is asynchronous. In both these examples,
the ``api.createSource`` call returns once the file is uploaded.
Then ``source`` will contain a resource whose status code will be either
``WAITING`` or ``QUEUED``.


### Datasets
------------

Once you have created a source, you can create a dataset. The only
required argument to create a dataset is a source id. You can add all
the additional arguments accepted by BigML and documented in the
[Datasets section of the Developer's documentation](https://bigml.com/api/datasets).

For example, to create a dataset named "my dataset" with the first 1024
bytes of a source, you can submit the following request:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my dataset\", \"size\": 1024}");
    JSONObject dataset = api.createDataset(source, args);
```

Upon success, the dataset creation job will be queued for execution, and
you can follow its evolution using ``api.datasetIsReady(dataset)``.

As for the rest of resources, the create method will return an incomplete
object, that can be updated by issuing the corresponding
``api.getDataset`` call until it reaches a ``FINISHED`` status.
Then you can export the dataset data to a CSV file using:

    api.downloadDataset("dataset/526fc344035d071ea3031d75",
        filename="my_dir/my_dataset.csv");

You can also extract samples from an existing dataset and generate a new one
with them using the ``api.createDataset`` method. The first argument should
be the origin dataset and the rest of arguments that set the range or the
sampling rate should be passed as a dictionary. For instance, to create a new
dataset extracting the 80% of instances from an existing one, you could use:

    JSONObject originDataset = api.createSataset(source);
    JSONObject sampleArgs = JSONValue.parseValue("{\"sample_rate\": 0.8}");
    JSONObjectdataset = api.createDataset(originDataset, sampleArgs);

Similarly, if you want to split your source into training and test datasets,
you can set the `sample_rate` as before to create the training dataset and
use the `out_of_bag` option to assign the complementary subset of data to the
test dataset. If you set the `seed` argument to a value of your choice, you
will ensure a deterministic sampling, so that each time you execute this call
you will get the same datasets as a result and they will be complementary:

```
    JSONObject originDataset = api.createSataset(source);

    JSONObject trainArgs = JSONValue.parseValue(
        "{\"name\": \"Dataset Name | Training\", 
          \"sample_rate\": 0.8, 
          \"seed\": \"my seed\"}");
    JSONObject trainDataset = api.createDataset(originDataset, trainArgs);

    JSONObject testArgs = JSONValue.parseValue(
        "{\"name\": \"Dataset Name | Test\", 
          \"sample_rate\": 0.8, 
          \"seed\": \"my seed\",
          \"out_of_bag\": true}");
    JSONObject testDataset = api.createDataset(originDataset, testArgs);
```

Sometimes, like for time series evaluations, it's important that the data
in your train and test datasets is ordered. In this case, the split
cannot be done at random. You will need to start from an ordered dataset and
decide the ranges devoted to training and testing using the ``range``
attribute:

```
    JSONObject originDataset = api.createSataset(source);

    JSONObject trainArgs = JSONValue.parseValue(
        "{\"name\": \"Dataset Name | Training\", 
          \"range\": [1, 80]}");
    JSONObject trainDataset = api.createDataset(originDataset, trainArgs);

    JSONObject testArgs = JSONValue.parseValue(
        "{\"name\": \"Dataset Name | Test\",
          \"range\": [81, 100]}");
    JSONObject testDataset = api.createDataset(originDataset, testArgs);
```
    
It is also possible to generate a dataset from a list of datasets
(multidataset):

```
    JSONObject dataset1 = api.createDataset(source1);
    JSONObject dataset2 = api.createDataset(source2);
    List datasetsIds = new ArrayList();
    datasetsIds.add(dataset1);
    datasetsIds.add(dataset2);
    JSONObject multidataset = api.createDataset(datasetsIds);
```

Clusters can also be used to generate datasets containing the instances grouped around each centroid. You will need the cluster id and the centroid id
to reference the dataset to be created. For instance,

```
    JSONObject cluster = api.createCluster(dataset);
    JSONObject args = JSONValue.parseValue("{\"centroid\": \"000000\"}");
    JSONObject clusterDataset1 = api.createDataset(cluster, args);
```

would generate a new dataset containing the subset of instances in the cluster
associated to the centroid id ``000000``.


### Models
------------

Once you have created a dataset you can create a model from it. If you don't
select one, the model will use the last field of the dataset as objective
field. The only required argument to create a model is a dataset id.
You can also include in the request all the additional arguments accepted by BigML and documented in the [Models section of the Developer's
documentation](https://bigml.com/api/models).

For example, to create a model only including the first two fields and the first 10 instances in the dataset, you can use the following invocation:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my model\",
          \"input_fields\": [\"000000\", \"000001\"],
          \"range\": [1, 10]}");
    JSONObject model = api.createModel(dataset, args);
```

Again, the model is scheduled for creation, and you can retrieve its status at any time by means of ``api.modelIsReady(model)``.

Models can also be created from lists of datasets. Just use the list of ids as the first argument in the api call

```
    JSONObject dataset1 = api.createDataset(source1);
    JSONObject dataset2 = api.createDataset(source2);
    List datasetsIds = new ArrayList();
    datasetsIds.add(dataset1);
    datasetsIds.add(dataset2);
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my model\",
          \"input_fields\": [\"000000\", \"000001\"],
          \"range\": [1, 10]}");
    JSONObject model = api.createModel(datasetsIds, args);
```

And they can also be generated as the result of a clustering procedure. When
a cluster is created, a model that predicts if a certain instance belongs to
a concrete centroid can be built by providing the cluster and centroid ids:

```
    JSONObject cluster = api.createCluster(dataset);
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"model for centroid 000001\",
          \"centroid\": \"000001\"}");
    JSONObject model = api.createModel(cluster, args);
```

if no centroid id is provided, the first one appearing in the cluster is used.


### Clusters
------------

If your dataset has no fields showing the objective information to predict for the training data, you can still build a cluster that will group similar data around some automatically chosen points (centroids). Again, the only required
argument to create a cluster is the dataset id. You can also include in the request all the additional arguments accepted by BigML and documented in the [Clusters section of the Developer's documentation](https://bigml.com/api/clusters).

Let's create a cluster from a given dataset:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my cluster\", \"k\": 5}");
    JSONObject cluster = api.createCluster(dataset, args);
```

that will create a cluster with 5 centroids.


### Anomaly detectors
------------

If your problem is finding the anomalous data in your dataset, you can
build an anomaly detector, that will use iforest to single out the
anomalous records. Again, the only required
argument to create an anomaly detector is the dataset id. You can also
include in the request all the additional arguments accepted by BigML
and documented in the [Anomaly detectors section of the Developer's
documentation](https://bigml.com/api/anomalies).

Let's create an anomaly detector from a given dataset:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my anomaly\"}");
    JSONObject anomaly = api.createAnomaly(dataset, args);
```

that will create an anomaly resource with a `top_anomalies` block of the
most anomalous points.


### Associations
------------

To find relations between the field values you can create an association
discovery resource. The only required argument to create an association
is a dataset id. You can also include in the request all the additional arguments accepted by BigML and documented in the [Association section of the Developer's documentation](https://bigml.com/api/associations.

For example, to create an association only including the first two fields and
the first 10 instances in the dataset, you can use the following
invocation:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my association\",
          \"input_fields\": [\"000000\", \"000001\"],
          \"range\": [1, 10]}");
    JSONObject association = api.createAssociation(dataset, args);
```

Again, the association is scheduled for creation, and you can retrieve its
status at any time by means of ``api.associtionIsReady(association)``.

Associations can also be created from lists of datasets. Just use the
list of ids as the first argument in the api call

```
    List datasetsIds = new ArrayList();
    datasetsIds.add(dataset1);
    datasetsIds.add(dataset2);
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my association\",
          \"input_fields\": [\"000000\", \"000001\"],
          \"range\": [1, 10]}");
    JSONObject association = api.createAssociation(dataset, args);
```


### Topic models
------------

To find which topics do your documents refer to you can create a topic model.
The only required argument to create a topic model is a dataset id. You can also include in the request all the additional arguments accepted by BigML and documented in the [Topic Model section of the Developer's documentation](https://bigml.com/api/topicmodels).

For example, to create a topic model including exactly 32 topics you can use the following invocation:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my topics\",
          \"number_of_topics\": 32}");
    JSONObject topicModel = api.createTopicModel(dataset, args);
```

Again, the topic model is scheduled for creation, and you can retrieve its
status at any time by means of ``api.topicModelIsReady(topicModel)``.

Topic models can also be created from lists of datasets. Just use the list of ids as the first argument in the api call.

```
    List datasetsIds = new ArrayList();
    datasetsIds.add(dataset1);
    datasetsIds.add(dataset2);
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my topics\",
          \"number_of_topics\": 32}");
    JSONObject topicModel = api.createTopicModel(datasetsIds, args);
```


### Time series
------------

To forecast the behaviour of any numeric variable that depends on its historical records you can use a time series. The only required argument to create a time series is a dataset id.
You can also include in the request all the additional arguments accepted by BigML and documented in the [Time Series section of the Developer's documentation](https://bigml.com/api/timeseries.

For example, to create a time series including a forecast of 10 points for the numeric values you can use the following invocation:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my time series\",
          \"horizon\": 10}");
    JSONObject timeSeries = api.createTimeSeries(dataset, args);
```

Again, the time series is scheduled for creation, and you can retrieve its
status at any time by means of ``api.timeSeriesIsReady(timeSeries)``.

Time series also be created from lists of datasets. Just use the list of ids as the first argument in the api call

```
    List datasetsIds = new ArrayList();
    datasetsIds.add(dataset1);
    datasetsIds.add(dataset2);
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my time series\",
          \"horizon\": 10}");
    JSONObject timeSeries = api.createTimeSeries(datasetsIds, args);
```


### OptiML
------------

To create an OptiML, the only required argument is a dataset id.
You can also include in the request all the additional arguments accepted by BigML and documented in the [OptiML section of the Developer's documentation](https://bigml.com/api/optimls).

For example, to create an OptiML which optimizes the accuracy of the model you
can use the following method

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my optiml\",
          \"metric\": \"accuracy\"}");
    JSONObject optiml = api.createOptiML(dataset, args);
```

The OptiML is then scheduled for creation, and you can retrieve its
status at any time by means of ``api.optiMLIsReady(optiml)``.


### Fusion
------------

To create a Fusion, the only required argument is a list of models.
You can also include in the request all the additional arguments accepted by BigML and documented in the [Fusion section of the Developer's documentation](https://bigml.com/api/fusions).

For example, to create a Fusion you can use this connection method:

```
    List modelsIds = new ArrayList();
    modelsIds.add("model/5af06df94e17277501000010");
    modelsIds.add("model/5af06df84e17277502000019");
    modelsIds.add("deepnet/5af06df84e17277502000016");
    modelsIds.add("ensemble/5af06df74e1727750100000d");
    JSONObject args = JSONValue.parseValue("{\"name\": \"my fusion\"}");
    JSONObject fusion = api.createFusion(modelsIds, args);
```

The Fusion is then scheduled for creation, and you can retrieve its
status at any time by means of ``api.fusionIsReady(fusion)``.

Fusions can also be created by assigning some weights to each model in the
list. In this case, the argument for the create call will be a list of
dictionaries that contain the ``id`` and ``weight`` keys:

```
    JSONArray models = JSONValue.parseValue(
        "[{\"id\": \"model/5af06df94e17277501000010\", \"weight\": 10},
          {\"id\": \"model/5af06df84e17277502000019\", \"weight\": 20},
          {\"id\": \"deepnet/5af06df84e17277502000016\",\"weight\": 5}]}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my weighted fusion\"}");
    JSONObject fusion = api.createFusion(models, args);
```


### Predictions
------------

You can now use the model resource identifier together with some input parameters to ask for predictions, using the ``createPrediction`` method. You can also give the prediction a name:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"sepal length\": 5,
          \"sepal width\": 2.5});
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my prediction\"}");
    JSONObject prediction = api.createPrediction(
        "model/5af272fe4e1727d3780000d6", inputData, args);
```

Predictions can be created using any supervised model (model, ensemble,
logistic regression, linear regression, deepnet and fusion) as first argument.


### Centroids
------------

To obtain the centroid associated to new input data, you can now use the ``createCentroid`` method. Give the method a cluster identifier and the input data to obtain the centroid. You can also give the centroid predicition a name:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"pregnancies\": 0,
          \"plasma glucose\": 118,
          \"blood pressure\": 84,
          \"triceps skin thickness\": 47}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my centroid\"}");
    JSONObject centroid = api.createCentroid(
        "cluster/56c42ea47e0a8d6cca0151a0", inputData, args);
```


### Anomaly scores
------------

To obtain the anomaly score associated to new input data, you can now use the ``createAnomalyScore`` method. Give the method an anomaly detector identifier and the input data to obtain the score:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"src_bytes\": 350}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my score\"}");
    anomaly_score = api.create_anomaly_score(
        "anomaly/56c432728a318f66e4012f82", inputData, args);
```


### Association sets
------------

Using the association resource, you can obtain the consequent items associated
by its rules to your input data. These association sets can be obtained calling
the ``createAssociationSet`` method. The first argument is the association
ID and the next one is the input data.

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"genres\": \"Action$Adventure\"}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my association set\"}");
    JSONObject associationSet = api.createAssociationSet(
        "association/5621b70910cb86ae4c000000", inputData);
```


### Topic distributions
------------

To obtain the topic distributions associated to new input data, you
can now use the ``createTopicDistribution`` method. Give
the method a topic model identifier and the input data to obtain the score:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"Message\": \"The bubble exploded in 2007.\"}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my topic distribution\"}");
    JSONObject topicDistribution = api.createTopicDistribution(
        "topicmodel/58362aaa983efc45a1000007", inputData, args);
```


### Forecasts
------------

To obtain the forecast associated to a numeric variable, you can now use the ``createForecast`` method. Give the method a time series identifier and the input data to obtain the forecast:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"Final\": {\"horizon\": 10}}");
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my forecast\"}");
    JSONObject forecast = api.createForecast(
        "timeseries/596a0f66983efc53f3000000", inputData, args);
```


### Evaluations
------------

Once you have created a supervised learning model, you can measure its perfomance by running a dataset of test data through it and comparing its predictions to the objective field real values. Thus, the required arguments to create an evaluation are model id and a dataset id. You can also include in the request all the additional arguments accepted by BigML and documented in the [Evaluations section of the Developer's documentation](https://bigml.com/api/evaluations).

For instance, to evaluate a previously created model using an existing dataset
you can use the following call:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my evaluation\"}");
    JSONObject evaluation = api.createEvaluation(
        "model/5afde64e8bf7d551fd005131", 
        "dataset/5afde6488bf7d551ee00081c", 
        args);
```

Again, the evaluation is scheduled for creation and ``api.evaluationIsReady(evaluation)`` will show its state.

Evaluations can also check the ensembles' performance. To evaluate an ensemble
you can do exactly what we just did for the model case, using the ensemble
object instead of the model as first argument:

    JSONObject evaluation = api.createEvaluation(
        "ensemble/5af272eb4e1727d378000050", 
        "dataset/5afde6488bf7d551ee00081c");

Evaluations can be created using any supervised model (including time series)
as first argument.


### Ensembles
------------

To improve the performance of your predictions, you can create an ensemble of models and combine their individual predictions. The only required argument to create an ensemble is the dataset id:

    JSONObject ensemble = api.createEnsemble(
        "dataset/5143a51a37203f2cf7000972");

BigML offers three kinds of ensembles. Two of them are known as ``Decision Forests`` because they are built as collections of ``Decision trees`` whose predictions are aggregated using different combiners (``plurality``, ``confidence weighted``, ``probability weighted``) or setting a ``threshold``
to issue the ensemble's prediction. All ``Decision Forests`` use bagging to sample the data used to build the underlying models.

As an example of how to create a ``Decision Forest`` with `20` models, you only need to provide the dataset ID that you want to build the ensemble from and the number of models:

```
    JSONObject args = JSONValue.parseValue(
        "{\"number_of_models\": 20}");
    JSONObject ensemble = api.createEnsemble(
        "dataset/5143a51a37203f2cf7000972", args);
```

If no ``number_of_models`` is provided, the ensemble will contain 10 models.

``Random Decision Forests`` fall also into the ``Decision Forest`` category, but they only use a subset of the fields chosen at random at each split. To create this kind of ensemble, just use the ``randomize`` option:

```
    JSONObject args = JSONValue.parseValue(
        "{\"number_of_models\": 20,
          \"randomize\": true}");
    JSONObject ensemble = api.createEnsemble(
        "dataset/5143a51a37203f2cf7000972", args);
```

The third kind of ensemble is ``Boosted Trees``. This type of ensemble uses quite a different algorithm. The trees used in the ensemble don't have as objective field the one you want to predict, and they don't aggregate the
underlying models' votes. Instead, the goal is adjusting the coefficients
of a function that will be used to predict. The models' objective is, therefore, the gradient that minimizes the error of the predicting function (when comparing its output with the real values). The process starts with
some initial values and computes these gradients. Next step uses the previous
fields plus the last computed gradient field as the new initial state for the next iteration. Finally, it stops when the error is smaller than a certain threshold or iterations reach a user-defined limit.
In classification problems, every category in the ensemble's objective field
would be associated with a subset of the ``Boosted Trees``. The objective of
each subset of trees is adjustig the function to the probability of belonging to this particular category.

In order to build an ensemble of ``Boosted Trees`` you need to provide the ``boosting`` attributes. You can learn about the existing attributes in the [ensembles' section of the API documentation](https://bigml.com/api/ensembles#es_gradient_boosting), but a typical attribute to be set would be the maximum number of iterations:

```
    args = {'boosting': {'iterations': 20}}
    ensemble = api.create_ensemble('dataset/5143a51a37203f2cf7000972', args)

    JSONObject args = JSONValue.parseValue(
        "{\"boosting\": {\"iterations\": 20}");
    JSONObject ensemble = api.createEnsemble(
        "dataset/5143a51a37203f2cf7000972", args);
```


### Linear regressions
------------

For regression problems, you can choose also linear regressions to model
your data. Linear regressions expect the predicted value for the objective
field to be computable as a linear combination of the predictions.

As the rest of models, linear regressions can be created from a dataset by
calling the corresponding create method:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my linear regression\",
          \"objective_field\": \"my_objective_field\"}");
    JSONObject linearRegression = api.createLinearRegression(
        "dataset/5143a51a37203f2cf7000972", args);
```

In this example, we created a linear regression named
``my linear regression`` and set the objective field to be
``my_objective_field``. Other arguments, like ``bias``,
can also be specified as attributes in arguments dictionary at
creation time.
Particularly for categorical fields, there are three different available
`field_codings`` options (``contrast``, ``other`` or the ``dummy``
default coding). For a more detailed description of the
``field_codings`` attribute and its syntax, please see the 
[Developers API Documentation](https://bigml.com/api/linearregressions#lr_linear_regression_arguments).


### Logistic regressions
------------

For classification problems, you can choose also logistic regressions to model
your data. Logistic regressions compute a probability associated to each class in the objective field. The probability is obtained using a logistic function, whose argument is a linear combination of the field values.

As the rest of models, logistic regressions can be created from a dataset by calling the corresponding create method:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my logistic regression\",
          \"objective_field\": \"my_objective_field\"}");
    JSONObject logisticRegression = api.createLogisticRegression(
        "dataset/5143a51a37203f2cf7000972", args);
```

In this example, we created a logistic regression named ``my logistic regression`` and set the objective field to be ``my_objective_field``. Other arguments, like ``bias``, ``missing_numerics`` and ``c`` can also be specified as attributes in arguments dictionary at creation time.
Particularly for categorical fields, there are four different available
`field_codings`` options (``dummy``, ``contrast``, ``other`` or the ``one-hot``
default coding). For a more detailed description of the ``field_codings`` attribute and its syntax, please see the [Developers API Documentation](https://bigml.com/api/logisticregressions#lr_logistic_regression_arguments).


### Deepnets
------------

Deepnets can also solve classification and regression problems. Deepnets are an optimized version of Deep Neural Networks, a class of machine-learned models inspired by the neural circuitry of the human brain. In these classifiers, the input features are fed to a group of "nodes" called a "layer".
Each node is essentially a function on the input that transforms the input features into another value or collection of values. Then the entire layer transforms an input vector into a new "intermediate" feature vector. This new vector is fed as input to another layer of nodes. This process continues layer by layer, until we reach the final "output" layer of nodes, where the output is the network’s prediction: an array of per-class probabilities for classification problems or a single, real value for regression problems.

Deepnets predictions compute a probability associated to each class in the objective field for classification problems. As the rest of models, deepnets can be created from a dataset by calling the corresponding create method:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my deepnet\",
          \"objective_field\": \"my_objective_field\"}");
    JSONObject deepnet = api.createDeepnet
        "dataset/5143a51a37203f2cf7000972", args);
```

In this example, we created a deepnet named ``my deepnet`` and set the objective field to be ``my_objective_field``. Other arguments, like ``number_of_hidden_layers``, ``learning_rate`` and ``missing_numerics`` can also be specified as attributes in an arguments dictionary at creation time. For a more detailed description of the available attributes and its syntax, please see the [Developers API Documentation](https://tropo.dev.bigml.com/api/deepnets#dn_deepnet_arguments).


### Batch predictions
------------

We have shown how to create predictions individually, but when the amount
of predictions to make increases, this procedure is far from optimal. In this
case, the more efficient way of predicting remotely is to create a dataset
containing the input data you want your model to predict from and to give its
id and the one of the model to the ``createBatchPrediction`` api call:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my batch prediction\",
          \"all_fields\": true,
          \"header\": true,
          \"confidence\": true}");
    JSONObject batchPrediction = api.createBatchPrediction(
        "model/5af06df94e17277501000010", 
        "dataset/5143a51a37203f2cf7000972", 
        args);
```

In this example, setting ``all_fields`` to true causes the input data to be included in the prediction output, ``header`` controls whether a headers line is included in the file or not and ``confidence`` set to true causes the confidence of the prediction to be appended. If none of these arguments is given, the resulting file will contain the name of the objective field as a header row followed by the predictions.

As for the rest of resources, the create method will return an incomplete object, that can be updated by issuing the corresponding ``api.getBatchPrediction`` call until it reaches a ``FINISHED`` status.
Then you can download the created predictions file using:

    api.downloadBatchPrediction(
        "batchprediction/526fc344035d071ea3031d70",
        "my_dir/my_predictions.csv");

that will copy the output predictions to the local file given in the second param. 

The output of a batch prediction can also be transformed to a source object
using the ``createSourceFromBatchPrediction`` method in the api:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my_batch_prediction_source\"}");
    api.createSourceFromBatchPrediction(
        "batchprediction/526fc344035d071ea3031d70", null, args);
```

This code will create a new source object, that can be used again as starting
point to generate datasets.


### Batch centroids
------------

As described in the previous section, it is also possible to make centroids' predictions in batch. First you create a dataset containing the input data you want your cluster to relate to a centroid. The ``createBatchCentroid`` call will need the id of the input data dataset and the cluster used to assign a centroid to each instance:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my batch centroid\",
          \"all_fields\": true,
          \"header\": true}");
    JSONObject batchCentroid = api.createBatchCrediction(
        "cluster/5af06df94e17277501000010", 
        "dataset/5143a51a37203f2cf7000972", 
        args);
```


### Batch anomaly scores
------------

Input data can also be assigned an anomaly score in batch. You train an anomaly detector with your training data and then build a dataset from your
input data. The ``createBatchAnomalyScore`` call will need the id of the dataset and of the anomaly detector to assign an anomaly score to each input data instance:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my batch anomaly score\",
          \"all_fields\": true,
          \"header\": true}");
    JSONObject batchAnomalyScore = api.createBatchAnomalyScore(
        "anomaly/5af06df94e17277501000010", 
        "dataset/5143a51a37203f2cf7000972", 
        args);
```


### Batch topic distributions
------------

Input data can also be assigned a topic distribution in batch. You train a
topic model with your training data and then build a dataset from your
input data. The ``createBatchTopicDistribution`` call will need the id
of the dataset and of the topic model to assign a topic distribution to each input data instance:

```
    JSONObject args = JSONValue.parseValue(
        "{\"name\": \"my batch topic distribution\",
          \"all_fields\": true,
          \"header\": true}");
    JSONObject batchTopicDistribution = api.createBatchTopicDistribution(
        "topicmodel/58362aaa983efc45a1000007", 
        "dataset/5143a51a37203f2cf7000972", 
        args);
```


Reading Resources
------------

When retrieved individually, resources are returned as a dictionary identical to the one you get when you create a new resource. However, the status code will be ``HTTP_OK`` if the resource can be retrieved without problems, or one of the HTTP standard error codes otherwise.


Listing Resources
------------

You can list resources with the appropriate api method:

    api.listSources(null);
    api.listDatasets(null);
    api.listModels(null);
    api.listPredictions(null);
    api.listEvaluations(null);
    api.listEnsembles(null);
    api.listBatchPredictions(null);
    api.listClusters(null);
    api.listCentroids(null);
    api.listBatchCentroids(null);
    api.listAnomalies(null);
    api.listAnomalyScores(null);
    api.listBatchAnomalyScores(null);
    api.listProjects(null);
    api.listSamples(null);
    api.listCorrelations(null);
    api.listStatisticalTests(null);
    api.listLogisticRegressions(null);
    api.listLinearRegressions(null);
    api.listAssociations(null);
    api.listAssociationSets(null);
    api.listTopicModels(null);
    api.listTopicDistributions(null);
    api.listBatchTopicDistributions(null);
    api.listTimeSeries(null);
    api.listForecasts(null);
    api.listDeepnets(null);
    api.listScripts(null);
    api.listLibraries(null);
    api.listExecutions(null);
    api.list_external_connectors();

you will receive a dictionary with the following keys:

-  **code**: If the request is successful you will get a
   ``HTTP_OK`` (200) status code. Otherwise, it will be one of
   the standard HTTP error codes. See [BigML documentation on status
   codes](https://bigml.com/api/status_codes) for more info.
-  **meta**: A dictionary including the following keys that can help you
   paginate listings:

   -  **previous**: Path to get the previous page or ``None`` if there
      is no previous page.
   -  **next**: Path to get the next page or ``None`` if there is no
      next page.
   -  **offset**: How far off from the first entry in the resources is
      the first one listed in the resources key.
   -  **limit**: Maximum number of resources that you will get listed in
      the resources key.
   -  **total\_count**: The total number of resources in BigML.

-  **objects**: A list of resources as returned by BigML.
-  **error**: If an error occurs and the resource cannot be created, it
   will contain an additional code and a description of the error. In
   this case, **meta**, and **resources** will be ``None``.


Filtering resources
------------

In order to filter resources you can use any of the properties labeled
as *filterable* in the [BigML documentation](https://bigml.com/api). Please, check the available properties for each kind of resource in their particular section. In addition to specific selectors you can use two general selectors to paginate the resources list: `offset` and `limit`. For details, please check [this requests section](https://bigml.com/api/requests#rq_paginating_resources).

A few examples:

First 5 sources created before April 1st, 2012
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    api.listSources("limit=5;created__lt=2012-04-1");


First 10 datasets bigger than 1MB
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    api.listDatasets("limit=10;size__gt=1048576");


Models with more than 5 fields (columns)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    api.listModels("columns__gt=5");


Predictions whose model has not been deleted
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    api.listPredictions("model_status=true");


Ordering Resources
------------

In order to order resources you can use any of the properties labeled as
*sortable* in the [BigML documentation](https://bigml.com/api). Please, check the sortable properties for each kind of resources in their particular section. By default BigML paginates the results in groups of 20, so it’s possible that you need to specify the `offset` or increase the `limit` of resources to returned in the list call. For details, please, check [this requests section](https://bigml.com/api/requests#rq_paginating_resources).

A few examples:

Sources ordered by size
^^^^^^^^^^^^^^^^^^^^^^^

    api.listSources("order_by=size");

Datasets created before April 1st, 2012 ordered by size
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    api.listDatasets("created__lt=2012-04-1;order_by=size");

Models ordered by number of predictions (in descending order).
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    
    api.listModels("order_by=-number_of_predictions");


Predictions ordered by name.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    
    api.listPredictions("order_by=name");


Updating Resources
------------

When you update a resource, it is returned in a dictionary exactly like
the one you get when you create a new one. However the status code will
be ``HTTP_ACCEPTED`` if the resource can be updated without problems or one of the HTTP standard error codes otherwise.

```
    JSONObjects args = new JSONObject();
    args.put("name", "new name");

    api.updateSource(source, args);
    api.updateDataset(dataset, args);
    api.updateModel(model, args);
    api.updatePrediction(prediction, args);
    api.updateEvaluation(evaluation, args);
    api.updateEnsemble(ensemble, args);
    api.updateBatchPrediction(batchPrediction, args);
    api.updateCluster(cluster, args);
    api.updateCentroid(centroid, args);
    api.updateBatchCentroid(batchCentroid, args);
    api.updateAnomaly(anomaly, args);
    api.updateAnomalyScore(anomalyScore, args);
    api.updateBatchAnomalyScore(batchAnomalyScore, args);
    api.updateProject(project, args);
    api.updateCorrelation(correlation, args);
    api.updateStatisticalTest(statisticalTest, args);
    api.updateLogisticRegression(logisticRegression, args);
    api.updateLinearcRegression(linearRegression, args);
    api.updateAssociation(association, args);
    api.updateAssociationSet(associationSet, args);
    api.updateTopicModel(topicModel, args);
    api.updateTopicDistribution(topicDistribution, args);
    api.updateBatchTopicDistribution(batchTopicDistribution, args);
    api.updateTimeSeries(timeSeries, args);
    api.updateForecast(forecast, args);
    api.updateDeepnet(deepnet, args);
    api.updateScript(script, args);
    api.updateLibrary(library, args);
    api.updateExecution(execution, args);
    api.updateExternalConnector(externalConnector, args)
```

Updates can change resource general properties, such as the ``name`` or
``description`` attributes of a dataset, or specific properties, like
the ``missing tokens`` (strings considered as missing values). As an example,
let's say that your source has a certain field whose contents are numeric integers. BigML will assign a numeric type to the field, but you might want it to be used as a categorical field. You could change its type to ``categorical`` by calling:

```
    JSONObject args = JSONValue.parseValue(
        "{\"fields\": {\"000001\": {\"optype\": \"categorical\"}}}");
    api.updateSource(source, args);
```

where ``000001`` is the field id that corresponds to the updated field.

Another usually needed update is changing a fields' ``non-preferred`` attribute, so that it can be used in the modeling process:

```
    JSONObject args = JSONValue.parseValue(
        "{\"fields\": {\"000001\": {\"preferred\": true}}}");
    api.updateDataset(dataset, args);
```

where you would be setting as ``preferred`` the field whose id is ``000001``.

You may also want to change the name of one of the clusters found in your clustering:

```
    JSONObject args = JSONValue.parseValue(
        "{\"clusters\": {\"000001\": {\"name\": \"my cluster\"}}}");
    api.updateCluster(cluster, args);
```

which is changing the name of the cluster whose centroid id is ``000001`` to
``my_cluster``. Or, similarly, changing the name of one detected topic:

```
    JSONObject args = JSONValue.parseValue(
        "{\"topics\": {\"000001\": {\"name\": \"my topic\"}}}");
    api.updateTopicModel(topicModel, args);
```

You will find detailed information about the updatable attributes of each resource in [BigML developer's documentation](https://bigml.com/api).


Deleting Resources
------------

Resources can be deleted individually using the corresponding method for
each type of resource.

```
    api.deleteSource(source);
    api.deleteDataset(dataset);
    api.deleteModel(model);
    api.deletePrediction(prediction);
    api.deleteEvaluation(evaluation);
    api.deleteEnsemble(ensemble);
    api.deleteBatchPrediction(batchPrediction);
    api.deleteCluster(cluster);
    api.deleteCentroid(centroid);
    api.deleteBatchCentroid(batchCentroid);
    api.deleteAnomaly(anomaly);
    api.deleteAnomalyScore(anomalyScore);
    api.deleteBatchAnomalyScore(batchAnomalyScore);
    api.deleteSample(sample);
    api.deleteCorrelation(correlation);
    api.deleteStatisticalTest(statisticalTest);
    api.deleteLogisticRegression(logisticRegression);
    api.deleteLinearRegression(linearRegression);
    api.deleteAssociation(association);
    api.deleteAssociationSet(associationSet);
    api.deleteTopicModel(topicModel);
    api.deleteTopicDistribution(topicDistribution);
    api.deleteBatchTopicDistribution(batchTopicDistribution);
    api.deleteTimeSeries(timeSeries);
    api.deleteForecast(forecast);
    api.deleteDeepnet(deepnet);
    api.deleteProject(project);
    api.deleteScript(script);
    api.deleteLibrary(library);
    api.deleteExecution(execution);
    api.deleteExternalConnector(externalConnector)
```

Each of the calls above will return a dictionary with the following keys:

-  **code** If the request is successful, the code will be a
   ``HTTP_NO_CONTENT`` (204) status code. Otherwise, it wil be
   one of the standard HTTP error codes. See the [documentation on
   status codes](https://bigml.com/api/status_codes) for more
   info.
-  **error** If the request does not succeed, it will contain a
   dictionary with an error code and a message. It will be ``None``
   otherwise.


Public and shared resources
------------

The previous examples use resources that were created by the same user that asks for their retrieval or modification. If a user wants to share one of her resources, she can make them public or share them. Declaring a resource public means that anyone can see the resource. This can be applied to datasets and models. To turn a dataset public, just update its ``private`` property:

```
    JSONObject args = JSONValue.parseValue(
        "{\"private\": false}");
    api.updateDataset("dataset/5143a51a37203f2cf7000972", args);
```

and any user will be able to download it using its id prepended by ``public``:

    api.getDataset("public/dataset/5143a51a37203f2cf7000972");

In the models' case, you can also choose if you want the model to be fully downloadable or just accesible to make predictions. This is controlled with the
``white_box`` property. If you want to publish your model completely, just use:

    JSONObject args = JSONValue.parseValue(
        "{\"private\": false, \"white_box\": true}");
    api.updateModel("model/5143a51a37203f2cf7000956"'", args);

Both public models and datasets, will be openly accessible for anyone, registered or not, from the web gallery.

Still, you may want to share your models with other users, but without making
them public for everyone. This can be achieved by setting the ``shared``
property:

```
    JSONObject args = JSONValue.parseValue(
        "{\"shared\": true}");
    api.updateModel("model/5143a51a37203f2cf7000956", args);
```

Shared models can be accessed using their share hash (propery ``shared_hash``
in the original model):

    api.getModel("shared/model/d53iw39euTdjsgesj7382ufhwnD");
