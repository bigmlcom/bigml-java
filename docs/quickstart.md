
Quick Start
=====================

This chapter shows how to create a model from a remote CSV file and use it to make a prediction for a new single instance.

Imagine that you want to use [this csv file](https://static.bigml.com/csv/iris.csv) containing the [Iris flower dataset](http://en.wikipedia.org/wiki/Iris_flower_data_set) to predict the species of a flower whose ``sepal length``  is ``5`` and whose ``sepal width`` is ``2.5``. A preview of the dataset is shown below. It has 4 numeric fields: ``sepal length``, ``sepal width``, ``petal length``, ``petal width`` and a categorical field: ``species``.
By default, BigML considers the last field in the dataset as the objective field (i.e., the field that you want to generate predictions for).

    sepal length,sepal width,petal length,petal width,species
    5.1,3.5,1.4,0.2,Iris-setosa
    4.9,3.0,1.4,0.2,Iris-setosa
    4.7,3.2,1.3,0.2,Iris-setosa
    ...
    5.8,2.7,3.9,1.2,Iris-versicolor
    6.0,2.7,5.1,1.6,Iris-versicolor
    5.4,3.0,4.5,1.5,Iris-versicolor
    ...
    6.8,3.0,5.5,2.1,Iris-virginica
    5.7,2.5,5.0,2.0,Iris-virginica
    5.8,2.8,5.1,2.4,Iris-virginica


The typical process you need to follow when using BigML is to:

1.  open a connection to BigML API with your user name and API Key

2.  create a **source** by uploading the data file

3.  create a **dataset** (a structured version of the source)

4.  create a **model** using the dataset

5.  finally, use the model to make a **prediction** for some new input data.

As you can see, all the steps above share some similarities, in that each one consists of creating a new BigML resource from some other BigML resource. This makes the BigML API very easy to understand and use, since all available operations are orthogonal to the kind of resource you want to create.

All API calls in BigML are asynchronous, so you will not be blocking your program while waiting for the network to send back a reply. This means that at each step you need to wait for the resource creation to finish before you can move on to the next step.

This can be exemplified with the first step in our process, creating a **source** by uploading the data file.

First of all, you need to create the connecting to BigML:

    import org.bigml.binding.BigMLClient;

    // Create BigMLClient with the properties in binding.properties
    BigMLClient api = new BigMLClient();

You will need to create then a `Source` object to encapsulate all information that will be used to create it correctly, i.e., an optional name for the source and the data file to use:

    JSONObject args = null;
    JSONObject source = api.createRemoteSource(
        "https://static.bigml.com/csv/iris.csv",
        "Iris Source", args);

If you do not want to use a remote data file, as you are doing in this example, you can use a local data file by replacing the last line above, as shown here:


    JSONObject args = null;
    JSONObject source = api.createSource(
        "./data/iris.csv", "Iris Source", args);

That’s all! BigML will create the source, as per our request, and automatically list it in the BigML Dashboard. As mentioned, though, you will need to monitor the source status until it is fully created before you can move on to the next step, which can be easily done like this:

    while (!api.sourceIsReady(source)) 
        Thread.sleep(1000);

The steps described above define a generic pattern of how to create the resources you need next, i.e., a `Dataset`, a `Model`, and a `Prediction`. As an additional example, this is how you create a `Dataset` from the `Source` you have just created:

``` 
  // --- create a dataset from the previous source ---
  // Dataset object which will encapsulate the dataset information
  JSONObject args = null;
  args.put("name", "my new dataset");

  JSONObject dataset = api.createDataset(
        (String)source.get("resource"), args, null, null);

  while (!api.datasetIsReady(dataset)) 
        Thread.sleep(1000);
```

You can easily complete the crreation of a prediction following these steps:

```
    JSONObject model = api.createModel(
        (String)dataset.get("resource"), args, null, null);

    while (!api.modelIsReady(model)) 
        Thread.sleep(1000);

    JSONObject inputData = new JSONObject();
    inputData.put("sepal length", 5);
    inputData.put("sepal width", 2.5);

    JSONObject prediction = api.createPrediction(
        (String)model.get("resource"), inputData, true,
        args, null, null);
```

After this quick introduction, it should be now easy to follow and understand the full code that is required to create a prediction starting from a data file. Make sure you have properly installed BigML Java bindings as detailed in [Requirements](intro.html#requirements).

You can then get the prediction result:

    prediction = api.getPrediction(prediction);

and print the result:

    String output = (String)Utils.getJSONObject(
        prediction, "object.output");
    System.out.println("Prediction result: " + output);

    Prediction result: Iris-virginica

and also generate an evaluation for the model by using:

```    JSONObject testSource = api.createSource("./data/test_iris.csv",
        "Test Iris Source", args);

    while (!api.sourceIsReady(source)) Thread.sleep(1000);

    JSONObject testDataset = api.createDataset(
        (String)testSource.get("resource"), args, null, null);

    while (!api.datasetIsReady(dataset)) Thread.sleep(1000);

    JSONObject evaluation = api.createEvaluation(
        (String)model.get("resource"), (String)dataset.get("resource"),
        args, null, null);
```

Setting the ``storage`` argument in the api client instantiation:


    BigMLClient api = new BigMLClient(
        "myusername", "ae579e7e53fb9abd646a6ff8aa99d4afe83ac291", "./storage");

all the generated, updated or retrieved resources will be automatically
saved to the chosen directory.

You can also find a sample API client code from [here](https://github.com/bigmlcom/bigml-java/blob/master/samples/BigML-Sample-Client/src/main/java/org/bigml/sample/BigMLSampleClient.java).
