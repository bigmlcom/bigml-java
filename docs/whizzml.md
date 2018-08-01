Whizzml Resources
=====================

Whizzml is a Domain Specific Language that allows the definition and
execution of ML-centric workflows. Its objective is allowing BigML
users to define their own composite tasks, using as building blocks
the basic resources provided by BigML itself. Using Whizzml they can be
glued together using a higher order, functional, Turing-complete language.
The Whizzml code can be stored and executed in BigML using three kinds of
resources: ``Scripts``, ``Libraries`` and ``Executions``.

Whizzml ``Scripts`` can be executed in BigML's servers, that is,
in a controlled, fully-scalable environment which takes care of their
parallelization and fail-safe operation. Each execution uses an ``Execution``
resource to store the arguments and results of the process. Whizzml
``Libraries`` store generic code to be shared of reused in other Whizzml
``Scripts``.


Scripts
-------

In BigML a ``Script`` resource stores Whizzml source code, and the results of
its compilation. Once a Whizzml script is created, it's automatically compiled;
if compilation succeeds, the script can be run, that is,
used as the input for a Whizzml execution resource.

An example of a ``script`` that would create a ``source`` in BigML using the
contents of a remote file is:

```
    import org.bigml.binding.BigMLClient;

    // Create BigMLClient
    BigMLClient api = new BigMLClient();

    // creating a script directly from the source code. This script creates
    // a source uploading data from an s3 repo. You could also create a
    // a script by using as first argument the path to a .whizzml file which
    // contains your source code.
    JSONObject script = api.createScript(
            "(create-source {\"remote\" \"s3://bigml-public/csv/iris.csv\"})")

    while (!api.scriptIsReady(script)) 
        Thread.sleep(1000);

    JSONObject object = (JSONObject) Utils.getJSONObject(script, "object");
```

script ``object`` object:

```
{   
    "approval_status": 0,
    "category": 0,
    "code": 200,
    "created": "2016-05-18T16:54:05.666000",
    "description": "",
    "imports": [],
    "inputs": None,
    "line_count": 1,
    "locale": "en-US",
    "name": "Script",
    "number_of_executions": 0,
    "outputs": None,
    "price": 0.0,
    "private": True,
    "project": None,
    "provider": None,
    "resource": "script/573c9e2db85eee23cd000489",
    "shared": False,
    "size": 59,
    "source_code": "(create-source {"remote" "s3://bigml-public/csv/iris.csv"})",
    "status": {   
        "code": 5,
        "elapsed": 4,
        "message": "The script has been created",
        "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "updated": "2016-05-18T16:54:05.850000",
    "white_box": False
}
```

A ``script`` allows to define some variables as ``inputs``. In the previous example, no input has been defined, but we could modify our code to allow the user to set the remote file name as input:

```
    import org.bigml.binding.BigMLClient;

    // Create BigMLClient
    BigMLClient api = new BigMLClient();

    JSONArray inputsList = JSONValue.parse(
        "[{"name": "my_remote_data",
           "type": "string",
           "default": "s3://bigml-public/csv/iris.csv",
           "description": "Location of the remote data"}]"
    );
    JSONObject inputs = new JSONObject();
    inputs.put("inputs", inputsList);

    JSONObject script = api.createScript(
            "(create-source {\"remote\" my_remote_data})",
            inputs)

    while (!api.sctiptIsReady(source)) 
        Thread.sleep(1000);
```

