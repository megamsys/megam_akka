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
  val Version = "0.4.0"
  val ScalaVersion = "2.10.4"
  val Description = "Cloud bridge to cloud manage megam platform."

  lazy val megamAkka = Project(
    id = "megam_akka",
    base = file("."),
    settings = Defaults.defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      libraryDependencies ++= Dependencies.megamAkkaKernel,
      resolvers := HerkResolvers.All,
      distJvmOptions in Dist := "-Xms256M -Xmx512M",
      additionalLibs in Dist := Seq(new java.io.File("lib/libsigar-amd64-linux-1.6.4.so")),
      outputDirectory in Dist := file("target/megam_herk")))

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version := Version,
    scalaVersion := ScalaVersion,
    crossPaths := false,
    description := Description,
    organizationName := "Megam Systems.",
    organizationHomepage := Some(url("https://www.megam.co")))

  lazy val defaultSettings = buildSettings ++ Seq(
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Xlint",
      "-Xverify",
      "-Yinline-warnings",
      "-Yclosure-elim",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Ydead-code"),
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"))
}

object HerkResolvers {
  val akkasp = "Akka Snapshots Repo" at "http://repo.akka.io/snapshots"
  val typesp = "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"
  val sonasp = "Sonatype Snapshots" at Opts.resolver.sonatypeSnapshots.root
  val sonarl = "Sonatype Releases" at Opts.resolver.sonatypeStaging.root
  val scatsp = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"
  val All = Seq(akkasp, typesp, sonarl, sonasp, scatsp)
}

object Dependencies {
  import Dependency._

  val megamAkkaKernel = Seq(
    akkaKernel, akkaSlf4j, akkaActor, akkaRemote, akkaCluster, sigar, snowflake, mg, mc)
}

object Dependency {
  // Versions
  object V {
    val Akka = "2.3.2"
    val Mg = "0.4.0"
    val Mg_SNST = "0.4.0-SNAPSHOT"

  }
  val snowflake = "com.twitter.service" % "snowflake" % "1.0.2" from "https://s3-ap-southeast-1.amazonaws.com/megampub/0.1/jars/snowflake.jar"
  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.Akka
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.Akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % V.Akka
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % V.Akka
  val sigar = "org.fusesource" % "sigar" % "1.6.4"
  val mg = "com.github.indykish" % "megam_common_2.10" % V.Mg
  val mc = "com.github.indykish" % "megam_chef" % V.Mg_SNST
}
