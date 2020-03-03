# BigML.io Java bindings

`BigML <https://bigml.com>`_ makes machine learning easy by taking care
of the details required to add data-driven decisions and predictive
power to your company. Unlike other machine learning services, BigML
creates
`beautiful predictive models <https://bigml.com/gallery/models>`_ that
can be easily understood and interacted with.

These BigML Java bindings allow you to interact with
`BigML.io <https://bigml.io/>`_, the API
for BigML. You can use it to easily create, retrieve, list, update, and
delete BigML resources (i.e., sources, datasets, models and,
predictions). For additional information, see
the `full documentation for the Java
bindings on Read the Docs <http://bigml-java.readthedocs.org>`_.

This module is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

See all the changes history [here](Changes.md).

## Support

Please, report problems and bugs to
[BigML.io-Java issue tracker](https://github.com/bigmlcom/bigml-java/issues)

You can send us an email at [BigML email support](mailto://support.bigml.com)

You can join us in [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0)


## Integrating Maven

Add the following dependency to your project's pom.xml file:

    <dependency>
        <groupId>org.bigml</groupId>
        <artifactId>bigml-binding</artifactId>
        <version>1.8.13</version>
    </dependency>

Add the following lines to your project's pom.xml file if you want to use the SNAPSHOT versions of the library:

    <repositories>
        <repository>
            <id>osshr-snapshots</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <snapshots><enabled>true</enabled></snapshots>
            <releases><enabled>false</enabled></releases>
        </repository>
    </repositories>

## Requirements

You will find in the `binding.properties` file where to setup your BigML
credentialsc`BIGML_USERNAME` and `BIGML_API_KEY`.
They can be overwritten passing the values as JVM variables with `-D`.

The project uses Maven as project manager.

## Running the Tests

There is a test suite using [Cucumber](http://cukes.info/) available,
you may want to run it by execute:

```bash
$ mvn test
```

or this way, if you want to debug the tests

```bash
$ mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" test
```

## Examples

The `samples` directory contains a maven project named `BigML-Sample-Client`
that can be imported. It shows some basic examples about how to use the
bindings to create resources in BigML.
See the corresponding [readme](samples/BigML-Sample-Client/README.md) for details.

## Generated JAR file of the bindings

Since version 1.1 the name of the JAR file is _bigml-binding_.
