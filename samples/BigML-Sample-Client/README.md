BigML Sample project
====================

This is a sample project that shows the basic features of the
bigmlcom/bigml-java bindings.

It is a Eclipse project with a simple class
`org.bigml.sample.BigMLSampleClient` that shows how to create resources and
local predictions using these bindings.

The examples include:

A basic predictive workflow using BigML's API:

  - Create a [Source](https://bigml.com/api/sources)
  - Create a [Dataset](https://bigml.com/api/datasets)
  - Create a [Model](https://bigml.com/api/models)
  - Create a [Prediction](https://bigml.com/api/prediction)

Creatinge local predictions by downloading the contents
of the remote `Model` JSON object.

  - Create a [LocalPredictiveModel](https://github.com/bigmlcom/bigml-java/blob/master/src/main/java/org/bigml/binding/LocalPredictiveModel.java)
  - Create a prediction using the `LocalPredictiveModel` instance

Evaluating your model by sampling your existing dataset:

  - Create a training sampled [Model](https://bigml.com/api/models)
  - Create an [Evaluation](https://bigml.com/api/evaluations) with the out of
    bag test data

Creating some unsupervised models:

  -  Create a [Cluster](https://bigml.com/api/clusters)
  -  Create an [Anomaly](https://bigml.com/api/anomalies)

The workflow needed to create Topic Distributions:

  -  Create a [Topic Model](https://bigml.com/api/topicmodels)
  -  Create a [Topic Distribution](https://bigml.com/api/topicdistributions)

How to change the properties of the fields in your
`Source` or `Dataset`.

How to create a `WhizzML Script` and execute
it for some `inputs` by creating an `Execution`.

BigML API credentials
---------------------

You'll need to set your own API credentials in
`src/main/resources/binding.properties`. The instance of the `BigMLClient`
will use these credentials to authenticate your calls to the API. The file
should contain the constants below, that define the domain that
the calls will point to, and the user authentication information

``` bash
    BIGML_URL=https://bigml.io/andromeda/
    BIGML_DEV_URL=https://bigml.io/dev/andromeda/
    BIGML_USERNAME=
    BIGML_API_KEY=
```

You can find your API Key at https://bigml.com/account/apikey
