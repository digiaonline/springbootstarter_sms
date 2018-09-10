#!/bin/bash

mvn package && mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=target/sms-auth-spring-boot-starter-0.0.2-SNAPSHOT.jar.original -DpomFile=pom.xml
