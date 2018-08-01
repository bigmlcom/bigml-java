Running the tests
=====================

There is a test suite using [Cucumber](http://cukes.info/) available,
you may want to run it by execute:

```bash
$ mvn test
```

or this way, if you want to debug the tests

```bash
$ mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" test
```

or this way, if you want run an specific feature

```bash
$ mvn test -Dcucumber.options="--glue classpath:org.bigml.binding --format pretty src/test/resources/test_01_prediction.feature"
```
