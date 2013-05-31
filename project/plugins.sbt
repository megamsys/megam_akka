
resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("Typesafe Snapshots", url("http://repo.typesafe.com/typesafe/snapshots/"))

resolvers += Resolver.url("Akka Snapshots", url("http://repo.akka.io/snapshots/"))

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2-M3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.2.0")

