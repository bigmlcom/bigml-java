Local Resources
=====================

All the resources in BigML can be saved in json format and used locally with no
connection whatsoever to BigML's servers. This is specially important
for all Supervised and Unsupervised models, that can be used to generate
predictions in any programmable device. The next sections describe how to
do that for each type of resource.

This json can be used just as the remote model to generate predictions. As you'll see in next section, the local ``Model`` object can be instantiated by giving json as first argument:

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalPredictiveModel;

    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

    // Get remote model
    JSONObject model = api.getModel("model/502fdbff15526876610002615");

    // Create local model
    LocalPredictiveModel localModel = new LocalPredictiVeModel(model);

    // Predict 
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localModel.predict(inputData);
```


Local Models
------------

You can instantiate a local version of a remote model.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalPredictiveModel;

    BigMLClient api = new BigMLClient();

    // Get remote model
    JSONObject model = api.getModel("model/502fdbff15526876610002615");

    // Create local model
    LocalPredictiveModel localModel = new LocalPredictiVeModel(model);
```

This will retrieve the remote model information, using an implicitly built
``BigML()`` connection object (see the ``Authentication`` section for more
details on how to set your credentials) and return a Model object
that you can use to make local predictions. 


### Local Predictions
------------

Once you have a local model you can use to generate predictions locally.

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localModel.predict(inputData);
```

Local predictions have three clear advantages:

- Removing the dependency from BigML to make new predictions.

- No cost (i.e., you do not spend BigML credits).

- Extremely low latency to generate predictions for huge volumes of data.

The default output for local predictions is the prediction itself, but you can
also add other properties associated to the prediction, like its confidence or probability, the distribution of values in the predicted node (for decision tree models), and the number of instances supporting the prediction. To obtain a dictionary with the prediction and the available additional properties use the ``full=True`` argument:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localModel.predict(inputData, null, null, null, true);
```

that will return:

```
{    
    "count": 47,
    "confidence": 0.92444,
    "probability": 0.9861111111111112,
    "prediction": "Iris-versicolor",
    "distribution_unit": "categories",
    "path": ["petal length > 2.45",
             "petal width <= 1.75",
             "petal length <= 4.95",
             "petal width <= 1.65"],
    "distribution": [["Iris-versicolor", 47]]
}
```

Note that the ``path`` attribute for the ``proportional`` missing strategy
shows the path leading to a final unique node, that gives the prediction, or
to the first split where a missing value is found. Other optional
attributes are ``next`` which contains the field that determines the next split after the prediction node and ``distribution`` that adds the distribution
that leads to the prediction. For regression models, ``min`` and
``max`` will add the limit values for the data that supports the
prediction.

When your test data has missing values, you can choose between ``last
prediction`` or ``proportional`` strategy to compute the
prediction. The ``last prediction`` strategy is the one used by
default. To compute a prediction, the algorithm goes down the model's
decision tree and checks the condition it finds at each node (e.g.:
'sepal length' > 2). If the field checked is missing in your input
data you have two options: by default (``last prediction`` strategy)
the algorithm will stop and issue the last prediction it computed in
the previous node. If you chose ``proportional`` strategy instead, the
algorithm will continue to go down the tree considering both branches
from that node on. Thus, it will store a list of possible predictions
from then on, one per valid node. In this case, the final prediction
will be the majority (for categorical models) or the average (for
regressions) of values predicted by the list of predicted values.

You can set this strategy by using the ``missingStrategy`` argument with code ``0`` to use ``last prediction`` and ``1`` for ``proportional``.

```
    import org.bigml.binding.MissingStrategy;
    
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localModel.predict(
        inputData, MissingStrategy.PROPORTIONAL, null, null, true);
```

For classification models, it is sometimes useful to obtain a
probability or confidence prediction for each possible class of the
objective field.  To do this, you can use the ``predictProbability``
and ``predictConfidence`` methods respectively.  The former gives a
prediction based on the distribution of instances at the appropriate
leaf node, with a Laplace correction based on the root node
distribution.  The latter returns a lower confidence bound on the leaf
node probability based on the Wilson score interval.

Each of these methods take the ``missingStrategy`` argument that functions as it does in ``predict``.  Note that these methods substitute the deprecated ``multiple`` parameter in the ``predict`` method functionallity.

So, for example, the following:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3}");
    localModel.predictProbability(inputData);
```

would result in

```
    [{"prediction": "Iris-setosa",
      "probability": 0.0033003300330033},
     {"prediction": "Iris-versicolor",
      "probability": 0.4983498349834984},
     {"prediction": "Iris-virginica",
      "probability": 0.4983498349834984}]
```

The output of ``predictConfidence`` is the same, except that the
output maps are keyed with ``confidence`` instead of ``probability``.

For classifications, the prediction of a local model will be one of the
available categories in the objective field and an associated ``confidence``
or ``probability`` that is used to decide which is the predicted category.
If you prefer the model predictions to be operated using any of them, you can
use the ``operatingKind`` argument in the ``predict`` method.
Here's the example to use predictions based on ``confidence``:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localModel.predict(inputData, null, null, "confidence", true, null);
```

