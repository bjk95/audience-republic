// build.sbt

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "audience-republic",
    // Set the main class for the assembly JAR
    assembly / mainClass := Some("AudienceRepublicCodeChallenge")
  )

// Library dependency for testing with ScalaTest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
