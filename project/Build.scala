import sbt._
import Keys._

import de.johoop.testngplugin.Keys._

object InjectionBusBuild extends Build {

  lazy val slf4jVersion = "1.7.3"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.1"
  )

  lazy val InjectionBusSettings = CommonSettings ++ Seq(
    scalaVersion := "2.10.1",
    crossPaths := true,
    libraryDependencies ++= Seq (
      "io.netty" % "netty-all" % "4.0.0.CR6",
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion % "test",
      "com.google.guava" % "guava" %  "14.0.1" % "test"
      )
  )

  lazy val CommonSettings = Defaults.defaultSettings ++ Seq(
    organization := "eu.pmsoft.sam",
    crossPaths := false,
    version := "0.4.1-SNAPSHOT",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8"),
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:deprecation"),
    libraryDependencies ++= CommonDependencies
  )

  lazy val TestNGExecution =  de.johoop.testngplugin.TestNGPlugin.testNGSettings ++ Seq(
    testNGSuites <<= (resourceDirectory in Test)(path => Seq((path / "testng.xml").absolutePath)),
    testNGVersion := "6.8.5"
  )


  lazy val CommonDependencies = Seq(
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
    "org.mockito" %  "mockito-all" % "1.9.5" % "test",
    "org.testng" % "testng" % "6.8.5" % "test"
  )


  lazy val root = Project(
    id = "root",
    base = file("."),
    settings =  CommonSettings
  ) aggregate(
    core,
    model,
    infrastructure
   )

  lazy val core = Project(
    id = "coreapi",
    base = file("coreapi"),
    settings =  CommonSettings ++ TestNGExecution
  )

  lazy val model = Project(
    id = "model",
    base = file("model"),
    settings =  CommonSettings
  ) dependsOn(
    core
    )

  lazy val infrastructure = Project(
    id = "infrastructure",
    base = file("infrastructure"),
    settings =  InjectionBusSettings ++ TestNGExecution
  ) dependsOn(
    core,model
    )

}