Previous versions of the bindings had additional arguments in the ``predict``
method that were used to format the prediction attributes. The signature of
the method has been changed to accept only arguments that affect the
prediction itself, (like ``missingStrategy``, ``operatingKind`` and
``opreatingPoint``) and ``full`` which is a boolean that controls whether
the output is the prediction itself or a dictionary will all the available
properties associated to the prediction. 

```
    public Prediction predict(
            JSONObject inputData, MissingStrategy missingStrategy, 
            JSONObject operatingPoint, String operatingKind, Boolean full, 
            List<String> unusedFields) throws Exception {
        ...
    }
```


### Operating point's predictions
------------

In classification problems, Models, Ensembles and Logistic Regressions can be used at different operating points, that is, associated to particular thresholds. Each operating point is then defined by the kind of property you use as threshold, its value and a the class that is supposed to be predicted if the threshold is reached.

Let's assume you decide that you have a binary problem, with classes ``True``
and ``False`` as possible outcomes. Imagine you want to be very sure to
predict the `True` outcome, so you don't want to predict that unless the
probability associated to it is over ``0,8``. You can achieve this with any
classification model by creating an operating point:

```
    JSONObject operatingPoint = JSONValue.parseValue(
        "{\"kind length\": \"probability\", 
          \"positive_class width\": \"True\",
          \"threshold\": 0.8}");
```

to predict using this restriction, you can use the ``operatingPoint`` parameter:

```
    Prediction prediction = localModel.predict(
        inputData, null, operatingPoint, nul, true, null);
```

where ``inputData`` should contain the values for which you want to predict.
Local models allow two kinds of operating points: ``probability`` and ``confidence``. For both of them, the threshold can be set to any number in the ``[0, 1]`` range.


Local Clusters
------------

You can instantiate a local version of a remote cluster.
    
```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalCluster;

    BigMLClient api = new BigMLClient();

    // Get remote cluster
    JSONObject cluster = api.getCluster("cluster/502fdbff15526876610002435");

    // Create local cluster
    LocalCluster localCluster = new LocalCluster(cluster);
```

This will retrieve the remote cluster information, using an implicitly built
``BigML()`` connection object (see the ``Authentication`` section for more
details on how to set your credentials) and return a ``LocalCluster`` object
that you can use to make local centroid predictions. 

