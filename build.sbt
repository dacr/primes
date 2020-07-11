name := "primes"
organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/primes"))
licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")
scmInfo := Some(ScmInfo(url(s"https://github.com/dacr/primes"), s"git@github.com:dacr/primes.git"))

scalaVersion := "2.13.3"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.13.3")

libraryDependencies += "org.scalatest" %% "scalatest"   % "3.2.0" % "test"

testOptions in Test += {
  val rel = scalaVersion.value.split("[.]").take(2).mkString(".")
  Tests.Argument(
    "-oDF", // -oW to remove colors
    "-u", s"target/junitresults/scala-$rel/"
  )
}

initialCommands in console := """
  |import fr.janalyse.primes._
  |val pgen=new PrimesGenerator[Long]
  |""".stripMargin

