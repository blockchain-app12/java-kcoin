# How to deploy java-kcoin after modularization

After modularization, java-kcoin is launched via shell script instead of typing command: `java -jar FullNode.jar`.

*`java -jar FullNode.jar` still works, but will be deprecated in future*.

## Download

```
git clone git@github.com:kcoinprotocol/java-kcoin.git
```

## Compile

Change to project directory and run:
```
./gradlew build
```
java-kcoin-1.0.0.zip will be generated in java-kcoin/build/distributions after compilation.

## Unzip

Unzip java-kcoin-1.0.0.zip
```
cd java-kcoin/build/distributions
unzip -o java-kcoin-1.0.0.zip
```
After unzip, two directories will be generated in java-kcoin: `bin` and `lib`, shell scripts are located in `bin`, jars are located in `lib`.

## Startup

Use the corresponding script to start java-kcoin according to the OS type, use `*.bat` on Windows, Linux demo is as below:
```
# default
java-kcoin-1.0.0/bin/FullNode

# using config file, there are some demo configs in java-kcoin/framework/build/resources
java-kcoin-1.0.0/bin/FullNode -c config.conf

# when startup with SR mode，add parameter: -w
java-kcoin-1.0.0/bin/FullNode -c config.conf -w
```

## JVM configuration

JVM options can also be specified, located in `bin/java-kcoin.vmoptions`:
```
# demo
-XX:+UseConcMarkSweepGC
-XX:+PrintGCDetails
-Xloggc:./gc.log
-XX:+PrintGCDateStamps
-XX:+CMSParallelRemarkEnabled
-XX:ReservedCodeCacheSize=256m
-XX:+CMSScavengeBeforeRemark
```