name := "sangria-finatra-elastic"

version := "1.0"

scalaVersion := "2.11.8"

parallelExecution in ThisBuild := true

mainClass in Compile := Some("SearchServerMain")

lazy val versions = new {
  val finatra = "2.1.2"
  val logback = "1.1.3"
  var json4s = "3.3.0"
  var sangriaVersion = "0.4.3"
  val elastic4s = "1.7.4"
  val elastic4sjackson = "1.7.4"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.twitter.finatra" %% "finatra-http" % versions.finatra,
  "com.twitter.finatra" %% "finatra-httpclient" % versions.finatra,
  "com.twitter.finatra" %% "finatra-slf4j" % versions.finatra,
  "com.twitter.inject" %% "inject-core" % versions.finatra,
  "org.json4s" %% "json4s-native" % versions.json4s,
  "org.sangria-graphql" %% "sangria" % versions.sangriaVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-core" % versions.elastic4s,
  "com.sksamuel.elastic4s" %% "elastic4s-jackson" % versions.elastic4sjackson,
  "ch.qos.logback" % "logback-classic" % "1.0.13")