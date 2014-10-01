# Changes in the BigML.io Java bindings

## 1.2 version

* Since version 1.2 we start to publish the release versions on the Maven Central Repository. Now, you can declare
the library dependency in your project's pom.xml.

```
    <dependency>
        <groupId>org.bigml</groupId>
        <artifactId>bigml-binding</artifactId>
        <version>1.2</version>
    </dependency>
```

* We have removed unnecessary dependencies with third party libraries, like Apache HttpClient and SLF4J-Log4j, making
a more lightweight library.

* If you were using the Apache HttpClient library in your own project, now you will need to explicitly declare the
dependencies to the libraries in your project's pom.xml file, because it's not anymore a dependency of this library.

* Support for json files in UTF-8 encoding. Now we can send json objects with UTF-8 characters inside them.

## 1.1 version

* Since version 1.1 the name of the JAR file is bigml-binding.

