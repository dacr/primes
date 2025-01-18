name         := "primes"
organization := "fr.janalyse"
description  := "Library to play with primes using scala language. Draw Ulam spiral, ..."

licenses += "NON-AI-APACHE2" -> url(s"https://github.com/non-ai-licenses/non-ai-licenses/blob/main/NON-AI-APACHE2")

scalaVersion := "3.3.4"
crossScalaVersions := Seq("2.13.16", "3.3.4")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

Test / testOptions += {
  val rel = scalaVersion.value.split("[.]").take(2).mkString(".")
  Tests.Argument(
    "-oDF", // -oW to remove colors
    "-u",
    s"target/junitresults/scala-$rel/"
  )
}

console / initialCommands := """
                               |import fr.janalyse.primes._
                               |val pgen=new PrimesGenerator[Long]
                               |""".stripMargin

homepage   := Some(url("https://github.com/dacr/primes"))
scmInfo    := Some(ScmInfo(url(s"https://github.com/dacr/primes"), s"git@github.com:dacr/primes.git"))
developers := List(
  Developer(
    id = "dacr",
    name = "David Crosson",
    email = "crosson.david@gmail.com",
    url = url("https://github.com/dacr")
  )
)
