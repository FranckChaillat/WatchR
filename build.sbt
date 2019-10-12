
lazy val root = (project in file("."))
    .settings(
      name := "watcher",
      version := "1.0",
      scalaVersion := "2.12.8",
      mainClass in Compile := Some("services.Main")
    )

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "2.47.1",
  "joda-time" % "joda-time" % "2.10.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
  "com.typesafe" % "config" % "1.3.4",
  "org.scalaz" %% "scalaz-core" % "7.2.28",
  "org.json4s" %% "json4s-native" % "3.6.7",
  "org.json4s" %% "json4s-jackson" % "3.6.7",
  "org.mockito" % "mockito-all" % "1.9.5" % Test
)
