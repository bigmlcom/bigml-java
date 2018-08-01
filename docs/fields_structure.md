
Fields Structure
=====================

Source
-------

BigML automatically generates identifiers for each field. The following example shows how to retrieve the fields, ids, and its types that have been assigned to a source:

    source = api.getSource(source);
    JSONObject fields = (JSONObject) Utils.getJSONObject(
        source, "object.fields");

source ``fields`` object:

```
{
    "000000":{
        "name":"sepal length",
        "column_number":0,
        "optype":"numeric",
        "order":0
    },
    "000001":{
        "name":"sepal width",
        "column_number":1,
        "optype":"numeric",
        "order":1
    },
    "000002":{
        "name":"petal length",
        "column_number":2,
        "optype":"numeric",
        "order":2
    },
    "000003":{
        "name":"petal width",
        "column_number":3,
        "optype":"numeric",
        "order":3
    },
    "000004":{
        "column_number":4,
        "name":"species",
        "optype":"categorical",
        "order":4,
        "term_analysis":{
            "enabled":true
        }
    }
}
```

When the number of fields becomes very large, it can be useful to exclude or filter them. This can be done using a query string expression, for instance:

    source = api.getSource(source, "limit=10&order_by=name");

would include in the retrieved dictionary the first 10 fields sorted by name.


Dataset
-------

If you want to get some basic statistics for each field you can retrieve the ``fields`` from the dataset as follows to get a dictionary keyed by field id:


    dataset = api.getDataset(dataset);
    JSONOoject fields = (JSONObject) Utils.getJSONObject(
        dataset, "object.fields");

dataset ``fields`` object:

```
{
    "000000": {
        "column_number": 0,
        "datatype": "double",
        "name": "sepal length",
        "optype": "numeric",
        "order": 0,
        "preferred": true,
        "summary": {
            "bins": [
                [4.3, 1],
                [4.425, 4],

                ...snip...

                [7.9, 1]
            ],
            "kurtosis": -0.57357,
            "maximum": 7.9,
            "mean": 5.84333,
            "median": 5.8,
            "minimum": 4.3,
            "missing_count": 0,
            "population": 150,
            "skewness": 0.31175,
            "splits": [
                4.51526,
                4.67252,

                ...snip...

                7.64746
            ],
            "standard_deviation": 0.82807,
            "sum": 876.5,
            "sum_squares": 5223.85,
            "variance": 0.68569
        }
    },

    ...snip...

    "000004": {

        ...snip...

    }
}
```

The field filtering options are also available using a query string expression,
for instance:

    dataset = api.getDataset(dataset, "limit=20");

limits the number of fields that will be included in ``dataset`` to 20.


Model
-----

One of the greatest things about BigML is that the models that it generates for you are fully white-boxed. To get the explicit tree-like predictive model for the example above:

    model = api.getModel(model);
    JSONObject tree = (JSONObject) Utils.getJSONObject(
        model, "object.model.root");


model ``tree`` object:


```
{
    "children":[{
        "children":[{
            "children":[{
                "confidence":0.91799,
                "count":43,
                "id":3,
                "objective_summary":{
                    "categories":[
                        [
                            "Iris-virginica",
                            43
                        ]
                    ]
                },
                "output":"Iris-virginica",
                "predicate":{
                    "field":"000002",
                    "operator":">",
                    "value":4.85
                }
            }, {
                "children":[{
                    "confidence":0.20654,
                    "count":1,
                    "id":5,
                    "objective_summary":{
                        "categories":[
                            [
                                "Iris-versicolor",
                                1
                            ]
                        ]
                    },
                    "output":"Iris-versicolor",
                    "predicate":{
                        "field":"000001",
                        "operator":">",
                        "value":3.1
                    }
                },

                ...snip...

            },

            ...snip...

        },

        ...snip...

    },

    ...snip...
}
```

(Note that we have abbreviated the output in the snippet above for readability: the full predictive model yo'll get is going to contain
much more details).

Again, filtering options are also available using a query string expression,
for instance:

    model = api.getModel(model, "limit=5");

limits the number of fields that will be included in ``model`` to 5.


Evaluation
----------

The predictive performance of a model can be measured using many different
measures. In BigML these measures can be obtained by creating evaluations. To
create an evaluation you need the id of the model you are evaluating and the id
of the dataset that contains the data to be tested with. The result is shown
as:

    evaluation = api.getEvaluation(evaluation);
    JSONObject result = (JSONObject) Utils.getJSONObject(evaluation, "object.result");

evaluation ``result`` object:

```
{
    "class_names":[
        "Iris-setosa",
        "Iris-versicolor",
        "Iris-virginica"
    ],
    "mode":{
        "accuracy":0.33333,
        "average_f_measure":0.16667,
        "average_phi":0,
        "average_precision":0.11111,
        "average_recall":0.33333,
        "confusion_matrix":[
            [50, 0, 0],
            [50, 0, 0],
            [50, 0, 0]
        ],
        "per_class_statistics":[
            {
                "accuracy":0.3333333333333333,
                "class_name":"Iris-setosa",
                "f_measure":0.5,
                "phi_coefficient":0,
                "precision":0.3333333333333333,
                "present_in_test_data":true,
                "recall":1.0
            },
            {
                "accuracy":0.6666666666666667,
                "class_name":"Iris-versicolor",
                "f_measure":0,
                "phi_coefficient":0,
                "precision":0,
                "present_in_test_data":true,
                "recall":0.0
            },
            {
                "accuracy":0.6666666666666667,
                "class_name":"Iris-virginica",
                "f_measure":0,
                "phi_coefficient":0,
                "precision":0,
                "present_in_test_data":true,
                "recall":0.0
            }
        ]
    },
    "model":{
        "accuracy":1,
        "average_f_measure":1,
        "average_phi":1,
        "average_precision":1,
        "average_recall":1,
        "confusion_matrix":[
            [50, 0, 0],
            [0, 50, 0],
            [0, 0, 50]
        ],
        "per_class_statistics":[
            {
                "accuracy":1.0,
                "class_name":"Iris-setosa",
                "f_measure":1.0,
                "phi_coefficient":1.0,
                "precision":1.0,
                "present_in_test_data":true,
                "recall":1.0
            },
            {
                "accuracy":1.0,
                "class_name":"Iris-versicolor",
                "f_measure":1.0,
                "phi_coefficient":1.0,
                "precision":1.0,
                "present_in_test_data":true,
                "recall":1.0
            },
            {
                "accuracy":1.0,
                "class_name":"Iris-virginica",
                "f_measure":1.0,
                "phi_coefficient":1.0,
                "precision":1.0,
                "present_in_test_data":true,
                "recall":1.0
            }
        ]
    },
    "random":{
        "accuracy":0.28,
        "average_f_measure":0.27789,
        "average_phi":-0.08123,
        "average_precision":0.27683,
        "average_recall":0.28,
        "confusion_matrix":[
            [14, 19, 17],
            [19, 10, 21],
            [15, 17, 18]
        ],
        "per_class_statistics":[
            {
                "accuracy":0.5333333333333333,
                "class_name":"Iris-setosa",
                "f_measure":0.2857142857142857,
                "phi_coefficient":-0.06063390625908324,
                "precision":0.2916666666666667,
                "present_in_test_data":true,
                "recall":0.28
            },
            {
                "accuracy":0.4933333333333333,
                "class_name":"Iris-versicolor",
                "f_measure":0.20833333333333331,
                "phi_coefficient":-0.16357216402190614,
                "precision":0.21739130434782608,
                "present_in_test_data":true,
                "recall":0.2
            },
            {
                "accuracy":0.5333333333333333,
                "class_name":"Iris-virginica",
                "f_measure":0.33962264150943394,
                "phi_coefficient":-0.019492029389636262,
                "precision":0.32142857142857145,
                "present_in_test_data":true,
                "recall":0.36
            }
        ]
    }
}
```

where two levels of detail are easily identified. For classifications, the first level shows these keys:

-  **class_names**: A list with the names of all the categories for the objective field (i.e., all the classes)
-  **mode**: A detailed result object. Measures of the performance of the classifier that predicts the mode class for all the instances in the dataset
-  **model**: A detailed result object.
-  **random**: A detailed result object.  Measures the performance of the classifier that predicts a random class for all the instances in the dataset.

and the detailed result objects include ``accuracy``, ``average_f_measure``, 
``average_phi``, ``average_precision``, ``average_recall``, ``confusion_matrix`` and ``per_class_statistics``.

For regressions first level will contain these keys:

-  **mean**: A detailed result object. Measures the performance of the model that predicts the mean for all the instances in the dataset.
-  **model**: A detailed result object.
-  **random**: A detailed result object. Measures the performance of the model that predicts a random class for all the instances in the dataset.

