package eu.maksat

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object EvaluationServer extends App with EvaluationRoutes {

  implicit val system: ActorSystem = ActorSystem("EvaluationServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = evaluationRoutes

  val evaluationActor: ActorRef = system.actorOf(RoundRobinPool(10).props(Props[EvaluationActor]), "EvaluationActor")

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  Await.result(system.whenTerminated, Duration.Inf)
}
