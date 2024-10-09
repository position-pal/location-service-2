package io.gihub.positionpal.location.experiments.kafka

import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}

import java.util.Properties

@main def helloWorldStreams(): Unit =
  val topology = SimpleStreamConsumer.topology()
  val config = Properties()
  config.put(StreamsConfig.APPLICATION_ID_CONFIG, "simple-stream-consumer")
  config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092")
  config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")
  config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")
  val streams = KafkaStreams(topology, config)
  println("Starting Kafka Streams")
  streams.start()

object SimpleStreamConsumer:

  def topology(): Topology =
    val builder = StreamsBuilder()
    val stream = builder.stream("sampled_locations_1")
    stream.print(Printed.toSysOut.withLabel("sampled_locations_1-stream"))
    builder.build()