where the detailed result objects include ``mean_absolute_error``,
``mean_squared_error`` and ``r_squared`` (refer to
[developers documentation](https://bigml.com/developers/evaluations) for
more info on the meaning of these measures.


Cluster
-------

For unsupervised learning problems, the cluster is used to classify in a
limited number of groups your training data. The cluster structure is defined
by the centers of each group of data, named centroids, and the data enclosed
in the group. As for in the model's case, the cluster is a white-box resource
and can be retrieved as a JSON:


    cluster = api.getCluster("cluster/56c42ea47e0a8d6cca0151a0");
    JSONObject result = (JSONObject) Utils.getJSONObject(cluster, "object");

cluster ``object`` object:

```
{
    "balance_fields":true,
    "category":0,
    "cluster_datasets":{},
    "cluster_models":{},
    "clusters":{
        "clusters":[{
            "center":{
                "000000":6.262,
                "000001":2.872,
                "000002":4.906,
                "000003":1.676,
                "000004":"Iris-virginica"
            },
            "count":100,
            "distance":{
                "bins":[
                    [0.03935, 1],
                    [0.04828, 1],
                    [0.06093, 1 ],
                    ...snip...
                    [0.47935, 1]
                ],
                "maximum":0.47935,
                "mean":0.21705,
                "median":0.20954,
                "minimum":0.03935,
                "population":100,
                "standard_deviation":0.0886,
                "sum":21.70515,
                "sum_squares":5.48833,
                "variance":0.00785
            },
            "id":"000000",
            "name":"Cluster 0"
        }, {
            "center":{
                "000000":5.006,
                "000001":3.428,
                "000002":1.462,
                "000003":0.246,
                "000004":"Iris-setosa"
            },
            "count":50,
            "distance":{
                "bins":[
                    [0.01427, 1],
                    [0.02279, 1],
                    ...snip...
                    [0.41736, 1]
                ],
                "maximum":0.41736,
                "mean":0.12717,
                "median":0.113,
                "minimum":0.01427,
                "population":50,
                "standard_deviation":0.08521,
                "sum":6.3584,
                "sum_squares":1.16432,
                "variance":0.00726
            },
            "id":"000001",
            "name":"Cluster 1"
        }],
        "fields":{
            ...snip...
        }
    },
    "code":200,
    "columns":5,
    "created":"2016-02-17T08:26:12.583000",
    "credits":0.017581939697265625,
    "credits_per_prediction":0.0,
    "critical_value":5,
    "dataset":"dataset/56c42ea07e0a8d6cca01519b",
    "dataset_field_types":{
        "categorical":1,
        "datetime":0,
        "effective_fields":5,
        "items":0,
        "numeric":4,
        "preferred":5,
        "text":0,
        "total":5
    },
    "dataset_status":true,
    "dataset_type":0,
    "description":"",
    "excluded_fields":[],
    "field_scales":{},
    "fields_meta":{
        "count":5,
        "limit":1000,
        "offset":0,
        "query_total":5,
        "total":5
    },
    "input_fields":[
        "000000",
        "000001",
        "000002",
        "000003",
        "000004"
    ],
    "k":2,
    "locale":"en_US",
    "max_columns":5,
    "max_rows":150,
    "model_clusters":false,
    "name":"Iris Source dataset's cluster",
    "number_of_batchcentroids":0,
    "number_of_centroids":0,
    "number_of_public_centroids":0,
    "out_of_bag":false,
    "price":0.0,
    "private":true,
    "project":null,
    "range":[
        1,
        150
    ],
    "replacement":false,
    "resource":"cluster/56c42ea47e0a8d6cca0151a0",
    "rows":150,
    "sample_rate":1.0,
    "scales":{
        "000000":0.18941532079904913,
        "000001":0.35975000221609077,
        "000002":0.08884141152890178,
        "000003":0.20571391803576422,
        "000004":0.15627934742019414
    },
    "shared":false,
    "size":4609,
    "source":"source/56c42e9f8a318f66df007548",
    "source_status":true,
    "status":{
        "code":5,
        "elapsed":1213,
        "message":"The cluster has been created",
        "progress":1.0
    },
    "subscription":false,
    "summary_fields":[],
    "tags":[],
    "updated":"2016-02-17T08:26:24.259000",
    "white_box":false
}
```

(Note that we have abbreviated the output in the snippet above for readability: the full predictive cluster yo'll get is going to contain
much more details).

Anomaly Detector
----------------

For anomaly detection problems, BigML uses iforest as an unsupervised
kind of model that detects anomalous data in a dataset. The information
it returns encloses a ``top_anomalies`` block that contains a list of
the most anomalous points. For each, we capture a ``score`` from 0 to 1.
The closer to 1, the more anomalous. We also capture the ``row`` which gives
values for each field in the order defined by ``input_fields``. Similarly
we give a list of ``importances`` which match the ``row`` values. These
importances tell us which values contributed most to the anomaly
score. Thus, the structure of an anomaly detector is similar to:

    anomaly = api.getAnomaly("anomaly/56c432728a318f66e4012f82");
    JSONObject object = (JSONObject) Utils.getJSONObject(anomaly, "object");

anomaly ``object`` object:


```
{
    "anomaly_seed":"2c249dda00fbf54ab4cdd850532a584f286af5b6",
    "category":0,
    "code":200,
    "columns":5,
    "constraints":false,
    "created":"2016-02-17T08:42:26.663000",
    "credits":0.12307357788085938,
    "credits_per_prediction":0.0,
    "dataset":"dataset/56c432657e0a8d6cd0004a2d",
    "dataset_field_types":{
        "categorical":1,
        "datetime":0,
        "effective_fields":5,
        "items":0,
        "numeric":4,
        "preferred":5,
        "text":0,
        "total":5
    },
    "dataset_status":true,
    "dataset_type":0,
    "description":"",
    "excluded_fields":[],
    "fields_meta":{
        "count":5,
        "limit":1000,
        "offset":0,
        "query_total":5,
        "total":5
    },
    "forest_size":128,
    "id_fields":[],
    "input_fields":[
        "000000",
        "000001",
        "000002",
        "000003",
        "000004"
    ],
    "locale":"en_US",
    "max_columns":5,
    "max_rows":150,
    "model":{
        "constraints":false,
        "fields":{
            ...snip...
        },
        "forest_size":128,
        "kind":"iforest",
        "mean_depth":9.557347074468085,
        "sample_size":94,
        "top_anomalies":[{
            "importance":[
                0.22808,
                0.23051,
                0.21026,
                0.1756,
                0.15555
            ],
            "row":[
                7.9,
                3.8,
                6.4,
                2.0,
                "Iris-virginica"
            ],
            "row_number":131,
            "score":0.58766
        },
        {
            "importance":[
                0.21552,
                0.22631,
                0.22319,
                0.1826,
                0.15239
            ],
            "row":[
                7.7,
                3.8,
                6.7,
                2.2,
                "Iris-virginica"
            ],
            "row_number":117,
            "score":0.58458
        },
        ...snip...
        {
            "importance":[
                0.23113,
                0.15013,
                0.17312,
                0.20304,
                0.24257
            ],
            "row":[
                4.9,
                2.5,
                4.5,
                1.7,
                "Iris-virginica"
            ],
            "row_number":106,
            "score":0.54096
        }],
        "top_n":10,
        "trees":[{
            "root":{
                "children":[{
                    "children":[{
                        "children":[{
                            "children":[{
                                "children":[{
                                    "population":1,
                                    "predicates":[{
                                        "field":"00001f",
                                        "op":">",
                                        "value":35.54357
                                    }]
                                }, {
                                ...snip...
                                }, {
                                    "population":1,
                                    "predicates":[{
                                        "field":"00001f",
                                        "op":"<=",
                                        "value":35.54357
                                    }]
                                }],
                                "population":2,
                                "predicates":[{
                                    "field":"000005",
                                    "op":"<=",
                                    "value":1385.5166
                                }]
                            }],
                            "population":3,
                            "predicates":[{
                                "field":"000020",
                                "op":"<=",
                                "value":65.14308
                            }, {
                                "field":"000019",
                                "op":"=",
                                "value":0
                            }]
                        }],
                        ...snip...
                        "population":105,
                        "predicates":[{
                            "field":"000017",
                            "op":"<=",
                            "value":13.21754
                        }, {
                            "field":"000009",
                            "op":"in",
                            "value":["0"]
                        }]
                    }],
                    "population":126,
                    "predicates":[true, {
                        "field":"000018",
                        "op":"=",
                        "value":0
                    }]
                },
            },
            "training_mean_depth":11.071428571428571
        }
    },
    "name":"Iris Source dataset's anomaly detector",
    "number_of_anomalyscores":0,
    "number_of_batchanomalyscores":0,
    "number_of_public_anomalyscores":0,
    "ordering":0,
    "out_of_bag":false,
    "price":0.0,
    "private":true,
    "project":null,
    "range":[
        1,
        150
    ],
    "replacement":false,
    "resource":"anomaly/56c432728a318f66e4012f82",
    "rows":150,
    "sample_rate":1.0,
    "sample_size":94,
    "shared":false,
    "size":4609,
    "source":"source/56c432638a318f66e4012f7b",
    "source_status":true,
    "status":{
        "code":5,
        "elapsed":617,
        "message":"The anomaly detector has been created",
        "progress":1.0
    },
    "subscription":false,
    "tags":[],
    "top_n":10,
    "updated":"2016-02-17T08:42:42.238000",
    "white_box":false
}
```

(Note that we have abbreviated the output in the snippet above for
readability: the full anomaly detector yo'll get is going to contain
much more details).

The ``trees`` list contains the actual isolation forest, and it can be quite
large usually. That's why, this part of the resource should only be included
in downloads when needed. Each node in an isolation tree can have multiple predicates.
For the node to be a valid branch when evaluated with a data point, all of its
predicates must be true.


Samples
-------

To provide quick access to your row data you can create a ``sample``. Samples
are in-memory objects that can be queried for subsets of data by limiting
their size, the fields or the rows returned. The structure of a sample would
be::

Samples are not permanent objects. Once they are created, they will be
available as long as GETs are requested within periods smaller than
a pre-established TTL (Time to Live). The expiration timer of a sample is
reset every time a new GET is received.

If requested, a sample can also perform linear regression and compute
Pearson's and Spearman's correlations for either one numeric field
against all other numeric fields or between two specific numeric fields.


Correlations
------------

A ``correlation`` resource contains a series of computations that reflect the
degree of dependence between the field set as objective for your predictions
and the rest of fields in your dataset. The dependence degree is obtained by
comparing the distributions in every objective and non-objective field pair,
as independent fields should have probabilistic
independent distributions. Depending on the types of the fields to compare,
the metrics used to compute the correlation degree will be:

- for numeric to numeric pairs:
  [Pearson's](https://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient)
  and [Spearman's correlation](https://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient) coefficients.
- for numeric to categorical pairs:
  [One-way Analysis of Variance](https://en.wikipedia.org/wiki/One-way_analysis_of_variance), with the categorical field as the predictor variable.
- for categorical to categorical pairs:
  [contingency table (or two-way table)](https://en.wikipedia.org/wiki/Contingency_table),
  [Chi-square test of independence](https://en.wikipedia.org/wiki/Pearson%27s_chi-squared_test)
  , and [Cramer's V](https://en.wikipedia.org/wiki/Cram%C3%A9r%27s_V)
  and [Tschuprow's T](https://en.wikipedia.org/wiki/Tschuprow%27s_T)coefficients.

An example of the correlation resource JSON structure is:

    JSONObject correlation = 
        api.getCorrelation("correlation/55b7c4e99841fa24f20009bf");
    JSONObject object = (JSONObject) Utils.getJSONObject(
        correlation, "object");

correlation ``object`` object:

```
{   
    "category": 0,
    "clones": 0,
    "code": 200,
    "columns": 5,
    "correlations": {   
        "correlations": [   
            {   
                "name": "one_way_anova",
                "result": {
                        "000000": {
                            "eta_square": 0.61871,
                            "f_ratio": 119.2645,
                            "p_value": 0,
                            "significant": [True,
                                True,
                                True
                            ]
                        },
                        "000001": {
                            "eta_square": 0.40078,
                            "f_ratio": 49.16004,
                            "p_value": 0,
                            "significant": [True,
                                True,
                                True
                            ]
                        },
                        "000002": {
                            "eta_square": 0.94137,
                            "f_ratio": 1180.16118,
                            "p_value": 0,
                            "significant": [True,
                                True,
                                True
                            ]
                        },
                        "000003": {
                            "eta_square": 0.92888,
                            "f_ratio": 960.00715,
                            "p_value": 0,
                            "significant": [True,
                                True,
                                True
                            ]
                        }
                    },
                }],
               "fields": {   
                    "000000": {   
                        "column_number": 0,
                        "datatype": "double",
                        "idx": 0,
                        "name": "sepal length",
                        "optype": "numeric",
                        "order": 0,
                        "preferred": True,
                        "summary": {   
                            "bins": [[4.3,1], [4.425,4], ..., [7.9,1]],
                            "kurtosis": -0.57357,
                            "maximum": 7.9,
                            "mean": 5.84333,
                            "median": 5.8,
                            "minimum": 4.3,
                            "missing_count": 0,
                            "population": 150,
                            "skewness": 0.31175,
                            "splits': [4.51526, 4.67252, 4.81113, 4.89582, 4.96139, 5.01131, ..., 6.92597, 7.20423, 7.64746],
                            "standard_deviation": 0.82807,
                            "sum": 876.5,
                            "sum_squares": 5223.85,
                            "variance": 0.68569
                        }
                    },
                    "000001": {   
                        "column_number": 1,
                        "datatype": 'double',
                        "idx": 1,
                        "name": "sepal width",
                        "optype": "numeric",
                        "order": 1,
                        "preferred": True,
                        "summary": {   
                            'counts': [[2,1], [2.2,
                        ...
                    },
                    ....
                    "000004": {   
                        "column_number': 4,
                        "datatype": '"string'",
                        "idx": 4,
                        "name": "species",
                        "optype": "categorical",
                        "order": 4,
                        "preferred": True,
                        "summary": {   
                            "categories": [["Iris-setosa", 50], 
                                           ["Iris-versicolor",50],
                                           ["Iris-virginica", 50]],
                            "missing_count": 0
                        },
                        "term_analysis": {"enabled": True}
                    }
                },
            "significance_levels": [0.01, 0.05, 0.1]
    },
    "created": "2015-07-28T18:07:37.010000",
    "credits": 0.017581939697265625,
    "dataset": "dataset/55b7a6749841fa2500000d41",
    "dataset_status": True,
    "dataset_type": 0,
    "description": "",
    "excluded_fields": [],
    "fields_meta": {   
        "count": 5,
        "limit": 1000,
        "offset": 0,
        "query_total": 5,
        "total": 5},
    "input_fields": ["000000", "000001", "000002", "000003"],
    'locale": "en_US",
    "max_columns": 5,
    "max_rows": 150,
    "name": u"iris' dataset correlation",
    "objective_field_details": {   
        "column_number": 4,
        "datatype": "string",
        "name": "species",
        "optype": "categorical",
        "order": 4
    },
    "out_of_bag": False,
    "price": 0.0,
    "private": True,
    "project": None,
    "range": [1, 150],
    "replacement": False,
    "resource": "correlation/55b7c4e99841fa24f20009bf",
    "rows": 150,
    "sample_rate": 1.0,
    "shared": False,
    "size": 4609,
    "source": "source/55b7a6729841fa24f100036a",
    "source_status": True,
    "status": {   
        "code": 5,
        "elapsed": 274,
        "message": "The correlation has been created",
        "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "updated": "2015-07-28T18:07:49.057000",
    "white_box": False
}
```

Note that the output in the snippet above has been abbreviated. As you see, the
``correlations`` attribute contains the information about each field correlation to the objective field.


Statistical Tests
-----------------

A ``statisticaltest`` resource contains a series of tests that compare the
distribution of data in each numeric field of a dataset to certain canonical distributions, such as the [normal distribution](https://en.wikipedia.org/wiki/Normal_distribution) or [Benford's law](https://en.wikipedia.org/wiki/Benford%27s_law) distribution. Statistical test are useful in tasks such as fraud, normality, or outlier detection.

- Fraud Detection Tests:
Benford: This statistical test performs a comparison of the distribution of
first significant digits (FSDs) of each value of the field to the Benford's
law distribution. Benford's law applies to numerical distributions spanning
several orders of magnitude, such as the values found on financial balance
sheets. It states that the frequency distribution of leading, or first
significant digits (FSD) in such distributions is not uniform.
On the contrary, lower digits like 1 and 2 occur disproportionately
often as leading significant digits. The test compares the distribution
in the field to Bendford's distribution using a Chi-square goodness-of-fit
test, and Cho-Gaines d test. If a field has a dissimilar distribution,
it may contain anomalous or fraudulent values.

- Normality tests:
These tests can be used to confirm the assumption that the data in each field
of a dataset is distributed according to a normal distribution. The results
are relevant because many statistical and machine learning techniques rely on
this assumption.
Anderson-Darling: The Anderson-Darling test computes a test statistic based on
the difference between the observed cumulative distribution function (CDF) to
that of a normal distribution. A significant result indicates that the
assumption of normality is rejected.
Jarque-Bera: The Jarque-Bera test computes a test statistic based on the third
and fourth central moments (skewness and kurtosis) of the data. Again, a
significant result indicates that the normality assumption is rejected.
Z-score: For a given sample size, the maximum deviation from the mean that
would expected in a sampling of a normal distribution can be computed based
on the 68-95-99.7 rule. This test simply reports this expected deviation and
the actual deviation observed in the data, as a sort of sanity check.

- Outlier tests:
Grubbs: When the values of a field are normally distributed, a few values may
still deviate from the mean distribution. The outlier tests reports whether
at least one value in each numeric field differs significantly from the mean
using Grubb's test for outliers. If an outlier is found, then its value will
be returned.

An example of the statisticaltest resource JSON structure is:

    JSONObject statisticalTest = api.getStatisticalTest("statisticaltest/55b7c7089841fa25000010ad");
    JSONObject object = (JSONObject) Utils.getJSONObject(
        statisticalTest, "object");

statisticalTest ``object`` object:


```
{   
    "category": 0,
    "clones": 0,
    "code": 200,
    "columns": 5,
    "created": "2015-07-28T18:16:40.582000",
    "credits": 0.017581939697265625,
    "dataset": "dataset/55b7a6749841fa2500000d41",
    "dataset_status": True,
    "dataset_type": 0,
    "description": "",
    "excluded_fields": [],
    "fields_meta": {   
      "count": 5,
      "limit": 1000,
      "offset": 0,
      "query_total": 5,
      "total": 5
    },
    "input_fields": ["000000", "000001", "000002", "000003"],
    "locale": "en_US",
    "max_columns": 5,
    "max_rows": 150,
    "name": u"iris" dataset test",
    "out_of_bag": False,
    "price": 0.0,
    "private": True,
    "project": None,
    "range": [1, 150],
    "replacement": False,
    "resource": "statisticaltest/55b7c7089841fa25000010ad",
    "rows": 150,
    "sample_rate": 1.0,
    "shared": False,
    "size": 4609,
    "source": "source/55b7a6729841fa24f100036a",
    "source_status": True,
    "status": {   
      "code": 5,
      "elapsed": 302,
      "message": "The test has been created",
      "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "statistical_tests": {   
      "ad_sample_size": 1024,
      "fields": {   
          "000000": {   
              "column_number": 0,
              "datatype": "double",
              "idx": 0,
              "name": "sepal length",
              "optype": "numeric",
              "order": 0,
              "preferred": True,
              "summary": {   
                  "bins": [[4.3,1], [4.425,4], ..., [7.9, 1]],
                  "kurtosis": -0.57357,
                  "maximum": 7.9,
                  "mean": 5.84333,
                  "median": 5.8,
                  "minimum": 4.3,
                  "missing_count": 0,
                  "population": 150,
                  "skewness": 0.31175,
                  "splits": [4.51526, 4.67252, 4.81113, 4.89582, ..., 7.20423, 7.64746],
                  "standard_deviation": 0.82807,
                  "sum": 876.5,
                  "sum_squares": 5223.85,
                  "variance": 0.68569
                }  
            },
            ...
            "000004": {   
                "column_number": 4,
                "datatype": "string",
                "idx": 4,
                "name": "species",
                "optype": "categorical",
                "order": 4,
                "preferred": True,
                "summary": {   
                    "categories": [ ["Iris-setosa", 50],
                                    ["Iris-versicolor", 50],
                                    ["Iris-virginica", 50]],
                    "missing_count": 0
                },
                "term_analysis": {"enabled": True}
            }
        },      
        "fraud": [
          {
            "name": "benford",
            "result": {   
                "000000": {   
                    "chi_square": {   
                        "chi_square_value": 506.39302,
                        "p_value": 0,
                        "significant": [ True, True, True ]
                    },
                    "cho_gaines": { 
                        "d_statistic": 7.124311073683573,
                        "significant": [ True, True, True ]
                    },
                    "distribution": [ 0, 0, 0, 22, 61, 54, 13, 0, 0],
                    "negatives": 0,
                    "zeros": 0
                },
                 "000001": {   
                    "chi_square": {   
                        "chi_square_value": 396.76556,
                        "p_value": 0,
                        "significant": [ True, True, True ]
                    },
                    "cho_gaines": {   
                      "d_statistic": 7.503503138331123,
                      "significant": [ True, True, True ]
                    },
                    "distribution": [ 0, 57, 89, 4, 0, 0, 0, 0, 0],
                    "negatives": 0,
                    "zeros": 0
                },
                .....                                   
            }
        }
      ],
      "normality": [
          {
            "name": "anderson_darling",
            "result": {
                "000000": {
                    "p_value": 0.02252,
                    "significant": [False, True, True]
                },
                 "000001": {
                    "p_value": 0.02023,
                    "significant": [False, True, True]
                },
                 "000002": {
                    "p_value": 0,
                    "significant": [True, True, True]
                },
                 "000003": {
                    "p_value": 0,
                    "significant": [True, True, True]
                }
            }
          },
          {   
            "name": "jarque_bera",
            "result": {
              "000000": {
                "p_value": 0.10615,
                "significant": [False, False, False]
              },
               "000001": {
                  "p_value": 0.25957,
                  "significant": [False, False, False]
              },
               "000002": {
                  "p_value": 0.0009,
                  "significant": [True, True, True]
              },
               "000003": {
                  "p_value": 0.00332,
                  "significant": [True, True, True]}
              }
          },
          {   
            "name": "z_score",
            "result": { 
                "000000": {   
                    "expected_max_z": 2.71305,
                    "max_z": 2.48369
                },
                "000001": {
                  "expected_max_z": 2.71305,
                  "max_z": 3.08044
                },
                "000002": {
                  "expected_max_z": 2.71305,
                  "max_z": 1.77987
                },
                "000003": {
                  "expected_max_z": 2.71305,
                  "max_z": 1.70638
                }
            }
          }
        ],        
        "outliers": [
          {   
             "name": "grubbs",
             "result": {
                "000000": {
                    "p_value": 1,
                    "significant": [False, False, False]
                },
                "000001": {
                    "p_value": 0.26555,
                    "significant": [False, False, False]
                },
                "000002": {
                    "p_value": 1,
                    "significant": [False, False, False]
                },
                "000003": {
                    "p_value": 1,
                    "significant": [False, False, False]
                }
            }
          }
        ],
        "significance_levels": [0.01, 0.05, 0.1]
    },
    "updated": "2015-07-28T18:17:11.829000",
    "white_box": False
}
```

Note that the output in the snippet above has been abbreviated. As you see, the
``statistical_tests`` attribute contains the ``fraud``, ``normality`` and 
``outliers`` sections where the information for each field's distribution is stored.


Logistic Regressions
--------------------

A logistic regression is a supervised machine learning method for solving classification problems. Each of the classes in the field you want to predict, the objective field, is assigned a probability depending on the values of the input fields. The probability is computed as the value of a logistic function, whose argument is a linear combination of the predictors' values.
You can create a logistic regression selecting which fields from your
dataset you want to use as input fields (or predictors) and which
categorical field you want to predict, the objective field. Then the
created logistic regression is defined by the set of coefficients in the
linear combination of the values. Categorical and text fields need some prior work to be modelled using this method. They are expanded as a set of new fields, one per category or term (respectively) where the number of occurrences of the category or term is store. Thus, the linear combination is made on the frequency of the categories or terms.

An example of the logisticregression resource JSON structure is:

    JSONObject logisticRegression = 
    api.getLogisticRegression("logisticregression/5617e71c37203f506a000001");
    JSONObject object = (JSONObject) Utils.getJSONObject(
        logisticRegression, "object");

logisticRegression ``object`` object:

```
{   
    "balance_objective": False,
    "category": 0,
    "code": 200,
    "columns": 5,
    "created": "2015-10-09T16:11:08.444000",
    "credits": 0.017581939697265625,
    "credits_per_prediction": 0.0,
    "dataset": "dataset/561304f537203f4c930001ca",
    "dataset_field_types": {   
        "categorical": 1,
        "datetime": 0,
        "effective_fields": 5,
        "numeric": 4,
        "preferred": 5,
        "text": 0,
        "total": 5
    },
    "dataset_status": True,
    "description": "",
    "excluded_fields": [],
    "fields_meta": {
        "count": 5,
        "limit": 1000,
        "offset": 0,
        "query_total": 5,
        "total": 5
    },
    "input_fields": ["000000", "000001", "000002", "000003"],
    "locale": "en_US",
    "logistic_regression": {   
        "bias": 1,
        "c": 1,
        "coefficients": [   [   "Iris-virginica",
                                 [   -1.7074433493289376,
                                     -1.533662474502423,
                                     2.47026986670851,
                                     2.5567582221085563,
                                     -1.2158200612711925]],
                             [   "Iris-setosa",
                                 [   0.41021712519841674,
                                     1.464162165246765,
                                     -2.26003266131107,
                                     -1.0210350909174153,
                                     0.26421852991732514]],
                             [   "Iris-versicolor",
                                 [   0.42702327817072505,
                                     -1.611817241669904,
                                     0.5763832839459982,
                                     -1.4069842681625884,
                                     1.0946877732663143]]],
        "eps": 1e-05,
        "fields": {   
          "000000": {   
              "column_number": 0,
              "datatype": "double",
              "name": "sepal length",
              "optype": "numeric",
              "order": 0,
              "preferred": True,
              "summary": {   
                  "bins": [[4.3,1],[4.425,4],[4.6,4],...,[7.9,1]],
                  "kurtosis": -0.57357,
                  "maximum": 7.9,
                  "mean": 5.84333,
                  "median": 5.8,
                  "minimum": 4.3,
                  "missing_count": 0,
                  "population": 150,
                  "skewness": 0.31175,
                  "splits": [4.51526, 4.67252, 4.81113, ..., 6.92597, 7.20423, 7.64746],
                  "standard_deviation": 0.82807,
                  "sum": 876.5,
                  "sum_squares": 5223.85,
                  "variance": 0.68569
              }
          },
          "000001": {   
              "column_number": 1,
              "datatype": "double",
              "name": "sepal width",
              "optype": "numeric",
              "order": 1,
              "preferred": True,
              "summary": {   
                  "counts": [[2,1],[2.2,3],...,[4.2,1],[4.4,1]],
                  "kurtosis": 0.18098,
                  "maximum": 4.4,
                  "mean": 3.05733,
                  "median": 3,
                  "minimum": 2,
                  "missing_count": 0,
                  "population": 150,
                  "skewness": 0.31577,
                  "standard_deviation": 0.43587,
                  "sum": 458.6,
                  "sum_squares": 1430.4,
                  "variance": 0.18998
              }
          },
           "000002": {   
              "column_number": 2,
              "datatype": "double",
              "name": "petal length",
              "optype": "numeric",
              "order": 2,
              "preferred": True,
              "summary": {   
                  "bins": [[1,1],[1.16667,3],...,[6.6,1],[6.7,2],[6.9,1]],
                  "kurtosis": -1.39554,
                  "maximum": 6.9,
                  "mean": 3.758,
                  "median": 4.35,
                  "minimum": 1,
                  "missing_count": 0,
                  "population": 150,
                  "skewness": -0.27213,
                  "splits": [1.25138,1.32426,1.37171,...,6.02913,6.38125],
                  "standard_deviation": 1.7653,
                  "sum": 563.7,
                  "sum_squares": 2582.71,
                  "variance": 3.11628
              }
          },
          "000003": {   
              "column_number": 3,
              "datatype": "double",
              "name": "petal width",
              "optype": "numeric",
              "order": 3,
              "preferred": True,
              "summary": {
                  "counts": [[0.1,5],[0.2,29],...,[2.4,3],[2.5,3]],
                  "kurtosis": -1.33607,
                  "maximum": 2.5,
                  "mean": 1.19933,
                  "median": 1.3,
                  "minimum": 0.1,
                  "missing_count": 0,
                  "population": 150,
                  "skewness": -0.10193,
                  "standard_deviation": 0.76224,
                  "sum": 179.9,
                  "sum_squares": 302.33,
                  "variance": 0.58101
              }
          },
          "000004": {  
              "column_number": 4,
              "datatype": "string",
              "name": "species",
              "optype": "categorical",
              "order": 4,
              "preferred": True,
              "summary": {   
                  "categories": [["Iris-setosa",50],
                                  ["Iris-versicolor",50],
                                  ["Iris-virginica",50]],
                  "missing_count": 0
              },
              "term_analysis": {"enabled": True}
          }
      },
      "normalize": False,
      "regularization": "l2"
    },
    "max_columns": 5,
    "max_rows": 150,
    "name": u"iris" dataset"s logistic regression",
    "number_of_batchpredictions": 0,
    "number_of_evaluations": 0,
    "number_of_predictions": 1,
    "objective_field": "000004",
    "objective_field_name": "species",
    "objective_field_type": "categorical",
    "objective_fields": ["000004"],
    "out_of_bag": False,
    "private": True,
    "project": "project/561304c137203f4c9300016c",
    "range": [1, 150],
    "replacement": False,
    "resource": "logisticregression/5617e71c37203f506a000001",
    "rows": 150,
    "sample_rate": 1.0,
    "shared": False,
    "size": 4609,
    "source": "source/561304f437203f4c930001c3",
    "source_status": True,
    "status": {   "code": 5,
                   "elapsed": 86,
                   "message": "The logistic regression has been created",
                   "progress": 1.0},
    "subscription": False,
    "tags": ["species"],
    "updated": "2015-10-09T16:14:02.336000",
    "white_box": False
}
```

Note that the output in the snippet above has been abbreviated. As you see,
the ``logistic_regression`` attribute stores the coefficients used in the
logistic function as well as the configuration parameters described in
the [developers section](https://bigml.com/api/logisticregressions).


Associations
------------

Association Discovery is a popular method to find out relations among values
in high-dimensional datasets.

A common case where association discovery is often used is market basket analysis. This analysis seeks for customer shopping patterns across large transactional datasets. For instance, do customers who buy hamburgers and ketchup also consume bread?

Businesses use those insights to make decisions on promotions and product
placements. Association Discovery can also be used for other purposes such as early incident detection, web usage analysis, or software intrusion detection.

In BigML, the Association resource object can be built from any dataset, and
its results are a list of association rules between the items in the dataset.
In the example case, the corresponding association rule would have hamburguers and ketchup as the items at the left hand side of the association rule and bread would be the item at the right hand side. Both sides in this association rule are related, in the sense that observing the items in the left hand side implies observing the items in the right hand side. There are some metrics to ponder the quality of these association rules:

- Support: the proportion of instances which contain an itemset.

For an association rule, it means the number of instances in the dataset which
contain the rule's antecedent and rule's consequent together
over the total number of instances (N) in the dataset.

It gives a measure of the importance of the rule. Association rules have
to satisfy a minimum support constraint (i.e., min_support).

- Coverage: the support of the antedecent of an association rule.
It measures how often a rule can be applied.

- Confidence or (strength): The probability of seeing the rule's consequent
under the condition that the instances also contain the rule's antecedent.
Confidence is computed using the support of the association rule over the
coverage. That is, the percentage of instances which contain the consequent
and antecedent together over the number of instances which only contain
the antecedent.

Confidence is directed and gives different values for the association
rules Antecedent → Consequent and Consequent → Antecedent. Association
rules also need to satisfy a minimum confidence constraint
(i.e., min_confidence).

- Leverage: the difference of the support of the association
rule (i.e., the antecedent and consequent appearing together) and what would
be expected if antecedent and consequent where statistically independent.
This is a value between -1 and 1. A positive value suggests a positive
relationship and a negative value suggests a negative relationship.
0 indicates independence.

Lift: how many times more often antecedent and consequent occur together
than expected if they where statistically independent.
A value of 1 suggests that there is no relationship between the antecedent
and the consequent. Higher values suggest stronger positive relationships.
Lower values suggest stronger negative relationships (the presence of the
antecedent reduces the likelihood of the consequent)

As to the items used in association rules, each type of field is parsed to
extract items for the rules as follows:

- Categorical: each different value (class) will be considered a separate item.
- Text: each unique term will be considered a separate item.
- Items: each different item in the items summary will be considered.
- Numeric: Values will be converted into categorical by making a
segmentation of the values.
For example, a numeric field with values ranging from 0 to 600 split
into 3 segments:
segment 1 → [0, 200), segment 2 → [200, 400), segment 3 → [400, 600].
You can refine the behavior of the transformation using
[discretization](https://bigml.com/api/associations#ad_create_discretization)
and [field_discretizations](https://bigml.com/api/associations#ad_create_field_discretizations).

An example of the association resource JSON structure is:

    JSONObject association =
         api.getAssociation("association/5621b70910cb86ae4c000000");
    JSONObject object = (JSONObject) Utils.getJSONObject(
        sssociation, "object");

association ``object`` object:

```
{
    "associations":{
        "complement":false,
        "discretization":{
            "pretty":true,
            "size":5,
            "trim":0,
            "type":"width"
        },
        "items":[
            {
                "complement":false,
                "count":32,
                "field_id":"000000",
                "name":"Segment 1",
                "bin_end":5,
                "bin_start":null
            },
            {
                "complement":false,
                "count":49,
                "field_id":"000000",
                "name":"Segment 3",
                "bin_end":7,
                "bin_start":6
            },
            {
                "complement":false,
                "count":12,
                "field_id":"000000",
                "name":"Segment 4",
                "bin_end":null,
                "bin_start":7
            },
            {
                "complement":false,
                "count":19,
                "field_id":"000001",
                "name":"Segment 1",
                "bin_end":2.5,
                "bin_start":null
            },
            ...
            {
                "complement":false,
                "count":50,
                "field_id":"000004",
                "name":"Iris-versicolor"
            },
            {
                "complement":false,
                "count":50,
                "field_id":"000004",
                "name":"Iris-virginica"
            }
        ],
        "max_k": 100,
        "min_confidence":0,
        "min_leverage":0,
        "min_lift":1,
        "min_support":0,
        "rules":[
            {
                "confidence":1,
                "id":"000000",
                "leverage":0.22222,
                "lhs":[
                    13
                ],
                "lhs_cover":[
                    0.33333,
                    50
                ],
                "lift":3,
                "p_value":0.000000000,
                "rhs":[
                    6
                ],
                "rhs_cover":[
                    0.33333,
                    50
                ],
                "support":[
                    0.33333,
                    50
                ]
            },
            {
                "confidence":1,
                "id":"000001",
                "leverage":0.22222,
                "lhs":[
                    6
                ],
                "lhs_cover":[
                    0.33333,
                    50
                ],
                "lift":3,
                "p_value":0.000000000,
                "rhs":[
                    13
                ],
                "rhs_cover":[
                    0.33333,
                    50
                ],
                "support":[
                    0.33333,
                    50
                ]
            },
            ...
            {
                "confidence":0.26,
                "id":"000029",
                "leverage":0.05111,
                "lhs":[
                    13
                ],
                "lhs_cover":[
                    0.33333,
                    50
                ],
                "lift":2.4375,
                "p_value":0.0000454342,
                "rhs":[
                    5
                ],
                "rhs_cover":[
                    0.10667,
                    16
                ],
                "support":[
                    0.08667,
                    13
                ]
            },
            {
                "confidence":0.18,
                "id":"00002a",
                "leverage":0.04,
                "lhs":[
                    15
                ],
                "lhs_cover":[
                    0.33333,
                    50
                ],
                "lift":3,
                "p_value":0.0000302052,
                "rhs":[
                    9
                ],
                "rhs_cover":[
                    0.06,
                    9
                ],
                "support":[
                    0.06,
                    9
                ]
            },
            {
                "confidence":1,
                "id":"00002b",
                "leverage":0.04,
                "lhs":[
                    9
                ],
                "lhs_cover":[
                    0.06,
                    9
                ],
                "lift":3,
                "p_value":0.0000302052,
                "rhs":[
                    15
                ],
                "rhs_cover":[
                    0.33333,
                    50
                ],
                "support":[
                    0.06,
                    9
                ]
            }
        ],
        "rules_summary":{
            "confidence":{
                "counts":[
                    [
                        0.18,
                        1
                    ],
                    [
                        0.24,
                        1
                    ],
                    [
                        0.26,
                        2
                    ],
                    ...
                    [
                        0.97959,
                        1
                    ],
                    [
                        1,
                        9
                    ]
                ],
                "maximum":1,
                "mean":0.70986,
                "median":0.72864,
                "minimum":0.18,
                "population":44,
                "standard_deviation":0.24324,
                "sum":31.23367,
                "sum_squares":24.71548,
                "variance":0.05916
            },
            "k":44,
            "leverage":{
                "counts":[
                    [
                        0.04,
                        2
                    ],
                    [
                        0.05111,
                        4
                    ],
                    [
                        0.05316,
                        2
                    ],
                    ...
                    [
                        0.22222,
                        2
                    ]
                ],
                "maximum":0.22222,
                "mean":0.10603,
                "median":0.10156,
                "minimum":0.04,
                "population":44,
                "standard_deviation":0.0536,
                "sum":4.6651,
                "sum_squares":0.61815,
                "variance":0.00287
            },
            "lhs_cover":{
                "counts":[
                    [
                        0.06,
                        2
                    ],
                    [
                        0.08,
                        2
                    ],
                    [
                        0.10667,
                        4
                    ],
                    [
                        0.12667,
                        1
                    ],
                    ...
                    [
                        0.5,
                        4
                    ]
                ],
                "maximum":0.5,
                "mean":0.29894,
                "median":0.33213,
                "minimum":0.06,
                "population":44,
                "standard_deviation":0.13386,
                "sum":13.15331,
                "sum_squares":4.70252,
                "variance":0.01792
            },
            "lift":{
                "counts":[
                    [
                        1.40625,
                        2
                    ],
                    [
                        1.5067,
                        2
                    ],
                    ...
                    [
                        2.63158,
                        4
                    ],
                    [
                        3,
                        10
                    ],
                    [
                        4.93421,
                        2
                    ],
                    [
                        12.5,
                        2
                    ]
                ],
                "maximum":12.5,
                "mean":2.91963,
                "median":2.58068,
                "minimum":1.40625,
                "population":44,
                "standard_deviation":2.24641,
                "sum":128.46352,
                "sum_squares":592.05855,
                "variance":5.04635
            },
            "p_value":{
                "counts":[
                    [
                        0.000000000,
                        2
                    ],
                    [
                        0.000000000,
                        4
                    ],
                    [
                        0.000000000,
                        2
                    ],
                    ...
                    [
                        0.0000910873,
                        2
                    ]
                ],
                "maximum":0.0000910873,
                "mean":0.0000106114,
                "median":0.00000000,
                "minimum":0.000000000,
                "population":44,
                "standard_deviation":0.0000227364,
                "sum":0.000466903,
                "sum_squares":0.0000000,
                "variance":0.000000001
            },
            "rhs_cover":{
                "counts":[
                    [
                        0.06,
                        2
                    ],
                    [
                        0.08,
                        2
                    ],
                    ...
                    [
                        0.42667,
                        2
                    ],
                    [
                        0.46667,
                        3
                    ],
                    [
                        0.5,
                        4
                    ]
                ],
                "maximum":0.5,
                "mean":0.29894,
                "median":0.33213,
                "minimum":0.06,
                "population":44,
                "standard_deviation":0.13386,
                "sum":13.15331,
                "sum_squares":4.70252,
                "variance":0.01792
            },
            "support":{
                "counts":[
                    [
                        0.06,
                        4
                    ],
                    [
                        0.06667,
                        2
                    ],
                    [
                        0.08,
                        2
                    ],
                    [
                        0.08667,
                        4
                    ],
                    [
                        0.10667,
                        4
                    ],
                    [
                        0.15333,
                        2
                    ],
                    [
                        0.18667,
                        4
                    ],
                    [
                        0.19333,
                        2
                    ],
                    [
                        0.20667,
                        2
                    ],
                    [
                        0.27333,
                        2
                    ],
                    [
                        0.28667,
                        2
                    ],
                    [
                        0.3,
                        4
                    ],
                    [
                        0.32,
                        2
                    ],
                    [
                        0.33333,
                        6
                    ],
                    [
                        0.37333,
                        2
                    ]
                ],
                "maximum":0.37333,
                "mean":0.20152,
                "median":0.19057,
                "minimum":0.06,
                "population":44,
                "standard_deviation":0.10734,
                "sum":8.86668,
                "sum_squares":2.28221,
                "variance":0.01152
            }
        },
        "search_strategy":"leverage",
        "significance_level":0.05
    },
    "category":0,
    "clones":0,
    "code":200,
    "columns":5,
    "created":"2015-11-05T08:06:08.184000",
    "credits":0.017581939697265625,
    "dataset":"dataset/562fae3f4e1727141d00004e",
    "dataset_status":true,
    "dataset_type":0,
    "description":"",
    "excluded_fields":[ ],
    "fields_meta":{
        "count":5,
        "limit":1000,
        "offset":0,
        "query_total":5,
        "total":5
    },
    "input_fields":[
        "000000",
        "000001",
        "000002",
        "000003",
        "000004"
    ],
    "locale":"en_US",
    "max_columns":5,
    "max_rows":150,
    "name":"iris' dataset's association",
    "out_of_bag":false,
    "price":0,
    "private":true,
    "project":null,
    "range":[
        1,
        150
    ],
    "replacement":false,
    "resource":"association/5621b70910cb86ae4c000000",
    "rows":150,
    "sample_rate":1,
    "shared":false,
    "size":4609,
    "source":"source/562fae3a4e1727141d000048",
    "source_status":true,
    "status":{
        "code":5,
        "elapsed":1072,
        "message":"The association has been created",
        "progress":1
    },
    "subscription":false,
    "tags":[ ],
    "updated":"2015-11-05T08:06:20.403000",
    "white_box":false
}
```

    
Note that the output in the snippet above has been abbreviated. As you see,
the ``associations`` attribute stores items, rules and metrics extracted
from the datasets as well as the configuration parameters described in
the [developers section](https://bigml.com/api/associations).


Topic Models
------------

A topic model is an unsupervised machine learning method for unveiling all the different topics underlying a collection of documents.
BigML uses Latent Dirichlet Allocation (LDA), one of the most popular probabilistic methods for topic modeling.
In BigML, each instance (i.e. each row in your dataset) will
be considered a document and the contents of all the text fields
given as inputs will be automatically concatenated and considered the
document bag of words.

Topic model is based on the assumption that any document
exhibits a mixture of topics. Each topic is composed of a set of words
which are thematically related. The words from a given topic have different
probabilities for that topic. At the same time, each word can be attributable
to one or several topics. So for example the word "sea" may be found in
a topic related with sea transport but also in a topic related to holidays.
Topic model automatically discards stop words and high
frequency words.

Topic model's main applications include browsing, organizing and understanding
large archives of documents. It can been applied for information retrieval,
collaborative filtering, assessing document similarity among others.
The topics found in the dataset can also be very useful new features
before applying other models like classification, clustering, or
anomaly detection.

An example of the topicmodel resource JSON structure is:

    JSONObject topicModel = 
        api.getTopicModel("topicmodel/58362aaa983efc45a1000007");
    JSONObject object = (JSONObject) Utils.getJSONObject(topicModel, "object");

topicModel ``object`` object:

```
{   
    "category": 0,
    "code": 200,
    "columns": 1,
    "configuration": None,
    "configuration_status": False,
    "created": "2016-11-23T23:47:54.703000",
    "credits": 0.0,
    "credits_per_prediction": 0.0,
    "dataset": "dataset/58362aa0983efc45a0000005",
    "dataset_field_types": {   
        "categorical": 1,
        "datetime": 0,
        "effective_fields": 672,
        "items": 0,
        "numeric": 0,
        "preferred": 2,
        "text": 1,
        "total": 2
    },
    "dataset_status": True,
    "dataset_type": 0,
    "description": "",
    "excluded_fields": [],
    "fields_meta": {   
        "count": 1,
        "limit": 1000,
        "offset": 0,
        "query_total": 1,
        "total": 1
    },
    "input_fields": ["000001"],
    "locale": "en_US",
    "max_columns": 2,
    "max_rows": 656,
    "name": u"spam dataset"s Topic Model ",
    "number_of_batchtopicdistributions": 0,
    "number_of_public_topicdistributions": 0,
    "number_of_topicdistributions": 0,
    "ordering": 0,
    "out_of_bag": False,
    "price": 0.0,
    "private": True,
    "project": None,
    "range": [1, 656],
    "replacement": False,
    "resource": "topicmodel/58362aaa983efc45a1000007",
    "rows": 656,
    "sample_rate": 1.0,
    "shared": False,
    "size": 54740,
    "source": "source/58362a69983efc459f000001",
    "source_status": True,
    "status": {   
        "code": 5,
        "elapsed": 3222,
        "message": "The topic model has been created",
        "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "topic_model": {   
        "alpha": 4.166666666666667,
        "beta": 0.1,
        "bigrams": False,
        "case_sensitive": False,
        "fields": {   
            "000001": {   
                "column_number": 1,
                "datatype": "string",
                "name": "Message",
                "optype": "text",
                "order": 0,
                "preferred": True,
                "summary": {   
                    "average_length": 78.14787,
                    "missing_count": 0,
                    "tag_cloud": [["call",72],["ok",36],...,["yijue",2]],
                    "term_forms": {   }
                },
                "term_analysis": {   
                    "case_sensitive": False,
                    "enabled": True,
                    "language": "en",
                    "stem_words": False,
                    "token_mode": "all",
                    "use_stopwords": False
                }
            }
        },
        "hashed_seed": 62146850,
        "language": "en",
        "number_of_topics": 12,
        "term_limit": 4096,
        "term_topic_assignments": [
            [0,5,0,1,0,19,0,0,19,0,1,0],
            [0,0,0,13,0,0,0,0,5,0,0,0],
            ...
            [0,7,27,0,112,0,0,0,0,0,14,2]
        ],
        "termset": ["000","03","04",...,"yr","yup","\xfc"],
        "top_n_terms": 10,
        "topicmodel_seed": "26c386d781963ca1ea5c90dab8a6b023b5e1d180",
        "topics": [   {   "id": "000000",
                           "name": "Topic 00",
                           "probability": 0.09375,
                           "top_terms": [   [   "im",
                                                 0.04849],
                                             [   "hi",
                                                 0.04717],
                                             [   "love",
                                                 0.04585],
                                             [   "please",
                                                 0.02867],
                                             [   "tomorrow",
                                                 0.02867],
                                             [   "cos",
                                                 0.02823],
                                             [   "sent",
                                                 0.02647],
                                             [   "da",
                                                 0.02383],
                                             [   "meet",
                                                 0.02207],
                                             [   "dinner",
                                                 0.01898]]},
                         {   "id": "000001",
                             "name": "Topic 01",
                             "probability": 0.08215,
                             "top_terms": [   [   "lt",
                                                   0.1015],
                                               [   "gt",
                                                   0.1007],
                                               [   "wish",
                                                   0.03958],
                                               [   "feel",
                                                   0.0272],
                                               [   "shit",
                                                   0.02361],
                                               [   "waiting",
                                                   0.02281],
                                               [   "stuff",
                                                   0.02001],
                                               [   "name",
                                                   0.01921],
                                               [   "comp",
                                                   0.01522],
                                               [   "forgot",
                                                   0.01482]]},
                          ...
                         {   "id": "00000b",
                             "name": "Topic 11",
                             "probability": 0.0826,
                             "top_terms": [   [   "call",
                                                   0.15084],
                                               [   "min",
                                                   0.05003],
                                               [   "msg",
                                                   0.03185],
                                               [   "home",
                                                   0.02648],
                                               [   "mind",
                                                   0.02152],
                                               [   "lt",
                                                   0.01987],
                                               [   "bring",
                                                   0.01946],
                                               [   "camera",
                                                   0.01905],
                                               [   "set",
                                                   0.01905],
                                               [   "contact",
                                                   0.01781]]
                        }
            ],
        "use_stopwords": False
    },
    "updated": "2016-11-23T23:48:03.336000",
    "white_box": False
}
```

Note that the output in the snippet above has been abbreviated.

The topic model returns a list of top terms for each topic found in the data.
Note that topics are not labeled, so you have to infer their meaning according
to the words they are composed of.

Once you build the topic model you can calculate each topic probability
for a given document by using Topic Distribution.
This information can be useful to find documents similarities based
on their thematic.

As you see, the ``topic_model`` attribute stores the topics and termset and term to topic assignment, as well as the configuration parameters described in
the [developers section](https://bigml.com/api/topicmodels).


Time Series
-----------

A time series model is a supervised learning method to forecast the future
values of a field based on its previously observed values.
It is used to analyze time based data when historical patterns can explain
the future behavior such as stock prices, sales forecasting,
website traffic, production and inventory analysis, weather forecasting, etc.
A time series model needs to be trained with time series data,
i.e., a field containing a sequence of equally distributed data points in time.

BigML implements exponential smoothing to train time series models.
Time series data is modeled as a level component and it can optionally
include a trend (damped or not damped) and a seasonality
components. You can learn more about how to include these components and their
use in the [API documentation page](https://bigml.io/api/).

You can create a time series model selecting one or several fields from
your dataset, that will be the ojective fields. The forecast will compute
their future values.

An example of the topicmodel resource JSON structure is:

    JSONObject timeSeries = 
        api.getTimeSeries("timeseries/596a0f66983efc53f3000000");
    JSONObject object = (JSONObject) Utils.getJSONObject(timeSeries, "object");

timeSeries ``object`` object:


```
{   
    "category": 0,
    "clones": 0,
    "code": 200,
    "columns": 1,
    "configuration": None,
    "configuration_status": False,
    "created": "2017-07-15T12:49:42.601000",
    "credits": 0.0,
    "dataset": "dataset/5968ec42983efc21b0000016",
    "dataset_field_types": {   
        "categorical": 0,
        "datetime": 0,
        "effective_fields": 6,
        "items": 0,
        "numeric": 6,
        "preferred": 6,
        "text": 0,
        "total": 6
    },
    "dataset_status": True,
    "dataset_type": 0,
    "description": "",
    "fields_meta": {   
        "count": 1,
        "limit": 1000,
        "offset": 0,
        "query_total": 1,
        "total": 1
    },
    "forecast": {   
      "000005": [   
        {   
          "lower_bound": [30.14111, 30.14111, ... 30.14111],
          "model": "A,N,N",
          "point_forecast": [68.53181, 68.53181, ..., 68.53181, 68.53181],
          "time_range": {   
               "end": 129,
               "interval": 1,
               "interval_unit": "milliseconds",
               "start": 80
          },
          "upper_bound": [106.92251, 106.92251, ... 106.92251, 106.92251]
        },
        {   
          "lower_bound": [35.44118, 35.5032, ..., 35.28083],
          "model": "A,Ad,N",
          ...
             66.83537,
             66.9465],
          "time_range": {   
              "end": 129,
              "interval": 1,
              "interval_unit": "milliseconds",
              "start": 80
          }
        }
      ]
    },
    "horizon": 50,
    "locale": "en_US",
    "max_columns": 6,
    "max_rows": 80,
    "name": "my_ts_data",
    "name_options": "period=1, range=[1, 80]",
    "number_of_evaluations": 0,
    "number_of_forecasts": 0,
    "number_of_public_forecasts": 0,
    "objective_field": "000005",
    "objective_field_name": "Final",
    "objective_field_type": "numeric",
    "objective_fields": ["000005"],
    "objective_fields_names": ["Final"],
    "price": 0.0,
    "private": True,
    "project": None,
    "range": [1, 80],
    "resource": "timeseries/596a0f66983efc53f3000000",
    "rows": 80,
    "shared": False,
    "short_url": "",
    "size": 2691,
    "source": "source/5968ec3c983efc218c000006",
    "source_status": True,
    "status": {
        "code": 5,
       "elapsed": 8358,
       "message": "The time series has been created",
       "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "time_series": {   
        "all_numeric_objectives": False,
        "datasets": {
          "000005": "dataset/596a0f70983efc53f3000003"},
          "ets_models": {   
              "000005": [   
                {   
                    "aic": 831.30903,
                    "aicc": 831.84236,
                    "alpha": 0.00012,
                    "beta": 0,
                    "bic": 840.83713,
                    "final_state": {   "b": 0,
                                        "l": 68.53181,
                                        "s": [   0]},
                    "gamma": 0,
                    "initial_state": {   "b": 0,
                                          "l": 68.53217,
                                          "s": [   0]},
                    "name": "A,N,N",
                    "period": 1,
                    "phi": 1,
                    "r_squared": -0.0187,
                    "sigma": 19.19535
                },
                {   
                    "aic": 834.43049,
                    ...
                    "slope": 0.11113,
                    "value": 61.39
                }
              ]
          },
          "fields": {   
              "000005": {   
                  "column_number": 5,
                  "datatype": "double",
                  "name": "Final",
                  "optype": "numeric",
                  "order": 0,
                  "preferred": True,
                  "summary": {   
                      "bins": [[28.06,1], ...,  [108.335,2]],
                      ...
                      "sum_squares": 389814.3944,
                      "variance": 380.73315
                  }
              }
          },
          "period": 1,
          "time_range": {
              "end": 79,
              "interval": 1,
              "interval_unit": "milliseconds",
              "start": 0
          }
      },
    "type": 0,
    "updated": "2017-07-15T12:49:52.549000",
    "white_box": False
}
```


OptiMLs
-------

An OptiML is the result of an automated optimization process to find the
best model (type and configuration) to solve a particular classification or regression problem.

The selection process automates the usual time-consuming task of trying
different models and parameters and evaluating their results to find the
best one. Using the OptiML, non-experts can build top-performing models.

You can create an OptiML selecting the ojective field to be predicted, the
evaluation metric to be used to rank the models tested in the process and
a maximum time for the task to be run.

An example of the optiML resource JSON structure is:

    JSONObject optiML = api.getOptiML("optiml/5afde4a42a83475c1b0008a2");
    JSONObject object = (JSONObject) Utils.getJSONObject(optiML, "object");

optiML ``object`` object:


```
{   
    "category": 0,
    "code": 200,
    "configuration": None,
    "configuration_status": False,
    "created": "2018-05-17T20:23:00.060000",
    "creator": "mmartin",
    "dataset": "dataset/5afdb7009252732d930009e8",
    "dataset_status": True,
    "datasets": ["dataset/5afde6488bf7d551ee00081c",
                  "dataset/5afde6488bf7d551fd00511f",
                  "dataset/5afde6488bf7d551fe002e0f",
                  ...
                  "dataset/5afde64d8bf7d551fd00512e"],
    "description": "",
    "evaluations": ["evaluation/5afde65c8bf7d551fd00514c",
                     "evaluation/5afde65c8bf7d551fd00514f",
                     ...
                     "evaluation/5afde6628bf7d551fd005161"],
    "excluded_fields": [],
    "fields_meta": {
        "count": 5,
        "limit": 1000,
        "offset": 0,
        "query_total": 5,
        "total": 5
    },
    "input_fields": ["000000", "000001", "000002", "000003"],
    "model_count": {
        "logisticregression": 1, 
        "model": 8, 
        "total": 9
    },
    "models": ["model/5afde64e8bf7d551fd005131",
                "model/5afde64f8bf7d551fd005134",
                "model/5afde6518bf7d551fd005137",
                "model/5afde6538bf7d551fd00513a",
                "logisticregression/5afde6558bf7d551fd00513d",
                ...
                "model/5afde65a8bf7d551fd005149"],
    "models_meta": {
        "count": 9, 
        "limit": 1000, 
        "offset": 0, 
        "total": 9
    },
    "name": "iris",
    "name_options": "9 total models (logisticregression: 1, model: 8), metric=max_phi, model candidates=18, max. training time=300",
    "objective_field": "000004",
    "objective_field_details": {
        "column_number": 4,
        "datatype": "string",
        "name": "species",
        "optype": "categorical",
        "order": 4
    },
    "objective_field_name": "species",
    "objective_field_type": "categorical",
    "objective_fields": ["000004"],
    "optiml": {   
        "created_resources": {   
            "dataset": 10,
            "logisticregression": 11,
            "logisticregression_evaluation": 11,
            "model": 29,
            "model_evaluation": 29
        },
       "datasets": [   {   "id": "dataset/5afde6488bf7d551ee00081c",
                            "name": "iris",
                            "name_options": "120 instances, 5 fields (1 categorical, 4 numeric), sample rate=0.8"},
                        {   "id": "dataset/5afde6488bf7d551fd00511f",
                            "name": "iris",
                            "name_options": "30 instances, 5 fields (1 categorical, 4 numeric), sample rate=0.2, out of bag"},
                        {   "id": "dataset/5afde6488bf7d551fe002e0f",
                            "name": "iris",
                            "name_options": "120 instances, 5 fields (1 categorical, 4 numeric), sample rate=0.8"},
                        ...
                        {   "id": "dataset/5afde64d8bf7d551fd00512e",
                            "name": "iris",
                            "name_options": "120 instances, 5 fields (1 categorical, 4 numeric), sample rate=0.8"}],
        "fields": {
          "000000": {   
               "column_number": 0,
               "datatype": "double",
               "name": "sepal length",
               "optype": "numeric",
               "order": 0,
               "preferred": True,
               "summary": {   
                  "bins": [[4.3,1], ..., [7.9,1]],
                  ...
                  "sum": 179.9,
                  "sum_squares": 302.33,
                  "variance": 0.58101
               }
          },
          "000004": {   
               "column_number": 4,
               "datatype": "string",
               "name": "species",
               "optype": "categorical",
               "order": 4,
               "preferred": True,
               "summary": {
                  "categories": [["Iris-setosa",50],
                                  ["Iris-versicolor",50],
                                  ["Iris-virginica",50]],
                  "missing_count": 0
              },
              "term_analysis": {"enabled": True}
          }
      },
      "max_training_time": 300,
      "metric": "max_phi",
      "model_types": ["model", "logisticregression"],
      "models": [   
        {
            "evaluation": {
               "id": "evaluation/5afde65c8bf7d551fd00514c",
               "info": {   
                  "accuracy": 0.96667,
                  "average_area_under_pr_curve": 0.97867,
                  ...
                  "per_class_statistics": [   
                    {   
                       "accuracy": 1,
                       "area_under_pr_curve": 1,
                       ...
                       "spearmans_rho": 0.82005
                    }
                  ]
               },
               "metric_value": 0.95356,
               "metric_variance": 0.00079,
               "name": "iris vs. iris",
               "name_options": "279-node, deterministic order, operating kind=probability"
            },
            "evaluation_count": 3,
            "id": "model/5afde64e8bf7d551fd005131",
            "importance": [   [   "000002",
                                   0.70997],
                               [   "000003",
                                   0.27289],
                               [   "000000",
                                   0.0106],
                               [   "000001",
                                   0.00654]],
            "kind": "model",
            "name": "iris",
            "name_options": "279-node, deterministic order"
        },
        ....
    }
    "private": True,
    "project": None,
    "resource": "optiml/5afde4a42a83475c1b0008a2",
    "shared": False,
    "size": 3686,
    "source": "source/5afdb6fb9252732d930009e5",
    "source_status": True,
    "status": {   
          "code": 5,
         "elapsed": 448878.0,
         "message": "The optiml has been created",
         "progress": 1
    },
    "subscription": False,
    "tags": [],
    "test_dataset": None,
    "type": 0,
    "updated": "2018-05-17T20:30:29.063000"
}
```


Fusions
-------

A Fusion is a special type of composed resource for which all
submodels satisfy the following constraints: they're all either
classifications or regressions over the same kind of data or
compatible fields, with the same objective field. Given those
properties, a fusion can be considered a supervised model,
and therefore one can predict with fusions and evaluate them.
Ensembles can be viewed as a kind of fusion subject to the additional
constraints that all its submodels are tree models that, moreover,
have been built from the same base input data, but sampled in particular ways.

The model types allowed to be a submodel of a fusion are:
deepnet, ensemble, fusion, model, and logistic regression.

An example of the fusion resource JSON structure is:

    JSONObject fusion = api.getFusion("fusion/59af8107b8aa0965d5b61138");
    JSONObject object = (JSONObject) Utils.getJSONObject(fusion, "object");

fusion ``object`` object:


```
{
    "category": 0,
    "code": 200,
    "configuration": null,
    "configuration_status": false,
    "created": "2018-05-09T20:11:05.821000",
    "credits_per_prediction": 0,
    "description": "",
    "fields_meta": {
        "count": 5,
        "limit": 1000,
        "offset": 0,
        "query_total": 5,
        "total": 5
    },
    "fusion": {
        "models": [
            {
                "id": "ensemble/5af272eb4e1727d378000050",
                "kind": "ensemble",
                "name": "Iris ensemble",
                "name_options": "boosted trees, 1999-node, 16-iteration, deterministic order, balanced"
            },
            {
                "id": "model/5af272fe4e1727d3780000d6",
                "kind": "model",
                "name": "Iris model",
                "name_options": "1999-node, pruned, deterministic order, balanced"
            },
            {
                "id": "logisticregression/5af272ff4e1727d3780000d9",
                "kind": "logisticregression",
                "name": "Iris LR",
                "name_options": "L2 regularized (c=1), bias, auto-scaled, missing values, eps=0.001"
            }
        ]
    },
    "importance": {
        "000000": 0.05847,
        "000001": 0.03028,
        "000002": 0.13582,
        "000003": 0.4421
    },
    "model_count": {
        "ensemble": 1,
        "logisticregression": 1,
        "model": 1,
        "total": 3
    },
    "models": [
        "ensemble/5af272eb4e1727d378000050",
        "model/5af272fe4e1727d3780000d6",
        "logisticregression/5af272ff4e1727d3780000d9"
    ],
    "models_meta": {
        "count": 3,
        "limit": 1000,
        "offset": 0,
        "total": 3
    },
    "name": "iris",
    "name_options": "3 total models (ensemble: 1, logisticregression: 1, model: 1)",
    "number_of_batchpredictions": 0,
    "number_of_evaluations": 0,
    "number_of_predictions": 0,
    "number_of_public_predictions": 0,
    "objective_field": "000004",
    "objective_field_details": {
        "column_number": 4,
        "datatype": "string",
        "name": "species",
        "optype": "categorical",
        "order": 4
    },
    "objective_field_name": "species",
    "objective_field_type": "categorical",
    "objective_fields": [
        "000004"
    ],
    "private": true,
    "project": null,
    "resource":"fusion/59af8107b8aa0965d5b61138",
    "shared": false,
    "status": {
        "code": 5,
        "elapsed": 8420,
        "message": "The fusion has been created",
        "progress": 1
    },
    "subscription": false,
    "tags": [],
    "type": 0,
    "updated": "2018-05-09T20:11:14.258000"
}
```
