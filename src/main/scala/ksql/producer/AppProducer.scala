package ksql.producer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object AppProducer {

  def main(args: Array[String]): Unit = {

    implicit val actorSystem = ActorSystem("vote_producer")
    implicit val ec = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer.create(actorSystem)

    println("Starting producer app ...")

    val voteProducer = new VoteProducer(actorSystem, "vote_topic")
    val future = voteProducer.populateTopics(getClass.getResourceAsStream("/votes.csv"))

    future.andThen {
      case _ =>
        println("Shutdown producer app ...")
        shutdown()
        println("Shutdown producer app ...OK")
    }

    def shutdown(): Unit = {
      Await.result(actorSystem.terminate(), Duration("10s"))
      ()
    }

  }
}
