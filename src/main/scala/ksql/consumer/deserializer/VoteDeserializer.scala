package ksql.consumer.deserializer

import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import io.circe.parser._

import ksql.models.Vote

class VoteDeserializer extends Deserializer[Vote] {

  val stringDeserializer = new StringDeserializer

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
    stringDeserializer.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]): Vote = {
    val jsonStr = stringDeserializer.deserialize(topic, data)
    parse(jsonStr).right.flatMap(_.as[Vote]).toTry.get // unsafe
  }

  override def close(): Unit = stringDeserializer.close()
}
