name         := "primes"
organization := "fr.janalyse"
homepage     := Some(url("https://github.com/dacr/primes"))
scmInfo      := Some(ScmInfo(url(s"https://github.com/dacr/primes"), s"git@github.com:dacr/primes.git"))

licenses += "NON-AI-APACHE2" -> url(s"https://github.com/non-ai-licenses/non-ai-licenses/blob/main/NON-AI-APACHE2")

scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/dacr/counters.git"),
    s"git@github.com:dacr/counters.git"
  )
)

developers   := List(
  Developer(
    id = "dacr",
    name = "David Crosson",
    email = "crosson.david@gmail.com",
    url = url("https://github.com/dacr")
  )
)
scalaVersion := "3.5.1"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.13.15", "3.5.1")
// 2.10.x : generates java 6 bytecodes
// 2.11.x : generates java 6 bytecodes
// 2.12.x : generates java 8 bytecodes
// 2.13.x : generates java 8 bytecodes
// 3.x    : generates java 8 bytecodes

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
