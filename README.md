[![Build Status](https://buildhive.cloudbees.com/job/Riduidel/job/maven-growl-plugin/badge/icon)](https://buildhive.cloudbees.com/job/Riduidel/job/maven-growl-plugin/)

# maven-growl-plugin #

A set of maven tools allowing growl messages sending.

As this project started as a maven plugin, the github name wasn't changed. However this project contains more than a simple maven-growl-plugin allowing arbitrary message sending. There is also a maven extension (or lifecycle spy) listening to maven build in order to fire messages.

Notification quantity and quality is configurable, of course **not**.

# Plugin how-to #

Want to test the plugin the simplest way ? well, go in any folder and type

    mvn fr.perigee.java:maven-growl-plugin:message

If your machine has growl installed, things should happen.

Want to customize some things ? Well, beside `mvn help:describe`, I would suggest you to type

    mvn fr.perigee.java:maven-growl-plugin:message -Dgrowl.title="A yummy notification" -Dgrowl.message="Yes, that notification is even better than chunky bacon"

I guess things are quite obvious now ... No ?

# Extension how-to #

Perform a full build of maven-growl-plugin by running

    mvn clean install

Then go in `maven-growl-extension/target`, pick the `maven-growl-extension-${project.version}-release.jar` and drop that jar in your `$MVN_HOME\lib\ext` folder. Once all bugs are fixed, it could provide you some nice messages during build ...

# History #

I was fed up with those build ending silently and never stopping my procrastination. In the same time, growl send me nice notifications from Pidgin, Foobar2000 and others. I added 1 and 1 in my brain and decided it would be cool to have maven send me nice messages during build. So I started. Writing the maven plugin was straightforward, but it didn't allow me to follow any build, which was stupid. So I left the whole thing aside.

Then, one day, @olamy told me it was possible to use maven extension to follow any running build. He goes as far as writing a [gist](https://gist.github.com/4708014) explaining the whole thing. And voila !
