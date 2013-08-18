/**
 * Copyright 2012-2013 Megam Systems
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

import io.Source
import java.io.PrintWriter
import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, additionalLibs }

object MegamAkkaKernel extends Build {

   val Organization = "org.megam"
  val Version      = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.2"

lazy val megamAkka = Project(
    id = "megam_akka",
    base = file("."),
    settings = Defaults.defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      libraryDependencies ++= Dependencies.megamAkkaKernel,
      distJvmOptions in Dist := "-Xms256M -Xmx1024M",
      additionalLibs in Dist := Seq(new java.io.File("lib/libsigar-amd64-linux-1.6.4.so")),
      outputDirectory in Dist := file("target/megam_akka")))   


  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version := Version,
    scalaVersion := ScalaVersion,   
    crossPaths := false,
    organizationName := "Megam Systems.",
    organizationHomepage := Some(url("http://www.megam.co")))



  lazy val defaultSettings = buildSettings ++ Seq(
      // compile options
      scalacOptions ++= Seq ("-encoding", "UTF-8", "-deprecation", "-unchecked"),
      javacOptions ++= Seq ("-Xlint:unchecked", "-Xlint:deprecation"))       
  

}

object Dependencies {
  import Dependency._

  val megamAkkaKernel = Seq(
    akkaKernel, akkaSlf4j, akkaActor, akkaRemote, akkaCluster, sigar, zk_common, mg, mc, scalaz, scalaz_effect,
    scalaz_concurrent, lift_json, scalacheck, util_log, util_core, zk)
}

object Dependency {
  // Versions
  object V {
    val Akka = "2.2.0"
    val scalaCheckVersion = "1.10.1"
    val scalazVersion = "7.0.3"
    val liftJsonVersion = "2.5"
    val Zk = "6.3.8"
    val Mg = "0.1.0-SNAPSHOT"
  }

  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.Akka
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.Akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % V.Akka
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % V.Akka 
  val sigar = "org.fusesource" % "sigar" % "1.6.4"
  val zk_common = "com.twitter" % "util-zk-common_2.10" % V.Zk
  val mg = "com.github.indykish" % "megam_common_2.10" % V.Mg
  val mc = "com.github.indykish" % "megam_chef" % V.Mg
  val scalaz = "org.scalaz" %% "scalaz-core" % V.scalazVersion
  val scalaz_effect = "org.scalaz" %% "scalaz-effect" % V.scalazVersion
  val scalaz_concurrent = "org.scalaz" %% "scalaz-concurrent" % V.scalazVersion
  val lift_json = "net.liftweb" %% "lift-json-scalaz7" % V.liftJsonVersion
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalaCheckVersion % "test"
  val util_log = "com.twitter" % "util-logging_2.10" %  V.Zk
  val util_core = "com.twitter" % "util-core_2.10" %  V.Zk
  val zk = "com.twitter" % "util-zk_2.10" % V.Zk
}
