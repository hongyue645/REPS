// build.sbt
name := "REPS"

version := "0.1.0"

scalaVersion := "3.3.5"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.9.0",
  "com.lihaoyi" %% "ujson" % "4.1.0",
  "com.lihaoyi" %% "upickle" % "4.1.0",
  "com.lihaoyi" %% "os-lib" % "0.11.4",
  "com.github.tototoshi" %% "scala-csv" % "2.0.0"
)