Local clusters provide also methods for the significant operations that
can be done using clusters: finding the centroid assigned to a certain data
point, sorting centroids according to their distance to a data point,
summarizing the centroids intra-distances and inter-distances and also finding the closest points to a given one. The [Local Centroids](#local-centroids)
and the [Summary generation](#summary-generation) sections will explain these methods.


### Local Centroids
------------

Using the local cluster object, you can predict the centroid associated to an input data set:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"pregnancies\": 0, \"plasma glucose\": 118, 
          \"blood pressure\": 84, \"triceps skin thickness\": 47, 
          \"insulin\": 230, \"bmi\": 45.8, 
          \"diabetes pedigree\": 0.551, \"age\": 31,
          \"diabetes\": \"true\"}");
    JSONObject centroid = localCluster.centroid(inputData);
```

that will return:
    
```
    {
        "distance": 0.454110207355, 
        "centroid_name": "Cluster 4",
        "centroid_id": "000004"
    }
```

You must keep in mind, though, that to obtain a centroid prediction, input data
must have values for all the numeric fields. No missing values for the numeric
fields are allowed unless you provided a ``default_numeric_value`` in the cluster construction configuration. If so, this value will be used to fill
the missing numeric fields.

As in the local model predictions, producing local centroids can be done independently of BigML servers, so no cost or connection latencies are involved.

Another interesting method in the cluster object is
``localCluster.closestInCluster``, which given a reference data point
will provide the rest of points that fall into the same cluster sorted
in an ascending order according to their distance to this point. You can limit
the maximum number of points returned by setting the ``numberOfPoints``
argument to any positive integer.

```
    JSONObject referencePoint = JSONValue.parseValue(
        "{\"pregnancies\": 0, \"plasma glucose\": 118, 
          \"blood pressure\": 84, \"triceps skin thickness\": 47, 
          \"insulin\": 230, \"bmi\": 45.8, 
          \"diabetes pedigree\": 0.551, \"age\": 31,
          \"diabetes\": \"true\"}");
    JSONObject point = localCluster.closestInCluster(inputData, 2, null);
```

The response will be a dictionary (JSONObject) with the centroid id of the cluster an the list of closest points and their distances to the reference point.

```
  {
    "closest": [
        {"distance": 0.06912270988567025,
           "data": {"plasma glucose": "115", "blood pressure": "70",
                    "triceps skin thickness": "30", "pregnancies": "1",
                    "bmi": "34.6", "diabetes pedigree": "0.529",
                    "insulin": "96", "age": "32", "diabetes": "true"}
        },
        {"distance": 0.10396456577958413,
           "data": {"plasma glucose": "167", "blood pressure": "74",
           "triceps skin thickness": "17", "pregnancies": "1", "bmi": "23.4",
           "diabetes pedigree": "0.447", "insulin": "144", "age": "33",
           "diabetes": "true"}
        }
    ],
    "reference": {
      "age": 31, "bmi": 45.8, "plasma glucose": 118,
      "insulin": 230, "blood pressure": 84,
      "pregnancies": 0, "triceps skin thickness": 47,
      "diabetes pedigree": 0.551, "diabetes": "true"},
    "centroid_id": "000000"
  }
```

No missing numeric values are allowed either in the reference data point.
If you want the data points to belong to a different cluster, you can
provide the ``centroid_id`` for the cluster as an additional argument.

Other utility methods are ``local_cluster.sortedCentroids`` which given a reference data point will provide the list of centroids sorted according
to the distance to it

```    JSONObject referencePoint = JSONValue.parseValue(
        "{\"pregnancies\": 1, \"plasma glucose\": 115, 
          \"blood pressure\": 70, \"triceps skin thickness\": 30, 
          \"insulin\": 96, \"bmi\": 34.6, 
          \"diabetes pedigree\": 0.529, \"age\": 32,
          \"diabetes\": \"true\"}");
    JSONObject sortedCentroids = localCluster.sortedCentroids(
      inputData, 2, null);
```

that will return:
 
```    
  {
      "centroids": [{"distance": 0.31656890408929705,
                      "data": {"000006": 0.34571, "000007": 30.7619,
                               "000000": 3.79592, "000008": "false"},
                      "centroid_id": "000000"},
                     {"distance": 0.4424198506958207,
                      "data": {"000006": 0.77087, "000007": 45.50943,
                               "000000": 5.90566, "000008": "true"},
                      "centroid_id": "000001"}],
      "reference": {"age": "32", "bmi": "34.6", "plasma glucose": "115",
                     "insulin": "96", "blood pressure": "70",
                     "pregnancies": "1", "triceps skin thickness": "30",
                     "diabetes pedigree": "0.529", "diabetes": "true"}
  }
```

or ``pointsInCluster`` that returns the list of data points assigned to a certain cluster, given its ``centroid_id``.

```
    JSONObject points = localCluster.pointsInCluster("000000");
```

Local AnomalyDetector
------------

You can also instantiate a local version of a remote anomaly.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalAnomaly;

    BigMLClient api = new BigMLClient();

    // Get remote anomaly
    JSONObject anomaly = api.getAnomalyDetector(
        "anomaly/502fcbff15526876610002435");

    // Create local anomaly detector
    LocalAnomaly localAnomaly = new LocalAnomaly(anomaly);
```

This will retrieve the remote anomaly detector information, using an implicitly
built ``BigML()`` connection object (see the ``Authentication`` section for
more details on how to set your credentials) and return an ``LocalAnomaly`` object that you can use to make local anomaly scores.

The anomaly detector object has also the method ``filter`` that will build the LISP filter you would need to filter the original dataset and create a new one excluding the top anomalies. Setting the ``include`` parameter to True you can do the inverse and create a dataset with only the most anomalous data points.


### Local Anomaly Scores
------------

Using the local anomaly detector object, you can predict the anomaly score
associated to an input data set:

```    
    JSONObject inputData = JSONValue.parseValue("{\"src_bytes\": 350}");
    double score = localAnomaly.score(inputData);
    
    0.9268527808726705
```

As in the local model predictions, producing local anomaly scores can be done
independently of BigML servers, so no cost or connection latencies are involved.


Local Logistic Regression
------------

You can also instantiate a local version of a remote logistic regression.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalLogisticRegression;

    BigMLClient api = new BigMLClient();

    // Get remote logistic regression
    JSONObject logistic = api.getLogisticRegression(
        "logisticregression/502fdbff15526876610042435");

    // Create local logistic regression
    LocalLogisticRegression localLogisticRegression = 
        new LocalLogisticRegression(logistic);
```

This will retrieve the remote logistic regression information, using an implicitly built ``BigML()`` connection object (see the ``Authentication`` section for more details on how to set your credentials) and return a ``LocalLogisticRegression`` object that you can use to make local predictions.


### Local Logistic Regression Predictions
------------

Using the local logistic regression object, you can predict the prediction for an input data set:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 2, \"sepal length\": 1.5, 
          \"petal width\": 0.5, \"sepal width\": 0.7}");
    localLogisticRegression.predict(inputData, null, null, true);
```

that will return:

```
  {
      "distribution": [
          {"category": "Iris-virginica", "probability": 0.5041444478857267},
          {"category": "Iris-versicolor", "probability": 0.46926542042788333},
          {"category": "Iris-setosa", "probability": 0.02659013168639014}
      ],
      "prediction": "Iris-virginica", 
      "probability": 0.5041444478857267
  }
```

As you can see, the prediction contains the predicted category and the
associated probability. It also shows the distribution of probabilities for
all the possible categories in the objective field. 

You must keep in mind, though, that to obtain a logistic regression prediction, input data must have values for all the numeric fields. No missing values for the numeric fields are allowed.

For consistency of interface with the ``LocalPredictiveModelModel`` class, logistic regressions again have a ``predictProbability`` method.  As stated above, missing values are not allowed, and so there is no ``missingStrategy`` argument.

Operating point predictions are also available for local logistic regressions
and an example of it would be:

```    
    JSONObject operatingPoint = JSONValue.parseValue(
        "{\"kind length\": \"probability\", 
          \"positive_class width\": \"True\",
          \"threshold\": 0.8}");
    localLogisticRegression.predict(inputData, operatingPoint, null, true);
```

You can check the [Operating point's predictions](#operating-point's-predictions) section to learn about operating points. For logistic regressions, the only available kind is ``probability``, that sets the threshold of probability to be reached for the prediction to be the positive class.


Local Deepnet
------------

You can also instantiate a local version of a remote Deepnet.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalDeepnet;

    BigMLClient api = new BigMLClient();

    // Get remote deepnet
    JSONObject deepnet = api.getDeepnet(
        "deepnet/502fdbff15526876610022435");

    // Create local deepnet
    LocalDeepnet localDeepnet = new LocalDeepnet(deepnet);
```

This will retrieve the remote deepnet information, using an implicitly built
``BigML()`` connection object (see the ``Authentication`` section for more
details on how to set your credentials) and return a ``LocalDeepnet``
object that you can use to make local predictions.

### Local Deepnet Predictions
------------

Using the local deepnet object, you can predict the prediction for an input data set:

    
```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 2, \"sepal length\": 1.5, 
          \"petal width\": 0.5, \"sepal width\": 0.7}");
    localDeepnet.predict(inputData, null, null, true);
