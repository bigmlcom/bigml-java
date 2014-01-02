BigML Sample project
====================

This is a simple sample project that shows the basics features of the javinp/bigml-java
bindings.

It is a Eclipse project with a simple class `org.bigml.sample.BigMLSampleClient`
with a basic predictive workflow using BigML's API:

  - Create a [Source](https://bigml.com/developers/sources)
  - Create a [Dataset](https://bigml.com/developers/datasets)
  - Create a [Model](https://bigml.com/developers/models)
  - Create a [Prediction](https://bigml.com/developers/prediction)

    - Create a [LocalPredictiveModel](https://github.com/javinp/bigml-java/blob/master/src/main/java/org/bigml/binding/LocalPredictiveModel.java)
    - Create a prediction using the `LocalPredictiveModel` instance

BigML API credentials
---------------------

You'll need to set your own API credentials in
`org.bigml.sample.BigMLSampleClient.BIGML_USERNAME` and
`org.bigml.sample.BigMLSampleClient.BIGML_API_KEY` in order to get an instance
of the `BigMLClient`. You can find your API Key at
https://bigml.com/account/apikey

Please, feel free to fork it and PR any improvement you may consider.

