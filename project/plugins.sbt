
resolvers += Resolver.url("Typesafe Snapshots", url("http://repo.typesafe.com/typesafe/snapshots/"))

resolvers += Resolver.url("Akka Snapshots", url("http://repo.akka.io/snapshots/"))

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-s3" % "0.5")