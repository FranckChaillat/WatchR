
lazy val root = (project in file("."))
    .settings(
      name := "watcher",
      version := "1.0",
      scalaVersion := "2.12.8",
      mainClass in Compile := Some("services.Main")
    )

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.akka" %% "akka-http"   % "10.1.11",
  "com.typesafe.akka" %% "akka-remote" % "2.6.4",
  "com.typesafe.akka" %% "akka-actor" % "2.6.4",
  "org.seleniumhq.selenium" % "selenium-java" % "2.47.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe" % "config" % "1.3.4",
  "org.scalaz" %% "scalaz-core" % "7.2.28",
  "org.json4s" %% "json4s-native" % "3.6.7",
  "org.json4s" %% "json4s-jackson" % "3.6.7",
  "org.mockito" % "mockito-all" % "1.9.5" % Test
)
