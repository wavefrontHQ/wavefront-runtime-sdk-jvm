# Wavefront JVM SDK

This SDK provides out of the box JVM metrics for your Java application. The data can be sent to Wavefront using either the [proxy](https://docs.wavefront.com/proxies.html) or [direct ingestion](https://docs.wavefront.com/direct_ingestion.html). You can analyze the data in [Wavefront](https://www.wavefront.com) to better understand how your application is performing in production.

## Maven
If you are using Maven, add following maven dependency to your pom.xml:
```
<dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-appagent-sdk-jvm</artifactId>
    <version>0.9.0</version>
</dependency>
```

## WavefrontJvmReporter
This SDK provides a `WavefrontJvmReporter` for collecting JVM metrics.

To create a `WavefrontJvmReporter`:
1. Create an instance of `ApplicationTags` providing metadata about your application
2. Create a `WavefrontSender`: a low-level interface that handles sending data to Wavefront
3. Finally create a `WavefrontJvmReporter`

The sections below detail each of the above steps.

### 1. Application Tags

The application tags determine the metadata (aka point tags) that are included with the JVM metrics reported to Wavefront.

The following tags are mandatory:
* `application`: The name of your application, for example: `OrderingApp`.
* `service`: The name of the microservice within your application, for example: `inventory`.

The following tags are optional:
* `cluster`: For example: `us-west-2`.
* `shard`: The shard (aka mirror), for example: `secondary`.

You can also optionally add custom tags specific to your application in the form of a `HashMap` (see example below).

To create the application tags:
```java
String application = "OrderingApp";
String service = "inventory";
String cluster = "us-west-2";
String shard = "secondary";

Map<String, String> customTags = new HashMap<String, String>() {{
  put("location", "Oregon");
  put("env", "Staging");
}};

ApplicationTags applicationTags = new ApplicationTags.Builder(application, service).
    cluster(cluster).       // optional
    shard(shard).           // optional
    customTags(customTags). // optional
    build();
```

You would typically define the above metadata in your application's YAML config file and create the `ApplicationTags`.

### 2. WavefrontSender

The `WavefrontJvmReporter` requires a WavefrontSender: A low-level interface that knows how to send data to Wavefront. There are two implementations of the Wavefront sender:

* `WavefrontProxyClient`: To send data to the Wavefront proxy
* `WavefrontDirectIngestionClient`: To send data to Wavefront using direct ingestion

See the [Wavefront sender documentation](https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/README.md#wavefrontsender) for details on instantiating a proxy or direct ingestion client.

**Note:** When using more than one Wavefront SDK (i.e. wavefront-opentracing-sdk-java, wavefront-dropwizard-metrics-sdk-java, wavefront-jersey-sdk-java, wavefront-grpc-sdk-java etc.), then you should instantiate the WavefrontSender only once within the same JVM process.
If the SDKs are used on different JVM processes, then you should instantiate one WavefrontSender per JVM.

### 3. Create WavefrontJvmReporter
To create the `WavefrontJvmReporter`:
```java
// Create WavefrontJvmReporter.Builder using applicationTags
WavefrontJvmReporter.Builder builder = new WavefrontJvmReporter.Builder(applicationTags);

// Optinal: Set the source for your metrics and histograms
// Defaults to hostname if omitted
builder.withSource("mySource");

// Optional: change the reporting frequency to 30 seconds, defaults to 1 min
builder.reportingIntervalSeconds(30);

// Create a WavefrontJvmReporter using ApplicationTags metadata and WavefronSender
WavefrontJvmReporter wfJvmReporter = new WavefrontJvmReporter.
    Builder(applicationTags).
    build(wavefrontSender);
```
Replace the source `mySource` with a relevant source name. The source should be identical across all the Wavefront SDKs.

### Starting and stopping the reporter
```java
// After instantiating, start the reporter
wfJvmReporter.start();

// Before shutting down your JVM, stop your reporter
wfJvmReporter.stop();
```

## JVM metrics

You can go to Wavefront and see the JVM metrics with the prefix `app-agent.jvm.*`.
