package ksql.consumer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object AppConsumer {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem("vote_consumer")
    implicit val ec = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer.create(actorSystem)

    println("Starting consumer app ...")

    val voteConsumer = new VoteConsumer(actorSystem, "vote_topic")
    val future = voteConsumer.printVoteTopic

    future.andThen {
      case _ =>
        println("Shutdown consumer app ...")
        shutdown()
        println("Shutdown consumer app ...OK")
    }

    def shutdown(): Unit = {
      Await.result(actorSystem.terminate(), Duration("10s"))
      ()
    }
  }
}