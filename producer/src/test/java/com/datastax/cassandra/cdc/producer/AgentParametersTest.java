/**
 * Copyright DataStax, Inc 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.cassandra.cdc.producer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.datastax.cassandra.cdc.producer.ProducerConfig.*;

public class AgentParametersTest {

    @Test
    public void testConfigure() {
        String agentArgs =
                CDC_RELOCATION_DIR_SETTING + "=cdc_mybackup," +
                        ERROR_COMMITLOG_REPROCESS_ENABLED_SETTING + "=true," +
                        CDC_DIR_POOL_INTERVAL_MS_SETTING + "=1234," +
                        TOPIC_PREFIX_SETTING + "=events-mutations," +
                        PULSAR_SERVICE_URL_SETTING + "=pulsar://mypulsar:6650," +
                        KAFKA_BROKERS_SETTING + "=mykafka:9092," +
                        KAFKA_SCHEMA_REGISTRY_URL_SETTING + "=http://myregistry:8081";
        ProducerConfig.configure(null);
        ProducerConfig.configure(agentArgs);
        assertEquals(cdcRelocationDir, "cdc_mybackup");
        assertEquals(errorCommitLogReprocessEnabled, true);
        assertEquals(cdcDirPollIntervalMs, 1234L);
        assertEquals(topicPrefix, "events-mutations");
        assertEquals(pulsarServiceUrl, "pulsar://mypulsar:6650");
        assertEquals(kafkaBrokers, "mykafka:9092");
        assertEquals(kafkaSchemaRegistryUrl, "http://myregistry:8081");
    }
}
