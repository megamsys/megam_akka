
resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("Typesafe Snapshots", url("http://repo.typesafe.com/typesafe/snapshots/"))

resolvers += Resolver.url("Akka Snapshots", url("http://repo.akka.io/snapshots/"))

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2-M3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.5.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-s3" % "0.5")