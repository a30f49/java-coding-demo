#!/bin/sh

app=java-coding-demo
mvn archetype:generate -DgroupId=com.example -DartifactId=$app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
