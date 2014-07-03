import sbt._
import Process._
import com.typesafe.sbt.packager.debian.Keys._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping

seq(packagerSettings:_*)

scalaVersion := "2.10.4"

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

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamd"

com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbt.Keys.version) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s" format (sv)
    }


maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "The engine for Megam."

packageDescription in Debian:= "Cloud engine to automate on Megam platform. "

debianPackageDependencies in Debian ++= Seq("curl", "openjdk-7-jre-headless",  "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "rabbitmq-server"


 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 (packageMapping((bd / "bin/herk_stash") -> "/var/lib/megam/megamd/herk_stash")
   withUser "megam" withGroup "megam" withPerms "0755")
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 (packageMapping((bd / "bin/megamherk.cron.d") -> "/etc/cron.d/megamherk_riakstash")
   withUser "megam" withGroup "megam" withPerms "0644")
 }

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
(packageMapping((bd / "bin/env.sh") -> "/var/lib/megam/megamd/env.sh")
	withUser "megam" withGroup "megam" withPerms "0644")
}


 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 (packageMapping((bd / "bin/start") -> "/usr/share/megam/megamd/bin/start")
   withUser "megam" withGroup "megam" withPerms "0755")
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 packageMapping(
    (bd / "logs") -> "/var/lib/megam/megamd/logs"
  ) withPerms "0755"
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_herk/lib"
  val dest = "/usr/share/megam/megamd/lib"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  )
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  val src = bd / "target/megam_herk/deploy"
  val dest = "/usr/share/megam/megamd/deploy"
  LinuxPackageMapping(
    for {
      path <- (src ***).get
      if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
 )
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping((bd / "target/megam_herk/config/application.conf") -> "/usr/share/megam/megamd/config/application.conf")
   withConfig())
 }

 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
 packageMapping(
    (bd / "copyright") -> "/usr/share/megam/megamd/copyright"
  ) withPerms "0644" asDocs()
 }


 linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "CHANGELOG") -> "/usr/share/megam/megamd/changelog.gz"
  ) withUser "megam" withGroup "megam" withPerms "0644" gzipped) asDocs()
}

linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
  (packageMapping(
    (bd / "templates/etc/init/megamd") -> "/etc/init/megamd")
		withUser "megam" withGroup "megam" withPerms "0755")
}