```

that will return:

```
  {
      "distribution": [
        {"category": "Iris-virginica", "probability": 0.5041444478857267},
        {"category": "Iris-versicolor", "probability": 0.46926542042788333},
        {"category": "Iris-setosa", "probability": 0.02659013168639014}
      ],
      "prediction": "Iris-virginica", 
      "probability": 0.5041444478857267
  }
```

As you can see, the full prediction contains the predicted category and the
associated probability. It also shows the distribution of probabilities for
all the possible categories in the objective field. 

To be consistent with the ``LocalPredictiveModelModel`` class interface, deepnets have also a ``predictProbability`` method.

Operating point predictions are also available for local deepnets and an
example of it would be:

```
    JSONObject operatingPoint = JSONValue.parseValue(
        "{\"kind length\": \"probability\", 
          \"positive_class width\": \"True\",
          \"threshold\": 0.8}");
    localDeepnet.predict(inputData, operatingpoint, null, true);
```
    

Local Fusion
------------

You can also instantiate a local version of a remote Fusion.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalFusion;

    BigMLClient api = new BigMLClient();

    // Get remote fusion
    JSONObject fusion = api.getFusion(
        "fusion/502fdbff15526876610022438");

    // Create local fusion
    LocalFusion localFusion = new LocalFusion(fusion);
```

This will retrieve the remote deepnet information, using an implicitly built
``BigML()`` connection object (see the ``Authentication`` section for more
details on how to set your credentials) and return a ``LocalFusion``
object that you can use to make local predictions.


### Local Fusion Predictions
------------

Using the local fusion object, you can predict the prediction for an input data set:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 2, \"sepal length\": 1.5, 
          \"petal width\": 0.5, \"sepal width\": 0.7}");
    localFusion.predict(inputData, null, null, true);
```

that will return:

```
  {
      "prediction": "Iris-setosa", 
      "probability": 0.45224
  }
```

As you can see, the full prediction contains the predicted category and the
associated probability. 

To be consistent with the ``ocalPredictiveModel`` class interface, fusions
have also a ``predict_probability`` method.

Operating point predictions are also available with probability as threshold for local fusions and an example of it would be:

```
    JSONObject operatingPoint = JSONValue.parseValue(
        "{\"kind length\": \"probability\", 
          \"positive_class width\": \"True\",
          \"threshold\": 0.8}");
    localFusion.predict(inputData, operatingpoint, null, true);
```


Local Association
------------

You can also instantiate a local version of a remote association resource.
    
```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalAssociation;

    BigMLClient api = new BigMLClient();

    // Get remote association
    JSONObject association = api.getAssociation(
        "association/502fdcff15526876610002435");

    // Create local association
    LocalAssociation localAssociation = new LocalAssociation(association);
```

This will retrieve the remote association information, using an implicitly
built ``BigML()`` connection object (see the ``Authentication`` section for more details on how to set your credentials) and return an ``LocalAssociation`` object that you can use to extract the rules found in the original dataset.

The created ``LocalAssociation`` object has some methods to help retrieving the
association rules found in the original data. The ``rules`` method will return the association rules. Arguments can be set to filter the rules returned according to its ``leverage``, ``strength``, ``support``, ``p_value``, a list of items involved in the rule or a user-given filter function.

```
    List itemList = new ArrayList();
    itemList.add("Edible");
    localAssociation.rules(null, null, 0.3, itemList, null);
