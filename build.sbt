import sbt._
import Process._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import S3._

seq(packagerSettings:_*)

s3Settings

scalaVersion := "2.10.3"

scalacOptions := Seq(
	"-target:jvm-1.7",
	"-deprecation",
	"-feature",
 	"-optimise",
  	"-Xcheckinit",
  	"-Xlint",
  	"-Xverify",
 // 	"-Yconst-opt",  	available in scala 2.11
  	"-Yinline",
  	"-Ywarn-all",
  	"-Yclosure-elim",
  	"-language:postfixOps",
  	"-language:implicitConversions",
  	"-Ydead-code")

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamherk"

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }
 
 
maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "Cloud Bridge for Megam."

packageDescription in Debian:= "Cloud bridge to manage megam platform. "

debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)")
 
debianPackageRecommends in Debian += "rabbitmq-server"

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 (packageMapping((bd / "bin/start") -> "/usr/share/megamherk/bin/start")
   withUser "root" withGroup "root" withPerms "0755")
 }
 
 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 packageMapping(
    (bd / "logs") -> "/usr/share/megamherk/logs"
  ) withPerms "0755"
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_herk/lib"
  val dest = "/usr/share/megamherk/lib"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  )
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_herk/deploy"
  val dest = "/usr/share/megamherk/deploy"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
 )
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "target/megam_herk/config/application.conf") -> "/usr/share/megamherk/config/application.conf")
   withConfig())
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 packageMapping(
    (bd / "copyright") -> "/usr/share/megamherk/copyright"
  ) withPerms "0644" asDocs()
 }
  

 linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "CHANGELOG") -> "/usr/share/megamherk/changelog.gz"
  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
}

mappings in upload := Seq((new java.io.File(("%s-%s.deb") format("target/megamherk", "0.1.0")),"0.1/debs/megamherk.deb"))

host in upload := "megampub.s3.amazonaws.com"

mappings in download := Seq((new java.io.File(("%s-%s.deb") format("target/megamherk", "0.1.0")),"0.1/debs/megamherk.deb"))

host in download := "megampub.s3.amazonaws.com"

host in delete := "megampub.s3.amazonaws.com"

credentials += Credentials(Path.userHome / "software" / "aws" / "keys" / "sbt_s3_keys")

S3.progress in S3.upload := true
