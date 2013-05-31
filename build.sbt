import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import MegCommonReleaseSteps._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import sbt._

name := "megam_akka"

organization := "org.megam"

scalaVersion := "2.10.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "UTF-8")

javacOptions  := Seq("-Xlint:unchecked", "-Xlint:deprecation")
   
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots"

resolvers  +=  "Sonatype OSS Snapshots"  at  "https://oss.sonatype.org/content/repositories/snapshots"

resolvers  += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/public"
      
resolvers += "Twitter Repo" at "http://maven.twttr.com"   
      
      

libraryDependencies ++= {
  val scalaCheckVersion = "1.10.1"
  val specs2Version = "1.14"
  val scalazVersion = "7.0.0"
  val zkVersion = "6.3.2"
  val liftJsonVersion = "2.5-RC5"
  val Akka = "2.2-SNAPSHOT"
  val Zk = "6.3.2"
  val Mg = "0.1.0-SNAPSHOT"  
  Seq(
  "com.typesafe.akka" %% "akka-kernel" % Akka,
  "com.typesafe.akka" %% "akka-slf4j" % Akka,
  "com.typesafe.akka" %% "akka-actor" % Akka,
  "com.typesafe.akka" %% "akka-remote" % Akka,
   "com.typesafe.akka" %% "akka-cluster-experimental" % Akka,
   "ch.qos.logback" % "logback-classic" % "1.0.11",
   "org.fusesource" % "sigar" % "1.6.4",
   "com.twitter" % "util-zk-common" % zkVersion,
   "com.github.indykish" % "megam_common_2.10" % Mg,  
   "org.scalaz" %% "scalaz-core" % scalazVersion,
   "org.scalaz" %% "scalaz-effect" % scalazVersion,
   "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
   "net.liftweb" %% "lift-json-scalaz7" % liftJsonVersion,
   "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
   "org.specs2" %% "specs2" % specs2Version % "test",   
   "org.pegdown" % "pegdown" % "1.0.2" % "test" 
  )
}

logBuffered := false

ScalastylePlugin.Settings

Plugin.graphSettings

releaseSettings

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  setReadmeReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

publishTo <<= (version) { version: String =>
  val nexus = "https://oss.sonatype.org/"
  if (version.trim.endsWith("SNAPSHOT")) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
   } else {
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}

publishMavenStyle := true

publishArtifact in Test := true

testOptions in Test += Tests.Argument("html", "console")

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/indykish/megam_akka</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:indykish/megam_akka.git</url>
    <connection>scm:git:git@github.com:indykish/megam_akka.git</connection>
  </scm>
  <developers>
    <developer>
      <id>indykish</id>
      <name>Kishorekumar Neelamegam</name>
      <url>http://www.megam.co</url>
    </developer>
    <developer>
      <id>rajthilakmca</id>
      <name>Raj Thilak</name>
      <url>http://www.megam.co</url>
    </developer>    
  </developers>
)
