# Wavefront JVM SDK

This SDK provides support for reporting JVM metrics for your JVM application. That data is reported to Wavefront via proxy or direct ingestion. That data will help you understand how your JVM application is performing in production.

## Usage
If you are using Maven, add following maven dependency to your pom.xml
```
<dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-appagent-sdk-jvm</artifactId>
    <version>0.9.0</version>
</dependency>
```

### Application Tags
Before you configure the SDK you need to decide the metadata that you wish to emit for those out of the box metrics and histograms. Each and every application should have the application tag defined. If the name of your application is Ordering application, then you can put that as the value for that tag.
```java
    /* Set the name of your JVM application that you wish to monitor */
    String application = "OrderingApp";
```
Let's say your application is composed of microservices. Each and every microservice in your application should have the service tag defined.
```java
    /* Set the name of your service, 
     * for instance - 'inventory' service for your OrderingApp */
    String service = "inventory";
```

You can also define optional tags (cluster and shard).
```java
    /* Optional cluster field, set it to 'us-west-2', assuming
     * your app is running in 'us-west-2' cluster */
    String cluster = "us-west-2";

    /* Optional shard field, set it to 'secondary', assuming your 
     * application has 2 shards - primary and secondary */
    String shard = "secondary";
```

You can add optional custom tags for your application.
```java
    /* Optional custom tags map */
    Map<String, String> customTags = new HashMap<String, String>() {{
      put("location", "Oregon");
      put("env", "Staging");
    }};
```
You can define the above metadata in your application YAML config file.
Now create ApplicationTags instance using the above metatdata.
```java
    /* Create ApplicationTags instance using the above metadata */
    ApplicationTags applicationTags = new ApplicationTags.Builder(application, service).
        cluster(cluster).shard(shard).customTags(customTags).build();
```

### WavefrontSender
We need to instantiate WavefrontSender 
(i.e. either WavefrontProxyClient or WavefrontDirectIngestionClient)
Refer to this page (https://github.com/wavefrontHQ/wavefront-sdk-java#wavefrontsender)
to instantiate WavefrontProxyClient or WavefrontDirectIngestionClient.
<br />
<br />
**Note:** If you are using more than one Wavefront SDK (i.e. wavefront-opentracing-sdk-java, wavefront-dropwizard-metrics-sdk-java, wavefront-jersey-sdk-java, wavefront-grpc-sdk-java etc.) that requires you to instantiate WavefrontSender, then you should instantiate the WavefrontSender only once and share that sender instance across multiple SDKs inside the same JVM.
If the SDKs will be installed on different JVMs, then you would need to instantiate one WavefrontSender per JVM.

### WavefrontJvmReporter
```java

    /* Create WavefrontJvmReporter.Builder using applicationTags. */
    WavefrontJvmReporter.Builder builder = new WavefrontJvmReporter.Builder(applicationTags);

    /* Set the source for your metrics and histograms */
    builder.withSource("mySource");

    /* Optionally change the reporting frequency to 30 seconds, defaults to 1 min */
    builder.reportingIntervalSeconds(30);

    /* Create a WavefrontJvmReporter using ApplicationTags metadata and WavefronSender */
    WavefrontJvmReporter wfJvmReporter = new WavefrontJvmReporter.
        Builder(applicationTags).build(wavefrontSender);
```

### Starting and stopping the reporter
```java
    /* After instantiating, start the reporter */
    wfJvmReporter.start();
    
    /* Before shutting down your JVM, stop your reporter */
    wfJvmReporter.stop();
```

### JVM metrics

You can go to Wavefront and see the metrics flowing with the prefix - `app-agent.jvm.*`
