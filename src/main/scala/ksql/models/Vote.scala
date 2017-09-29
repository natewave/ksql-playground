package ksql.models

import scala.collection.mutable
import scala.util.{Success, Try}

import io.circe.generic.semiauto.{deriveDecoder, _}
import io.circe.{Decoder, _}

case class VoteRatio(value: Int, registeredRatio: Double, votersRatio: Double)

object VoteRatio {
  implicit val jsonEncoder: Encoder[VoteRatio] = deriveEncoder
  implicit val jsonDecoder: Decoder[VoteRatio] = deriveDecoder

  def parseCsvRow(array: Array[String]): Try[VoteRatio] = {
    for {
      value <- Try(array(0).toInt)
      registeredRatio <- Try(array(1).toDouble)
      votersRatio <- Try(array(2).toDouble)
    } yield VoteRatio(value, registeredRatio, votersRatio)
  }
}

case class Candidate(
  pollsNumber: Int,
  gender: Char,
  lastName: String,
  firstName: String,
  numberOfVotes: Int,
  registeredRatio: Double,
  votesCastRatio: Double
)

object Candidate {
  implicit val jsonEncoder: Encoder[Candidate] = deriveEncoder

  implicit val jsonDecoder: Decoder[Candidate] = deriveDecoder

  def parseCsvRow(array: Array[String]): Try[Candidate] = {
    for {
      pollsNumber <- Try(array(0).toInt)
      gender <- Try(array(1).charAt(0))
      lastName <- Try(array(2))
      firstName <- Try(array(3))
      numberVotes <- Try(array(4).toInt)
      registeredRatio <- Try(array(5).toDouble)
      votersCastRatio <- Try(array(6).toDouble)
    } yield Candidate(pollsNumber, gender, lastName, firstName, numberVotes, registeredRatio, votersCastRatio)
  }

}

case class Locality(departmentCode: Int, departmentName: String, cityCode: Int, cityName: String)

object Locality {
  implicit val jsonEncoder: Encoder[Locality] = deriveEncoder

  implicit val jsonDecoder: Decoder[Locality] = deriveDecoder

  def parseCsvRow(array: Array[String]): Try[Locality] = {
    for {
      departmentCode <- Try(array(0).toInt)
      departmentName <- Try(array(1))
      cityCode <- Try(array(2).toInt)
      cityName <- Try(array(3))
    } yield Locality(departmentCode, departmentName, cityCode, cityName)
  }

}

case class Vote(
  locality: Locality,
  registered: Int,
  abstention: Int,
  abstentionRatio: Double,
  voters: Int,
  votersRatio: Double,
  blank: VoteRatio,
  invalid: VoteRatio,
  votesCast: VoteRatio,
  candidate: Map[String, Candidate]
)

object Vote {

  implicit val jsonEncoder: Encoder[Vote] = deriveEncoder
  implicit val jsonDecoder: Decoder[Vote] = deriveDecoder

  def parseCsvRow(row: String): Try[Vote] = {
    Try {
      row.split(";")
    }.flatMap { array =>
      for {
        locality <- Locality.parseCsvRow(array.take(4))
        registered <- Try(array(4).toInt)
        abstention <- Try(array(5).toInt)
        abstentionRatio <- Try(array(6).toDouble)
        voters <- Try(array(7).toInt)
        votersRatio <- Try(array(8).toDouble)
        blank <- VoteRatio.parseCsvRow(array.slice(9, 12))
        invalid <- VoteRatio.parseCsvRow(array.slice(12, 15))
        votesCast <- VoteRatio.parseCsvRow(array.slice(15, 18))
        candidates <- parseCandidates(array.slice(18, array.length))
      } yield Vote(
          locality, registered, abstention, abstentionRatio, voters, votersRatio, blank, invalid, votesCast, candidates
        )
    }
  }

  private def parseCandidates(array: Array[String]): Try[Map[String, Candidate]] = {

    type A = mutable.Builder[(String, Candidate), Map[String, Candidate]]

    array.grouped(7).map { subArray =>
      for {
        lastName <- Try(subArray(2))
        firstName <- Try(subArray(3))
        candidate <- Candidate.parseCsvRow(subArray)
      } yield s"$lastName-$firstName" -> candidate
    }.foldLeft[Try[A]](Success(Map.newBuilder[String, Candidate])) { case (acc, current) =>
      for {
        map <- acc
        (key, candidate) <- current
      } yield map += (key -> candidate)
    }.map(_.result())
  }
}
