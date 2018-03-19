package eu.maksat

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ JsNull, JsObject, JsString, JsValue, RootJsonFormat }

trait JsonSupport extends SprayJsonSupport {

  implicit object evaluationResultJsonFormat extends RootJsonFormat[EvaluationResult] {
    def write(er: EvaluationResult) = JsObject(
      "mostSpeeches" -> option2String(er.mostSpeeches),
      "mostSecurity" -> option2String(er.mostSecurity),
      "leastWordy" -> option2String(er.leastWordy)
    )

    private def option2String(opt: Option[String]) = opt match {
      case Some(value) => JsString(value)
      case _ => JsNull
    }

    override def read(json: JsValue): EvaluationResult = ???
  }

}