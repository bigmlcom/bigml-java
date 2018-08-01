Introduction
=====================

[BigML](https://bigml.com) makes machine learning easy by taking care of the details required to add data-driven decisions and predictive power to your company. Unlike other machine learning services, BigML creates [beautiful predictive models](https://bigml.com/gallery/models) that can be easily understood and interacted with.

These BigML Java bindings allow you to interact with BigML.io, the API for BigML. You can use it to easily create, retrieve, list, update, and delete BigML resources (i.e., sources, datasets, models and, predictions).

This module is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


Support
-----------------------------

Please report problems and bugs to our [BigML Java Binding issue tracker](https://github.com/bigmlcom/bigml-java/issues).

Discussions about the different bindings take place in the general
[BigML mailing list](http://groups.google.com/group/bigml). Or join us
in our [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0>).


Requirements
-----------------------------

JVM 1.6 and above are currently supported by these bindings.

You will also need `maven` to build the package. If you are new to
`maven, please refer to [Maven Getting Started Guide](https://maven.apache.org/guides/getting-started/index.html>).



Installation
------------

To use the latest stable release, include the following `maven` dependency in your project's `pom.xml`.

    <dependency>
        <groupId>org.bigml</groupId>
        <artifactId>bigml-binding</artifactId>
        <version>1.8.0</version>
    </dependency>

You can also download the development version of the bindings directly from the Git repository

    $ git clone git://github.com/bigmlcom/bigml-java.git



Authentication
------------

All the requests to BigML.io must be authenticated using your username and [API key](https://bigml.com/account/apikey) and are always transmitted over HTTPS.

This module will look for your username and API key in the `src/main/resources/binding.properties` file. Alternatively, you can respectively set the JVM parameters `BIGML_USERNAME` and `BIGML_API_KEY`  with `-D` or use envronment variables.

With that set up, connecting to BigML is a breeze.
First, import `BigMLClient`:

    import org.bigml.binding.BigMLClient;

then:

    BigMLClient api = new BigMLClient();

Otherwise, you can initialize directly when instantiating the BigMLClient class as follows:

    BigMLClient api = new BigMLClient(
        "myusername", "ae579e7e53fb9abd646a6ff8aa99d4afe83ac291", null);

These credentials will allow you to manage any resource in your user environment.

In BigML a user can also work for an `organization`. In this case, the organization administrator should previously assign permissions for the user to access one or several particular projects in the organization. Once permissions are granted, the user can work with resources in a project according to his permission level by creating a special constructor for each project. The connection constructor in this case should include the ``project ID``:


    BigMLClient api = new BigMLClient(
        "myusername", "ae579e7e53fb9abd646a6ff8aa99d4afe83ac291",
        "project/53739b98d994972da7001d4a", null, null);
  
If the project used in a connection object does not belong to an existing organization but is one of the projects under the user's account, all the resources created or updated with that connection will also be assigned to the
specified project.

When the resource to be managed is a `project` itself, the connection needs to include the corresponding `organization ID`:

    BigMLClient api = new BigMLClient(
        "myusername", "ae579e7e53fb9abd646a6ff8aa99d4afe83ac291",
        "project/53739b98d994972da7001d4a", 
        "organization/53739b98d994972da7025d4a", null);



Alternative domains
------------

For [Virtual Private Cloud](https://bigml.com/pricing/vpc) setups, you can change the remote server URL to the VPC particular one by either setting the 
`BIGML_URL` in `binding.properties` or in the JVM environment.
By default, they have the following values:

    BIGML_URL=https://bigml.io/andromeda/

If you are in Australia or New Zealand, you can change them to:

    BIGML_URL=https://au.bigml.io/andromeda/

The corresponding SSL REST calls will be directed to your private domain
henceforth.
