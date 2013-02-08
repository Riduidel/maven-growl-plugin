[![Build Status](https://buildhive.cloudbees.com/job/Riduidel/job/maven-growl-plugin/badge/icon)](https://buildhive.cloudbees.com/job/Riduidel/job/maven-growl-plugin/)

# maven-growl-plugin #

A set of maven tools allowing growl messages sending.

As this project started as a maven plugin, the github name wasn't changed. However this project contains more than a simple maven-growl-plugin allowing arbitrary message sending. There is also a maven extension (or lifecycle spy) listening to maven build in order to fire messages.

Notification quantity and quality is configurable, of course **not**.

The base class should extend [AbstractExecutionListener](http://maven.apache.org/ref/3.0.4/apidocs/org/apache/maven/execution/AbstractExecutionListener.html). Sample usage code can be found on [GrepCode](http://grepcode.com/search/usages?id=repo1.maven.org$maven2@org.apache.maven$maven-core@3.0.4@org$apache$maven$execution@AbstractExecutionListener&type=type&k=u)

@olamy produced a gist explaining the whole thing here https://gist.github.com/4708014
