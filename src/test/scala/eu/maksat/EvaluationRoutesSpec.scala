package eu.maksat

import akka.actor.{ ActorRef, Props }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class EvaluationRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with EvaluationRoutes {
  lazy val routes = evaluationRoutes
  override val evaluationActor: ActorRef = system.actorOf(Props[EvaluationActor], "EvaluationActor")

  "EvaluationRoutes" should {
    "return no evaluation if no present (GET /evaluation)" in {
      val request = HttpRequest(uri = "/evaluation")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{*}""")
      }
    }
  }
}