```

In this example, the only rules that will be returned by the ``rules`` method will be the ones that mention ``Edible`` and their ``p_value`` is greater or equal to ``0.3``.

The rules can also be stored in a CSV file using ``rulesCsv``:

```
    List itemList = new ArrayList();
    itemList.add("Edible");
    localAssociation.rulesCsv(
        "/tmp/my_rules.csv", null, null, 0.3, itemList, null);
```

This example will store the rules whose strength is bigger or equal to 0.1 in the ``/tmp/my_rules.csv`` file.

You can also obtain the list of ``items`` parsed in the dataset using the
``items`` method. You can also filter the results by field name, by item names and by a user-given function:
  
```
    List names = new ArrayList();
    names.add("Brown cap");
    names.add("White cap");
    names.add("Yellow cap");
    localAssociation.items("Cap Color", names, null, null);
```

This will recover the ``Item`` objects found in the ``Cap Color`` field for the names in the list, with their properties as described in the [developers section](https://bigml.com/api/associations#ad_retrieving_an_association).


### Local Association Sets
------------

Using the local association object, you can predict the association sets related to an input data set:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"gender\": \"Female\", \"genres\": \"Adventure$Action\", 
          \"timestamp\": 993906291, \"occupation\": \"K-12 student\",
          \"zipcode\": 59583, \"rating\": 3}");
    localAssociation.associationSet(inputData, null, null);
```

that returns

```
  [
      {"item": {"complement": False,
                 "count": 70,
                 "field_id": "000002",
                 "name": "Under 18"},
        "rules": ["000000"],
        "score": 0.0969181441561211},
      {"item": {"complement": False,
                 "count": 216,
                 "field_id": "000007",
                 "name": "Drama"},
        "score": 0.025050115102862636},
      {"item": {"complement": False,
                 "count": 108,
                 "field_id": "000007",
                 "name": "Sci-Fi"},
        "rules": ["000003"],
        "score": 0.02384578264599424},
      {"item": {"complement": False,
                 "count": 40,
                 "field_id": "000002",
                 "name": "56+"},
        "rules": ["000008",
                  "000020"],
        "score": 0.021845366022721312},
      {"item": {"complement": False,
                 "count": 66,
                 "field_id": "000002",
                 "name": "45-49"},
        "rules": ["00000e"],
        "score": 0.019657155185835006}
  ]
```

As in the local model predictions, producing local association sets can be done
independently of BigML servers, so no cost or connection latencies are involved.


Local Topic Model
------------

You can also instantiate a local version of a remote topic model.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalTopicModel;

    BigMLClient api = new BigMLClient();

    // Get remote topicModel
    JSONObject topicModel = api.getTopicModel(
        "topicmodel/502fdbcf15526876210042435");

    // Create local topicModel
    LocalTopicModel localTopicModel = new LocalTopicModel(topicModel);
```

This will retrieve the remote topic model information, using an implicitly built ``BigML()`` connection object (see the ``Authentication`` section for more details on how to set your credentials) and return a ``LocalTopicModel``
object that you can use to obtain local topic distributions.


### Local Topic Distributions
------------

Using the local topic model object, you can predict the local topic distribution for an input data set:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"Message\": \"Our mobile phone is free\"}");
    localTopicModel.distribution(inputData);
```

that returns

```
  [   
      {"name": "Topic 00", "probability": 0.002627154266498529},
      {"name": "Topic 01", "probability": 0.003257671290458176},
      {"name": "Topic 02", "probability": 0.002627154266498529},
      {"name": "Topic 03", "probability": 0.1968263976460698},
      {"name": "Topic 04", "probability": 0.002627154266498529},
      {"name": "Topic 05", "probability": 0.002627154266498529},
      {"name": "Topic 06", "probability": 0.13692728036990331},
      {"name": "Topic 07", "probability": 0.6419714165615805},
      {"name": "Topic 08", "probability": 0.002627154266498529},
      {"name": "Topic 09", "probability": 0.002627154266498529},
      {"name": "Topic 10", "probability": 0.002627154266498529},
      {"name": "Topic 11", "probability": 0.002627154266498529}
  ]
```

As you can see, the topic distribution contains the name of the possible topics in the model and the associated probabilities.


Local Time Series
------------

You can also instantiate a local version of a remote time series.
  
```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalTimeSeries;

    BigMLClient api = new BigMLClient();

    // Get remote timeSeries
    JSONObject timeSeries = api.getTimeSeries(
        "timeseries/502fdbcf15526876210042435");

    // Create local timeSeries
    LocalTimeSeries localTimeSeries = new LocalTimeSeries(timeSeries);
```

This will create a series of models from the remote time series information,
using an implicitly built ``BigML()`` connection object (see the ``Authentication`` section for more details on how to set your credentials) and return a ``LocalTimeSeries`` object that you can use to obtain local forecasts.


### Local Forecasts
------------