The ``script`` can also use a ``library`` resource (please, see the
``Libraries`` section below for more details) by including its id in the
``imports`` attribute. Other attributes can be checked at the
[API Developers documentation for Scripts](https://bigml.com/api/scripts#ws_script_arguments).


Executions
----------

To execute in BigML a compiled Whizzml ``script`` you need to create an
``execution`` resource. It's also possible to execute a pipeline of
many compiled scripts in one request.

Each ``execution`` is run under its associated user credentials and its
particular environment constaints. As ``scripts`` can be shared, you can execute the same ``script`` several times under different usernames by creating different ``executions``.

As an example of ``execution`` resource, let's create one for the script
in the previous section:

```
    import org.bigml.binding.BigMLClient;

    // Create BigMLClient
    BigMLClient api = new BigMLClient();

    JSONObject execution = api.createExecution("script/573c9e2db85eee23cd000489");

    while (!api.executionIsReady(execution)) 
        Thread.sleep(1000);

    JSONObject object = (JSONObject) Utils.getJSONObject(execution, "object");
```

execution ``object`` object:

   
```
{   
    "category": 0,
    "code": 200,
    "created": "2016-05-18T16:58:01.613000",
    "creation_defaults": {   },
    "description": "",
    "execution": {   
        "output_resources": [   
          {   
             "code": 1,
             "id": "source/573c9f19b85eee23c600024a",
             "last_update": 1463590681854,
             "progress": 0.0,
             "state": "queued",
             "task": "Queuing job",
             "variable": ""
          }
        ],
        "outputs": [],
        "result": "source/573c9f19b85eee23c600024a",
        "results": ["source/573c9f19b85eee23c600024a"],
        "sources": [["script/573c9e2db85eee23cd000489", ""]],
        "steps": 16
    },
    "inputs": None,
    "locale": "en-US",
    "name": u"Script"s Execution",
    "project": None,
    "resource": "execution/573c9f19b85eee23bd000125",
    "script": "script/573c9e2db85eee23cd000489",
    "script_status": True,
    "shared": False,
    "status": {   
        "code": 5,
        "elapsed": 249,
        "elapsed_times": {   
            "in-progress": 247,
            "queued": 62,
            "started": 2
        },
        "message": "The execution has been created",
        "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "updated": "2016-05-18T16:58:02.035000"
}
```


An ``execution`` receives inputs, the ones defined in the ``script`` chosen
to be executed, and generates a result. It can also generate outputs.
As you can see, the execution resource contains information about the result
of the execution, the resources that have been generated while executing and
users can define some variables in the code to be exported as outputs. Please
refer to the [Developers documentation for Executions](https://bigml.com/api/executions#we_execution_arguments) for details on how to define execution outputs.


Libraries
---------

The ``library`` resource in BigML stores a special kind of compiled Whizzml
source code that only defines functions and constants. The ``library`` is
intended as an import for executable scripts.
Thus, a compiled library cannot be executed, just used as an
import in other ``libraries`` and ``scripts`` (which then have access
to all identifiers defined in the ``library``).

As an example, we build a ``library`` to store the definition of two functions:
``mu`` and ``g``. The first one adds one to the value set as argument and
the second one adds two variables and increments the result by one.


```
    import org.bigml.binding.BigMLClient;

    // Create BigMLClient
    BigMLClient api = new BigMLClient();

    JSONObject library = api.createLibrary(
        "(define (mu x) (+ x 1)) (define (g z y) (mu (+ y z)))");

    while (!api.libraryIsReady(library)) 
        Thread.sleep(1000);

    JSONObject object = (JSONObject) Utils.getJSONObject(library, "object");
```

library ``object`` object:


```
{   
    "approval_status": 0,
    "category": 0,
    "code": 200,
    "created": "2016-05-18T18:58:50.838000",
    "description": "",
    "exports": [   
        {"name": "m", "signature": ["x"]},
        {"name": "g", "signature": ["z", "y"]}
    ],
    "imports": [],
    "line_count": 1,
    "name": "Library",
    "price": 0.0,
    "private": True,
    "project": None,
    "provider": None,
    "resource": "library/573cbb6ab85eee23c300018e",
    "shared": False,
    "size": 53,
    "source_code": "(define (mu x) (+ x 1)) (define (g z y) (mu (+ y z)))",
    "status": {   
        "code": 5,
        "elapsed": 2,
        "message": "The library has been created",
        "progress": 1.0
    },
    "subscription": True,
    "tags": [],
    "updated": "2016-05-18T18:58:52.432000",
    "white_box": False
}
```

Libraries can be imported in scripts. The ``imports`` attribute of a ``script``
can contain a list of ``library`` IDs whose defined functions
and constants will be ready to be used throughout the ``script``. Please,
refer to the [API Developers documentation for Libraries](https://bigml.com/api/libraries#wl_library_arguments) for more details.
