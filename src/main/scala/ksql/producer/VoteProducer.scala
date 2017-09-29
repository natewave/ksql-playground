package ksql.producer

import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.concurrent.Future
import scala.util.Success

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Framing, StreamConverters}
import akka.util.ByteString
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import ksql.models.Vote
import ksql.producer.serializer.VoteSerializer

class VoteProducer(actorSystem: ActorSystem, topicName: String) {

  private val kafkaProducerSettings: ProducerSettings[String, Vote] = ProducerSettings(
    actorSystem, new StringSerializer, new VoteSerializer
  )

  val kafkaProducer: KafkaProducer[String, Vote] = kafkaProducerSettings.createKafkaProducer()

  private def convertToVote = Flow[String].map { row => Vote.parseCsvRow(row) }

  def populateTopics(input: InputStream)(implicit m: Materializer): Future[Done] = {
      StreamConverters
        .fromInputStream(() => input)
        .via(Framing.delimiter(ByteString("\n"), 1024))
        .map(_.utf8String)
        .map(_.replace(",", ".")) // replace decimal number separator
        .drop(1)
        .take(10)
        .via(convertToVote)
        .collect{ case Success(x) => x }
        .map { vote =>
          new ProducerRecord(topicName, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE), vote)
        }
        .runWith(Producer.plainSink(kafkaProducerSettings, kafkaProducer))
  }
}
