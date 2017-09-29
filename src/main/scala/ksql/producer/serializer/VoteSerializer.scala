package ksql.producer.serializer

import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

import io.circe.syntax._
import ksql.models.Vote

class VoteSerializer extends Serializer[Vote] {

  val stringSerializer = new StringSerializer

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: Vote): Array[Byte] = {
    stringSerializer.serialize(topic, data.asJson.noSpaces)
  }

  override def close(): Unit = stringSerializer.close()
}
