# BigML.io Java bindings

In this repository you'll find an open source Java client that gives
you a simple binding to interact with [BigML](https://bigml.io). You
can use it to easily create, retrieve, list, update, and delete BigML
resources (i.e., sources, datasets, models, ensembles, clusters, 
predictions, centroids, batch predictions, batch centroids, evaluations).

This client is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

See the changes [here](Changes.md).

## Support

Please, report problems and bugs to 
[BigML.io-Java issue tracker](https://github.com/javinp/bigml-java/issues)

Discussions about the different bindings take place in the general
[BigML mailing list](http://groups.google.com/group/bigml). Or join us
in [Campfire chatroom](https://bigmlinc.campfirenow.com/f20a0)

## Integrating Maven

Add the following dependency to your project's pom.xml file:

    <dependency>
        <groupId>org.bigml</groupId>
        <artifactId>bigml-binding</artifactId>
        <version>1.2</version>
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

You will find in the `binding.properties` file where to setup your BigML credentials
`BIGML_USERNAME` and `BIGML_API_KEY`. They can be overwritten passing
the values as JVM variables with `-D`.

The project uses Maven as project manager.

## Running the Tests

There is a test suite using [Cucumber](http://cukes.info/) available,
you may want to run it by execute:

```bash
$ mvn test
```
## Generated JAR file of the bindings

Since version 1.1 the name of the JAR file is _bigml-binding_.
