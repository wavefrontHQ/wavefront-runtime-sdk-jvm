package com.wavefront.sdk.appagent.jvm.reporter;

import com.wavefront.internal.reporter.WavefrontInternalReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.common.application.HeartbeaterService;
import com.wavefront.sdk.entities.metrics.WavefrontMetricSender;

import javax.annotation.Nullable;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wavefront.sdk.appagent.jvm.Constants.JVM_COMPONENT;
import static com.wavefront.sdk.common.Constants.APPLICATION_TAG_KEY;
import static com.wavefront.sdk.common.Constants.CLUSTER_TAG_KEY;
import static com.wavefront.sdk.common.Constants.NULL_TAG_VAL;
import static com.wavefront.sdk.common.Constants.SERVICE_TAG_KEY;
import static com.wavefront.sdk.common.Constants.SHARD_TAG_KEY;

/**
 * Wavefront JVM reporter that reports JVM related metrics from your application to Wavefront.
 *
 * @author Sushant Dewan (sushant@wavefront.com).
 */
public class WavefrontJvmReporter implements Closeable {

  private final WavefrontInternalReporter wfReporter;
  private final int reportingIntervalSeconds;
  private final HeartbeaterService heartbeaterService;

  private WavefrontJvmReporter(WavefrontInternalReporter wfReporter,
                               int reportingIntervalSeconds,
                               WavefrontMetricSender wavefrontMetricSender,
                               ApplicationTags applicationTags,
                               String source) {
    this.wfReporter = wfReporter;
    this.reportingIntervalSeconds = reportingIntervalSeconds;
    heartbeaterService = new HeartbeaterService(wavefrontMetricSender, applicationTags,
            Collections.singletonList(JVM_COMPONENT), source);
  }

  /**
   * Start the JVM reporter so that it can periodically report JVM metrics to Wavefront.
   */
  public void start() {
    wfReporter.start(reportingIntervalSeconds, TimeUnit.SECONDS);
  }

  /**
   * Stop the reporter. Invoke this method before your JVM shuts down.
   */
  public void stop() {
    wfReporter.stop();
    heartbeaterService.close();
  }

  @Override
  public void close() {
    this.stop();
  }

  public void report() {
    this.wfReporter.report();
  }

  public static class Builder {
    // Required parameters
    private final ApplicationTags applicationTags;
    private final String prefix = "app-agent";

    // Optional parameters
    private int reportingIntervalSeconds = 60;

    @Nullable
    private String source;

    /**
     * Builder to build WavefrontJvmReporter.
     *
     * @param applicationTags  metadata about your application that you want to be propagated as
     *                         tags when metrics/histograms are sent to Wavefront.
     */
    public Builder(ApplicationTags applicationTags) {
      this.applicationTags = applicationTags;
    }

    /**
     * Set reporting interval i.e. how often you want to report the metrics/histograms to
     * Wavefront.
     *
     * @param reportingIntervalSeconds reporting interval in seconds.
     * @return {@code this}.
     */
    public Builder reportingIntervalSeconds(int reportingIntervalSeconds) {
      this.reportingIntervalSeconds = reportingIntervalSeconds;
      return this;
    }

    /**
     * Set the source tag for your metric and histograms.
     *
     * @param source Name of the source/host where your application is running.
     * @return {@code this}.
     */
    public Builder withSource(String source) {
      this.source = source;
      return this;
    }

    /**
     * Build WavefrontJvmReporter.
     *
     * @param wavefrontSender send data to Wavefront via proxy or direct ingestion.
     * @return An instance of {@link WavefrontJvmReporter}.
     */
    public WavefrontJvmReporter build(WavefrontSender wavefrontSender) {
      if (source == null) {
        try {
          source = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
          // Should never happen
          source = "unknown";
        }
      }

      Map<String, String> pointTags = new HashMap<>();
      pointTags.put(APPLICATION_TAG_KEY, applicationTags.getApplication());
      pointTags.put(SERVICE_TAG_KEY, applicationTags.getService());
      pointTags.put(CLUSTER_TAG_KEY,
              applicationTags.getCluster() == null ? NULL_TAG_VAL : applicationTags.getCluster());
      pointTags.put(SHARD_TAG_KEY,
              applicationTags.getShard() == null ? NULL_TAG_VAL : applicationTags.getShard());
      if (applicationTags.getCustomTags() != null) {
        pointTags.putAll(applicationTags.getCustomTags());
      }

      WavefrontInternalReporter wfReporter = new WavefrontInternalReporter.Builder().
          prefixedWith(prefix).withSource(source).withReporterPointTags(pointTags).
          includeJvmMetrics().build(wavefrontSender);
      return new WavefrontJvmReporter(wfReporter, reportingIntervalSeconds, wavefrontSender,
          applicationTags, source);
    }
  }
}