Using the local time series object, you can forecast any of the objective field values:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"Final\": {\"horizon\": 5},
          \"Assignment\": {\"horizon\": 10, \"ets_models\": {\"criterion\": \"aic\", \"limit\": 2}}}");
    localTimeSeries.forecast(inputData);
```

that returns

```
  {
    "000005": [
        {"point_forecast": [68.53181, 68.53181, 68.53181, 68.53181, 68.53181],
         "model": "A,N,N"}],
     "000001": [{"point_forecast": [54.776650000000004, 90.00943000000001,
                                     83.59285000000001, 85.72403000000001,
                                     72.87196, 93.85872, 84.80786, 84.65522,
                                     92.52545, 88.78403],
                  "model": "A,N,A"},
                 {"point_forecast": [55.882820120000005, 90.5255466567616,
                                     83.44908577909621, 87.64524353046498,
                                     74.32914583152592, 95.12372848262932,
                                     86.69298716626228, 85.31630744944385,
                                     93.62385478607113, 89.06905451921818],
                  "model": "A,Ad,A"}]
  }
```

As you can see, the forecast contains the ID of the forecasted field, the
computed points and the name of the models meeting the criterion.
For more details about the available parameters, please check the [API
documentation](https://bigml.com/api/forecasts).


Multi Models
------------

Multi Models use a numbers of BigML remote models to build a local version that can be used to generate predictions locally. Predictions are generated
combining the outputs of each model.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.MultiModel;

    BigMLClient api = new BigMLClient();

    JSONArray models = (JSONArray) api.listModels(
        ";tags__in=my_tag").get("objects");

    MultiModel multiModel = new MultiModel(models, null, null);
```

This will create a multi model using all the models that have been previously
tagged with ``my_tag`` and predict by combining each model's prediction.
The combination method used by default is ``plurality`` for categorical
predictions and mean value for numerical ones. You can also use ``confidence
weighted``:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    multiModel.predict(inputData, null, PredictionMethod.PLURALITY, null);
```

that will weight each vote using the confidence/error given by the model
to each prediction, or even ``probability weighted``:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    multiModel.predict(inputData, null, PredictionMethod.PROBABILITY, null);
```

that weights each vote by using the probability associated to the training
distribution at the prediction node.

There's also a ``threshold`` method that uses an additional set of options:
threshold and category. The category is predicted if and only if
the number of predictions for that category is at least the threshold value.
Otherwise, the prediction is plurality for the rest of predicted values.

An example of ``threshold`` combination method would be:

```
    Map options = new HashMap();
    options.put("threshold", 3);
    options.put("category", "Iris-virginica");
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 0.9, \"petal width\": 1}");
    multiModel.predict(inputData, null, PredictionMethod.THRESHOLD, options);
```

When making predictions on a test set with a large number of models,
``batch_predict`` can be useful to log each model's predictions in a
separated file. It expects a list of input data values and the directory path
to save the prediction files in.

```
    JSONArray inputDataList = JSONValue.parseValue(
        "[{\"petal length\": 3, \"petal width\": 1},
          {\"petal length\": 3, \"petal width\": 5.1}]");
    multiModel.batchPredict(inputDataList, "data/predictions");
```

The predictions generated for each model will be stored in an output
file in `data/predictions` using the syntax `model_[id of the model]__predictions.csv`. For instance, when using `model/50c0de043b563519830001c2` to predict, the output file name will be
`model_50c0de043b563519830001c2__predictions.csv`. An additional feature is
that using ``reuse=True`` as argument will force the function to skip the
creation of the file if it already exists. This can be helpful when using repeatedly a bunch of models on the same test set.

```
    JSONArray inputDataList = JSONValue.parseValue(
        "[{\"petal length\": 3, \"petal width\": 1},
          {\"petal length\": 3, \"petal width\": 5.1}]");
    multiModel.batchPredict(
        inputDataList, "data/predictions", true, null, null, null, null);
```

Prediction files can be subsequently retrieved and converted into a votes list
using ``batchVotes``:

```
    List<MultiVote> batchVotes = multiModel.batchVotes(
        "data/predictions", null);
```

which will return a list of MultiVote objects. Each MultiVote contains a list of predictions, e.g. 

```
  [
    {"prediction": "Iris-versicolor", "confidence": 0.34, "order": 0}, {"prediction": "Iris-setosa", "confidence": 0.25, "order": 1}
  ]
```

These votes can be further combined to issue a final prediction for each input data element using the method ``combine``

```
    for (MultiVote multiVote: batchVotes) {
        HashMap<Object, Object> prediction = multivote.combine();
    }
```

Again, the default method of combination is ``plurality`` for categorical
predictions and mean value for numerical ones. You can also use ``confidence
weighted``:

```
    HashMap<Object, Object> prediction = multivote.combine(
        PredictionMethod.CONFIDENCE, null);
```

or ``probability weighted``:

```
    HashMap<Object, Object> prediction = multivote.combine(
        PredictionMethod.PROBABILITY, null);
```

