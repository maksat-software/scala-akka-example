lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion = "2.5.11"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "eu.maksat",
      scalaVersion := "2.12.4"
    )),
    name := "evaluation",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.8",
      "org.scalaz" %% "scalaz-core" % "7.2.20",
      "com.typesafe" % "config" % "1.3.3",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )