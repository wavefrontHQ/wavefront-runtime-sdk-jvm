# Wavefront JVM SDK [![build status][ci-img]][ci] [![Released Version][maven-img]][maven]

The Wavefront by VMware JVM SDK provides out of the box metrics for the Java Virtual Machine (JVM) that runs your Java application. You can analyze the data in [Wavefront](https://www.wavefront.com) to better understand how your application is performing in production.

## Maven
If you are using Maven, add the following maven dependency to your pom.xml:
```xml
<dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-runtime-sdk-jvm</artifactId>
    <version>$releaseVersion</version>
</dependency>
```

Replace `$releaseVersion` with the latest version available on [maven](http://search.maven.org/#search%7Cga%7C1%7Cwavefront-runtime-sdk-jvm).

## Set Up a WavefrontJvmReporter
This SDK provides a `WavefrontJvmReporter` for collecting JVM metrics.

To create a `WavefrontJvmReporter`:
1. Create an `ApplicationTags` instance, which specifies metadata metadata about your application.
2. Create a `WavefrontSender` for sending data to Wavefront.
3. Create a `WavefrontJvmReporter` instance.

For the details of each step, see the sections below.

### 1. Set Up Application Tags

The application tags determine the metadata (point tags) that are included with the JVM metrics reported to Wavefront. These tags enable you to filter and query the reported JVM metrics in Wavefront.

You encapsulate application tags in an `ApplicationTags` object.
See [Instantiating ApplicationTags](https://github.com/wavefrontHQ/wavefront-sdk-doc-sources/blob/master/java/applicationtags.md#application-tags) for details.

### 2. Set Up a WavefrontSender

A `WavefrontSender` object implements the low-level interface for sending data to Wavefront. You can choose to send data using either the [Wavefront proxy](https://docs.wavefront.com/proxies.html) or [direct ingestion](https://docs.wavefront.com/direct_ingestion.html).

* If you have already set up a `WavefrontSender` for another SDK that will run in the same JVM, use that one.  (For details about sharing a `WavefrontSender` instance, see [Share a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-doc-sources/blob/master/java/wavefrontsender.md#share-a-wavefrontsender).)

* Otherwise, follow the steps in [Set Up a WavefrontSender](https://github.com/wavefrontHQ/wavefront-sdk-doc-sources/blob/master/java/wavefrontsender.md#wavefrontsender).


### 3. Create the WavefrontJvmReporter
A `WavefrontJvmReporter` reports metrics to Wavefront.

To build a `WavefrontJvmReporter`, you must specify:
* An `ApplicationTags` object.
* A `WavefrontSender` object.

You can optionally specify:
* A nondefault source for the reported data. If you omit the source, the host name is automatically used. The source should be identical across all the Wavefront SDKs running in the same JVM.
* A nondefault reporting interval, which controls how often data is reported to the `WavefrontSender`. The reporting interval determines the timestamps on the data sent to Wavefront. If you omit the reporting interval, data is reported once a minute.

```java

ApplicationTags applicationTags = buildTags(); // pseudocode; see above
WavefrontSender wavefrontSender = buildWavefrontSender(); // pseudocode; see above

// Create WavefrontJvmReporter.Builder using applicationTags
WavefrontJvmReporter.Builder wfJvmReporterBuilder = new WavefrontJvmReporter.Builder(applicationTags);

// Optionally set the source name to "mySource" for your metrics and histograms.
// Omit this statement to use the host name.
wfJvmReporterBuilder.withSource("mySource");

// Optionally change the reporting interval to 30 seconds. Default is 1 minute.
wfJvmReporterBuilder.reportingIntervalSeconds(30);

// Create a WavefrontJvmReporter with the WavefronSender.
WavefrontJvmReporter wfJvmReporter = wfJvmReporterBuilder.build(wavefrontSender);
```

## Start the WavefrontJvmReporter
You start the `WavefrontJvmReporter` explicitly to start reporting JVM metrics.

```java
// Start the reporter
wfJvmReporter.start();
```

## Stop the WavefrontJvmReporter
You must explicitly stop the `WavefrontJvmReporter` before shutting down your JVM.

```java
// Stop the reporter
wfJvmReporter.stop();
```

## JVM metrics

You can go to Wavefront and see the JVM metrics with the prefix `app-agent.jvm.*`.

[ci-img]: https://travis-ci.com/wavefrontHQ/wavefront-runtime-sdk-jvm.svg?branch=master
[ci]: https://travis-ci.com/wavefrontHQ/wavefront-runtime-sdk-jvm
[maven-img]: https://img.shields.io/maven-central/v/com.wavefront/wavefront-runtime-sdk-jvm.svg?maxAge=604800
[maven]: http://search.maven.org/#search%7Cga%7C1%7Cwavefront-runtime-sdk-jvm