For classification, the confidence associated to the combined prediction
is derived by first selecting the model's predictions that voted for the
resulting prediction and computing the weighted average of their individual
confidence. Nevertheless, when ``probability weighted`` is used,
the confidence is obtained by using each model's distribution at the
prediction node to build a probability distribution and combining them.
The confidence is then computed as the wilson score interval of the
combined distribution (using as total number of instances the sum of all
the model's distributions original instances at the prediction node)

In regression, all the models predictions' confidences contribute
to the weighted average confidence.


## Local Ensembles
------------

You can also instantiate a local version of a remote ensemble resource.

```
    import org.bigml.binding.BigMLClient;
    import org.bigml.binding.LocalEnsemble;

    BigMLClient api = new BigMLClient();

    // Get remote ensemble
    JSONObject ensemble = api.getEnsemble(
        "ensemble/5143a51a37203f2cf7020351");

    // Create local ensemble
    LocalEnsemble localEnsemble = new LocalEnsemble(ensemble);
```

The local ensemble object can be used to manage the three types of ensembles: ``Decision Forests`` (bagging or random) and the ones using ``Boosted Trees``.

The ``operatingKind`` argument overrides the legacy ``method`` argument, which
was previously used to define the combiner for the models predictions.

Similarly, local ensembles can also be created by giving a list of models to be
combined to issue the final prediction (note: only random decision forests and
bagging ensembles can be built using this method):

```
    import org.bigml.binding.LocalEnsemble;
    List models = new ArrayList();
    models.add("model/50c0de043b563519830001c2");
    models.add("model/50c0de043b5635198300031b");
    LocalEnsemble localEnsemble = new LocalEnsemble(models, 10);
```

Note: the ensemble JSON structure is not self-contained, meaning that it
contains references to the models that the ensemble is build of, but not the information of the models themselves.
To use an ensemble locally with no connection to the internet, you must make sure that not only a local copy of the ensemble JSON file is available in your computer, but also the JSON files corresponding to the models in it. This is automatically achieved when you use the ``LocalEnsemble(ensemble)`` constructor, that fetches all the related JSON files and stores them in an ``./storage`` directory. Next calls to ``Ensemble(ensemble)`` will retrieve the
files from this local storage, so that internet connection will only be needed
the first time an ``LocalEnsemble`` is built.

On the contrary, if you have no memory limitations and want to increase prediction speed, you can create the ensemble from a list of local model
objects. Then, local model objects will only be instantiated once, and
this could increase performance for large ensembles.


## Local Ensemble's Predictions
------------

As in the local model's case, you can use the local ensemble to create new predictions for your test data, and set some arguments to configure the final output of the ``predict`` method.

The predictions' structure will vary depending on the kind of ensemble used. For ``Decision Forests`` local predictions will just contain the ensemble's final prediction if no other argument is used.

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localEnsemble.predict(inputData, null, null, null, null, false)
```

returns

    Iris-versicolor

The final prediction of an ensemble is determined by aggregating or selecting the predictions of the individual models therein. For classifications, the most probable class is returned if no especial operating method is set. Using ``full=True`` you can see both the predicted output and the associated probability:

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localEnsemble.predict(inputData, null, null, null, null, null, true, null)
```

returns

```
  {
      "prediction": "Iris-versicolor",
      "probability": 0.98566
  }
```

In general, the prediction in a classification will be one amongst the list of categories in the objective field. When each model in the ensemble is used to predict, each category has a confidence, a probability or a vote associated to this prediction.
Then, through the collection of models in the ensemble, each category gets an averaged confidence, probabiity and number of votes. Thus you can decide whether to operate the ensemble using the ``confidence``, the ``probability`` or the ``votes`` so that the predicted category is the one that scores higher in any of these quantities. The criteria can be set using the `operatingKind` option (default is set to ``probability``):

```
    JSONObject inputData = JSONValue.parseValue(
        "{\"petal length\": 3, \"petal width\": 1}");
    localEnsemble.predict(
        inputData, null, null, null, null, "votes", true, null);
```

Regression will generate a predictiona and an associated error, however ``Boosted Trees`` don't have an associated confidence measure, so only the prediction will be obtained in this case.

For consistency of interface with the ``LocalPredictiveModelModel`` class, as well as between boosted and non-boosted ensembles, local Ensembles again have a ``predictProbability`` method.  This takes the same optional
arguments as ``LocalPredictiveModelModel.predict``: ``missingStrategy``. 

Operating point predictions are also available for local ensembles and an
example of it would be:

```
    JSONObject operatingPoint = JSONValue.parseValue(
        "{\"kind length\": \"probability\", 
          \"positive_class width\": \"True\",
          \"threshold\": 0.8}");
    localEnsemble.predict(
        inputData, null, null, null, operatingPoint, null, true, null)
```

You can check the [Operating point's predictions](#operating-point's-predictions) section to learn about operating points. For ensembles, three kinds of operating points are available: ``votes``, ``probability`` and ``confidence``. ``Votes`` will use as threshold the number of models in the ensemble that vote for the positive class. The other two are already explained in the above mentioned section.


## Rule Generation
---------------

You can also use a local predictive model to generate a IF-THEN rule set that can be very helpful to understand how the model works internally.

```     
      localModel.rules();
    
      IF petal_length > 2.45 AND
         IF petal_width > 1.75 AND
             IF petal_length > 4.85 THEN
                 species = Iris-virginica
             IF petal_length <= 4.85 AND
                 IF sepal_width > 3.1 THEN
                     species = Iris-versicolor
                 IF sepal_width <= 3.1 THEN
                     species = Iris-virginica
         IF petal_width <= 1.75 AND
             IF petal_length > 4.95 AND
                 IF petal_width > 1.55 AND
                     IF petal_length > 5.45 THEN
                         species = Iris-virginica
                     IF petal_length <= 5.45 THEN
                         species = Iris-versicolor
                 IF petal_width <= 1.55 THEN
                     species = Iris-virginica
             IF petal_length <= 4.95 AND
                 IF petal_width > 1.65 THEN
                     species = Iris-virginica
                 IF petal_width <= 1.65 THEN
                     species = Iris-versicolor
     IF petal_length <= 2.45 THEN
         species = Iris-setosa
```


## Summary generation
------------------

You can also print the model from the point of view of the classes it predicts
with ``localModel.summarize()``. It shows a header section with the training data initial distribution per class (instances and percentage) and the final predicted distribution per class.

Then each class distribution is detailed. First a header section shows the percentage of the total data that belongs to the class (in the training set and in the predicted results) and the rules applicable to all the the instances of that class (if any). Just after that, a detail section shows each of the leaves in which the class members are distributed.
They are sorted in descending order by the percentage of predictions of the class that fall into that leaf and also show the full rule chain that leads to it.

```
    Data distribution:
        Iris-setosa: 33.33% (50 instances)
        Iris-versicolor: 33.33% (50 instances)
        Iris-virginica: 33.33% (50 instances)

    Predicted distribution:
        Iris-setosa: 33.33% (50 instances)
        Iris-versicolor: 33.33% (50 instances)
        Iris-virginica: 33.33% (50 instances)

    Field importance:
        1. petal length: 53.16%
        2. petal width: 46.33%
        3. sepal length: 0.51%
        4. sepal width: 0.00%

    Iris-setosa : (data 33.33% / prediction 33.33%) petal length <= 2.45
        · 100.00%: petal length <= 2.45 [Confidence: 92.86%]

    Iris-versicolor : (data 33.33% / prediction 33.33%) petal length > 2.45
        · 94.00%: petal length > 2.45 and petal width <= 1.65 and petal length <= 4.95 [Confidence: 92.44%]
        · 2.00%: petal length > 2.45 and petal width <= 1.65 and petal length > 4.95 and sepal length <= 6.05 and petal width > 1.55 [Confidence: 20.65%]
        · 2.00%: petal length > 2.45 and petal width > 1.65 and petal length <= 5.05 and sepal width > 2.9 and sepal length > 6.4 [Confidence: 20.65%]
        · 2.00%: petal length > 2.45 and petal width > 1.65 and petal length <= 5.05 and sepal width > 2.9 and sepal length <= 6.4 and sepal length <= 5.95 [Confidence: 20.65%]

    Iris-virginica : (data 33.33% / prediction 33.33%) petal length > 2.45
        · 76.00%: petal length > 2.45 and petal width > 1.65 and petal length > 5.05 [Confidence: 90.82%]
        · 12.00%: petal length > 2.45 and petal width > 1.65 and petal length <= 5.05 and sepal width <= 2.9 [Confidence: 60.97%]
        · 6.00%: petal length > 2.45 and petal width <= 1.65 and petal length > 4.95 and sepal length > 6.05 [Confidence: 43.85%]
        · 4.00%: petal length > 2.45 and petal width > 1.65 and petal length <= 5.05 and sepal width > 2.9 and sepal length <= 6.4 and sepal length > 5.95 [Confidence: 34.24%]
        · 2.00%: petal length > 2.45 and petal width <= 1.65 and petal length > 4.95 and sepal length <= 6.05 and petal width <= 1.55 [Confidence: 20.65%]
```


You can also use ``localModel.getDataDistribution()`` and ``local_model.getPredictionDistribution()`` to obtain the training and
prediction basic distribution information as a list (suitable to draw histograms or any further processing).
The tree nodes' information (prediction, confidence, impurity and distribution)
can also be retrieved in a CSV format using the method ``localModel.exportTreeCSV()``. The output can be sent to a file by providing a
``outputFilePath`` argument or used as a list.

Local ensembles have a ``localEnsemble.summarize()`` method too, the output
in this case shows only the data distribution (only available in
``Decision Forests``) and field importance sections.

For local clusters, the ``localCluster.summarize()`` method prints also the
data distribution, the training data statistics per cluster and the basic
intercentroid distance statistics. There's also a ``localCluster.statisticsCsv(file_name)`` method that store in a CSV format the values shown by the ``summarize()`` method. If no file name is provided, the function returns the rows that would have been stored in the file as a list.

