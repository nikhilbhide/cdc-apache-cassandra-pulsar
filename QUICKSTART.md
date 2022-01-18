# Docker Quick Start

## Build docker images

Build docker images with CDC enabled:

    ./gradlew clean build -x test

## Start Cassandra and Pulsar

Start containers for Cassandra 4.0 and Apache Pulsar:

    ./gradlew agent-c4-pulsar:composeUp

Create the keyspace and table:

    docker exec -it cassandra cqlsh -e "CREATE KEYSPACE ks1 WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};"
    docker exec -it cassandra cqlsh -e "CREATE TABLE ks1.table1 (a text, b text, PRIMARY KEY (a)) WITH cdc=true;"

Deploy a Cassandra Source Connector in the pulsar container:

    docker exec -it pulsar bin/pulsar-admin source create \
    --source-type cassandra-source \
    --tenant public \
    --namespace default \
    --name cassandra-source-ks1-table1 \
    --destination-topic-name data-ks1.table1 \
    --source-config "{
      \"keyspace\": \"ks1\",
      \"table\": \"table1\",
      \"events.topic\": \"persistent://public/default/events-ks1.table1\",
      \"events.subscription.name\": \"sub1\",
      \"contactPoints\": \"cassandra\",
      \"loadBalancing.localDc\": \"datacenter1\"
    }"

Check the source connector status (should be running):

    docker exec -it pulsar bin/pulsar-admin source status --name cassandra-source-ks1-table1

Check the source connector logs:

    docker exec -it pulsar cat /pulsar/logs/functions/public/default/cassandra-source-ks1-table1/cassandra-source-ks1-table1-0.log

## Elasticsearch sink

Start elasticsearch and kibana containers:

    ./gradlew agent-c4-pulsar:elasticsearchComposeUp

Deploy an Elasticsearch sink connector:

    docker exec -it pulsar bin/pulsar-admin sink create \
    --sink-type elastic_search \
    --tenant public \
    --namespace default \
    --name es-sink-ks1-table1 \
    --inputs "persistent://public/default/data-ks1.table1" \
    --subs-position Earliest \
    --sink-config "{
      \"elasticSearchUrl\":\"http://elasticsearch:9200\",
      \"indexName\":\"ks1.table1\",
      \"keyIgnore\":\"false\",
      \"nullValueAction\":\"DELETE\",
      \"schemaEnable\":\"true\"
    }"

Check the sink connector status (should be running):

    docker exec -it pulsar bin/pulsar-admin sink status --name es-sink-ks1-table1

Check the source connector logs:

    docker exec -it pulsar cat /pulsar/logs/functions/public/default/es-sink-ks1-table1/es-sink-ks1-table1.log

Insert data into Cassandra table:
    
    docker exec -it cassandra cqlsh -e "INSERT INTO ks1.table1 (a, b) VALUES ('Test1', 'Example1')"

Check data are replicated in [elasticsearch](http://localhost:9200/_cat/indices):

    curl http://localhost:9200/ks1.table1/_search?pretty

You should notice following output:

    {
      "took" : 2,
      "timed_out" : false,
      "_shards" : {
        "total" : 1,
        "successful" : 1,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {
        "total" : {
          "value" : 1,
          "relation" : "eq"
        },
        "max_score" : 1.0,
        "hits" : [
          {
            "_index" : "ks1.table1",
            "_type" : "_doc",
            "_id" : "test1",
            "_score" : 1.0,
            "_source" : {
              "b" : "example1"
            }
          }
        ]
      }
    }

Update data into Cassandra table:
   
   docker exec -it cassandra cqlsh -e "UPDATE ks1.table1 SET b = 'example2' WHERE a = 'test1'"

Check data is updated in elasticsearch:

    {
      "took" : 1,
      "timed_out" : false,
      "_shards" : {
        "total" : 1,
        "successful" : 1,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {
        "total" : {
          "value" : 1,
          "relation" : "eq"
        },
        "max_score" : 1.0,
        "hits" : [
          {
            "_index" : "ks1.table1",
            "_type" : "_doc",
            "_id" : "test1",
            "_score" : 1.0,
            "_source" : {
              "b" : "example2"
            }
          }
        ]
      }
    }

Delete data from Cassandra table:
   
   docker exec -it cassandra cqlsh -e "DELETE FROM ks1.table1 where a = 'test1'"

Check Elasticsearch to confirm that data is deleted:

    {
      "took" : 807,
      "timed_out" : false,
      "_shards" : {
        "total" : 1,
        "successful" : 1,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {
        "total" : {
          "value" : 0,
          "relation" : "eq"
        },
        "max_score" : null,
        "hits" : [ ]
      }
    }

## Shutdown containers

    ./gradlew agent-c4-pulsar:composeDown
