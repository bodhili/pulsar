/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.broker.stats.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.Enumeration;
import org.apache.pulsar.common.util.SimpleTextOutputStream;

/**
 * Generate metrics in a text format suitable to be consumed by Prometheus.
 * Format specification can be found at {@link https://prometheus.io/docs/instrumenting/exposition_formats/}
 */
public class PrometheusMetricsGeneratorUtils {

    public static void generateSystemMetrics(SimpleTextOutputStream stream, String cluster) {
        Enumeration<Collector.MetricFamilySamples> metricFamilySamples =
                CollectorRegistry.defaultRegistry.metricFamilySamples();
        while (metricFamilySamples.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamily = metricFamilySamples.nextElement();

            // Write type of metric
            stream.write("# TYPE ").write(metricFamily.name).write(' ')
                    .write(getTypeStr(metricFamily.type)).write('\n');

            for (int i = 0; i < metricFamily.samples.size(); i++) {
                Collector.MetricFamilySamples.Sample sample = metricFamily.samples.get(i);
                stream.write(sample.name);
                if (!sample.labelNames.contains("cluster")) {
                    stream.write("{cluster=\"").write(cluster).write('"');
                }
                for (int j = 0; j < sample.labelNames.size(); j++) {
                    String labelValue = sample.labelValues.get(j);
                    if (labelValue != null) {
                        labelValue = labelValue.replace("\"", "\\\"");
                    }

                    stream.write(",");
                    stream.write(sample.labelNames.get(j));
                    stream.write("=\"");
                    stream.write(labelValue);
                    stream.write('"');
                }

                stream.write("} ");
                stream.write(Collector.doubleToGoString(sample.value));
                stream.write('\n');
            }
        }
    }

    static String getTypeStr(Collector.Type type) {
        switch (type) {
            case COUNTER:
                return "counter";
            case GAUGE:
                return "gauge";
            case SUMMARY        :
                return "summary";
            case HISTOGRAM:
                return "histogram";
            case UNTYPED:
            default:
                return "untyped";
        }
    }

}
