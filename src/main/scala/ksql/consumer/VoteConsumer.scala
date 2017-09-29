package ksql.consumer

import scala.concurrent.Future

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import ksql.consumer.deserializer.VoteDeserializer

class VoteConsumer(actorSystem: ActorSystem, topicName: String) {

  private val consumerSettings = ConsumerSettings(actorSystem, new StringDeserializer, new VoteDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def printVoteTopic(implicit mat: Materializer): Future[Done] = {
    Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName))
       .map(msg => println(msg.record))
      .runWith(Sink.ignore)
  }
}
