package eu.maksat

import java.time.LocalDate

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.pattern.{ ask, pipe }
import akka.routing.RoundRobinPool
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.Sink
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.util.Timeout
import scalaz.Scalaz._
import scalaz._

import scala.concurrent.Future
import scala.concurrent.duration._

final case class Evaluate(urls: List[String])

final case class Speech(speaker: String, theme: String, date: LocalDate, words: Int)

final case class EvaluationResult(mostSpeeches: Option[String], mostSecurity: Option[String], leastWordy: Option[String])

class EvaluationActor extends Actor with ActorLogging {

  import context.dispatcher

  implicit lazy val timeout = Timeout(60.seconds)
  lazy val relevantYear = context.system.settings.config.getInt("evaluation.relevantYear")
  lazy val relevantTheme = context.system.settings.config.getString("evaluation.relevantTheme")
  val router: ActorRef = context.actorOf(RoundRobinPool(10).props(Props[Worker]))

  def receive: Receive = {
    case Evaluate(urls) =>
      val future: Future[List[Map[String, List[Speech]]]] = Future.sequence(urls.map(ask(context.actorOf(Props[Worker]), _).mapTo[Map[String, List[Speech]]]))

      val result: Future[EvaluationResult] = future.map { el =>
        val merged = el.reduce((x, y) => merge(x, y))
        log.info(merged.mkString("\n", "\n", "\n"))

        val minWordy = merged.mapValues(_.map(_.words).sum).groupBy(_._2).minBy(_._1)
        val leastWordy = if (minWordy._2.size > 1) None else Some(minWordy._2.keys.head)

        val maxSpeeches = merged.mapValues(_.filter(_.date.getYear == relevantYear).size).groupBy(_._2).maxBy(_._1)
        val mostSpeeches = if (maxSpeeches._2.size > 1) None else Some(maxSpeeches._2.keys.head)

        val maxSecurity = merged.mapValues(_.filter(_.theme.equals(relevantTheme)).size).groupBy(_._2).maxBy(_._1)
        val mostSecurity = if (maxSecurity._2.size > 1) None else Some(maxSecurity._2.keys.head)

        EvaluationResult(mostSpeeches, mostSecurity, leastWordy)
      }
      result.pipeTo(sender())
  }

  private def merge[K, V: Semigroup](lhs: Map[K, V], rhs: Map[K, V]): Map[K, V] = lhs.foldLeft(rhs) {
    case (acc, (k, v)) => acc.updated(k, combine(v, acc.get(k)))
  }

  private def combine[A: Semigroup](a: A, opt: Option[A]): A = opt.map(a |+| _).getOrElse(a)

}

/**
 * Worker class.
 */
class Worker extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  override def receive: Receive = {
    case uri: String =>
      val future: Future[Map[String, List[Speech]]] = http.singleRequest(HttpRequest(uri = uri)).map { response =>
        {
          response match {
            case HttpResponse(StatusCodes.OK, headers, entity, _) =>
              entity.dataBytes.via(CsvParsing.lineScanner()).map(_.map(_.utf8String.trim)).runWith(Sink.seq).map { v =>
                v.tail.collect {
                  case List(speaker, theme, date, words) => Speech(speaker, theme, LocalDate.parse(date), words.toInt)
                }.toList.groupBy(_.speaker)
              }
            case response @ HttpResponse(code, _, _, _) => Future.successful(Map[String, List[Speech]]())
          }
        }
      }.flatten
      future.pipeTo(sender())
  }
}