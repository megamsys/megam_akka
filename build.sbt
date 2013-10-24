import sbt._
import com.typesafe.sbt.packager.Keys._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import S3._

scalaVersion := "2.10.3"

com.typesafe.sbt.packager.debian.Keys.name in Debian := "megamakka"

maintainer in Debian:= "Rajthilak <rajthilak@megam.co.in>"

packageSummary := "Cloud Bridge for Megam."

packageDescription in Debian:= "Cloud bridge to cloud manage megam platform. "

// packagerSettings

// s3Settings

// linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
//  (packageMapping((bd / "bin/start") -> "/usr/local/share/megamakka/bin/start")
//   withUser "root" withGroup "root" withPerms "0755")
// }

// linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
//  val src = bd / "target/megam_akka/lib"
//  val dest = "/usr/local/share/megamakka/lib"
//  LinuxPackageMapping(
//    for {
//      path <- (src ***).get
//      if !path.isDirectory
//    } yield path -> path.toString.replaceFirst(src.toString, dest)
//  )
// }

// linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
//  val src = bd / "target/megam_akka/deploy"
//  val dest = "/usr/local/share/megamakka/deploy"
//  LinuxPackageMapping(
//    for {
//      path <- (src ***).get
//      if !path.isDirectory
//    } yield path -> path.toString.replaceFirst(src.toString, dest)
// )
// }

// linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
//  (packageMapping((bd / "target/megam_akka/config/application.conf") -> "/usr/local/share/megamakka/config/application.conf")
//   withConfig())
// }



// com.typesafe.sbt.packager.debian.Keys.version in Debian <<= (com.typesafe.sbt.packager.debian.Keys.version, sbtVersion) apply { (v, sv) =>
//  sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
// }

// linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
//  packageMapping(
//    (bd / "copyright") -> "/usr/share/doc/megam_akka/copyright"
//  ) withPerms "0644" asDocs()
// }

// linuxPackageMappings in Debian <+= (com.typesafe.sbt.packager.debian.Keys.sourceDirectory) map { bd =>
//  (packageMapping(
//    (bd / "CHANGELOG") -> "/usr/share/doc/megam_akka/changelog.gz"
//  ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
//}

//mappings in upload := Seq((new java.io.File(("%s-%s.deb") format("target/megamakka", "0.12.4-build-0100")),"debs/megam_akka.deb"))

//host in upload := "megampub.s3.amazonaws.com"

//mappings in download := Seq((new java.io.File(("%s-%s.deb") format("target/megamakka", "0.12.4-build-0100")),"debs/megam_akka.deb"))

//host in download := "megampub.s3.amazonaws.com"

//mappings in delete := Seq("debs/megam_akka.deb")

//host in delete := "megampub.s3.amazonaws.com"

//credentials += Credentials(Path.userHome / "software" / "aws" / "keys" / "sbt_s3_keys")

//S3.progress in S3.upload := true
