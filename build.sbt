name         := "primes"
organization := "fr.janalyse"
homepage     := Some(new URL("https://github.com/dacr/primes"))
scmInfo      := Some(ScmInfo(url(s"https://github.com/dacr/primes"), s"git@github.com:dacr/primes.git"))

licenses += "NON-AI-APACHE2" -> url(s"https://github.com/non-ai-licenses/non-ai-licenses/blob/main/NON-AI-APACHE2")

scalaVersion := "3.3.1"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.13.12", "3.3.1")
// 2.10.x : generates java 6 bytecodes
// 2.11.x : generates java 6 bytecodes
// 2.12.x : generates java 8 bytecodes && JVM8 required for compilation
// 2.13.x : generates java 8 bytecodes && JVM8 required for compilation

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test"

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
