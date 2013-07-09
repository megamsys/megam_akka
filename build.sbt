import sbt._
import Process._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import org.scalastyle.sbt.ScalastylePlugin
import S3._

seq(packagerSettings:_*)

maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "Cloud instrumentation agents."

packageDescription in Debian:= "Cloud instrumentation allows the lifecycle of cloud infrastructure provisioning to be managed, ease repeatable deployments, change the behaviour of instances by injecting behaviour. "

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megam_akka"

ScalastylePlugin.Settings

s3Settings

scalaVersion := "2.10.2"

resolvers += "akka" at "http://repo.akka.io/snapshots"

resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "target/megam_akka/bin/start") -> "/usr/local/share/megamakka/bin/start")
   withUser "root" withGroup "root" withPerms "0755")
}


linuxPackageMappings <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_akka/lib"
  val dest = "/usr/local/share/megamakka/lib"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  )
}

linuxPackageMappings <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_akka/deploy"
  val dest = "/usr/local/share/megamakka/deploy"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  )
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "target/megam_akka/config/application.conf") -> "/usr/local/share/megamakka/config/application.conf")
   withConfig())
}


 
com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbtVersion) apply { (v, sv) =>
  sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
}

debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "rabbitmq-server"

linuxPackageMappings <+= (baseDirectory) map { bd =>
  packageMapping(
    (bd / "copyright") -> "/usr/share/doc/megam_akka/copyright"
  ) withPerms "0644" asDocs()
}

linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "CHANGELOG") -> "/usr/share/doc/megam_akka/changelog.gz"
  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
}


mappings in upload := Seq((new java.io.File(("%s-%s.deb") format("target/megamakka", "0.12.4-build-0100")),"debs/megam_akka0.1.0.deb"))

host in upload := "megampub.s3.amazonaws.com"

mappings in download := Seq((new java.io.File(("%s-%s.deb") format("target/megamakka", "0.12.4-build-0100")),"debs/megam_akka0.1.0.deb"))

host in download := "megampub.s3.amazonaws.com"

//mappings in delete := Seq("debs/megam_akka0.1.0.deb")

//host in delete := "megampub.s3.amazonaws.com"

credentials += Credentials(Path.userHome / "software" / "aws" / "keys" / "sbt_s3_keys")
