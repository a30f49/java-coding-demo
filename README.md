## create maven project
```
APP=java-coding-demo mvn archetype:generate -DgroupId=com.example -DartifactId=${APP} -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

or

```
sh scripts/create-maven-uqickstart-ap.sh
```

## add dependencies for http client, compile and assembly
 - dependency:okhttp
 - dependency:jackson-databind
 - dependency:log4j
 - plugin:maven-compiler-plugin
 - plugin:maven-assembly-plugin


## build maven project
```shell
mvn clean package
```


## run maven project
> via maven built-in plugin
```shell
mvn compile exec:java -Dexec.mainClass=com.candidate.demo.App
```
or 

> via maven assembly plugin
```shell 
java -jar target/java-coding-demo-1.0-SNAPSHOT.jar
```

