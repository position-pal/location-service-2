package io.gihub.positionpal.location.experiments.kafka

import org.apache.kafka.clients.CommonClientConfigs.*

object Configurations:

  val sampledLocationsTopic = "sampled_locations_"
  val kafkaBroker = "localhost:9092"

  val producerConfiguration: Map[String, String] = Map(
    BOOTSTRAP_SERVERS_CONFIG -> kafkaBroker,
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
  )

  val consumerConfiguration: Map[String, String] = Map(
    BOOTSTRAP_SERVERS_CONFIG -> kafkaBroker,
    "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
    "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
    GROUP_ID_CONFIG -> "location-service",
  )
