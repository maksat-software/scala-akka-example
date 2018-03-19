package eu.maksat

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

trait EvaluationRoutes extends JsonSupport {
  implicit def system: ActorSystem

  implicit lazy val timeout = Timeout(30.seconds)
  lazy val log = Logging(system, classOf[EvaluationRoutes])
  lazy val evaluationRoutes: Route =
    pathPrefix("evaluation") {
      concat(
        pathEnd {
          concat(
            get {
              parameters('url.*) { urls =>
                urls.toList match {
                  case Nil => complete("The url query does not include a url parameter. Please verify you are passing at least one url parameter.")
                  case url :: Nil => completeOrRecoverWith((evaluationActor ? Evaluate(url :: Nil)).mapTo[EvaluationResult])(failWith(_))
                  case urls => completeOrRecoverWith((evaluationActor ? Evaluate(urls)).mapTo[EvaluationResult])(failWith(_))
                }
              }
            }
          )
        }
      )
    }

  def evaluationActor: ActorRef
}
