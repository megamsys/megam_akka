import org.scalastyle.sbt.ScalastylePlugin
import sbt._

ScalastylePlugin.Settings


scalaVersion := "2.10.1"

resolvers += "akka" at "http://repo.akka.io/snapshots"

resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

