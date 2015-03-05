
/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import Keys._

import de.johoop.testngplugin.Keys._
import scalabuff.ScalaBuffPlugin._

object InjectionBusBuild extends Build {

  lazy val slf4jVersion = "1.7.10"
  lazy val scalaVersionStr = "2.11.5"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := scalaVersionStr
  )

  lazy val InjectionBusSettings = CommonSettings ++ Seq(
    scalaVersion := scalaVersionStr,
    crossPaths := true,
    libraryDependencies ++= Seq(
      "io.netty" % "netty-all" % "4.0.26.Final",
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion % "test",
      "com.google.guava" % "guava" % "18.0" % "test"
    )
  ) ++ TestNGExecution ++ scalabuffSettings

  lazy val CommonSettings = Defaults.defaultSettings ++ Seq(
    organization := "eu.paweld2",
    crossPaths := false,
    version := "0.4.3-SNAPSHOT",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8"),
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6", "-Xlint:deprecation"),
    javacOptions in doc := Seq("-source", "1.6"),
    libraryDependencies ++= CommonDependencies
  ) ++ sonatypeSettings

  val sonatypeSettings = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := {
      _ => false
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
//    useGpg := true,
//    useGpgAgent := true,
    pomExtra := (
      <url>https://github.com/paweld2/Service-Architecture-Model</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:paweld2/Service-Architecture-Model.git</url>
          <developerConnection>scm:git:git@github.com:paweld2/Service-Architecture-Model.git</developerConnection>
          <connection>scm:git:git@github.com:paweld2/Service-Architecture-Model.git</connection>
        </scm>
        <developers>
          <developer>
            <id>paweld2</id>
            <name>Pawel Cesar Sanjuan Szklarz</name>
            <url>http://paweld2.eu</url>
          </developer>
        </developers>
        <parent>
          <groupId>org.sonatype.oss</groupId>
          <artifactId>oss-parent</artifactId>
          <version>7</version>
        </parent>
      )
  )

  lazy val TestNGExecution = de.johoop.testngplugin.TestNGPlugin.testNGSettings ++ Seq(
    testNGSuites <<= (resourceDirectory in Test)(path => Seq((path / "testng.xml").absolutePath)),
    testNGVersion := "6.8.5"
  )


  lazy val CommonDependencies = Seq(
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
    "org.mockito" % "mockito-all" % "2.0.2-beta" % "test",
    "org.testng" % "testng" % "6.8.21" % "test"
  )


  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = CommonSettings ++ Seq(publishArtifact := false)
  ) aggregate(
    core,
    model,
    infrastructure
    )

  lazy val core = Project(
    id = "coreapi",
    base = file("coreapi"),
    settings = CommonSettings ++ TestNGExecution
  )

  lazy val model = Project(
    id = "model",
    base = file("model"),
    settings = CommonSettings
  ) dependsOn (
    core
    )


  lazy val infrastructure = Project(
    id = "infrastructure",
    base = file("infrastructure"),
    settings = InjectionBusSettings
  ).configs(ScalaBuff).dependsOn(
    core, model
  )


}
