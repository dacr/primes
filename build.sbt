import AssemblyKeys._

seq(assemblySettings: _*)

name := "primes"

version := "1.0.2"

organization :="fr.janalyse"

organizationHomepage := Some(new URL("http://www.janalyse.fr"))

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation" )

mainClass in assembly := Some("fr.janalyse.primes.Main")

jarName in assembly := "primes.jar"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

libraryDependencies += "junit" % "junit" % "4.+" % "test"

initialCommands in console := """
import fr.janalyse.primes._
"""

sourceGenerators in Compile <+= 
 (sourceManaged in Compile, version, name, jarName in assembly) map {
  (dir, version, projectname, jarexe) =>
  val file = dir / "primes" / "MetaInfo.scala"
  IO.write(file,
  """package primes
    |object MetaInfo { 
    |  val version="%s"
    |  val project="%s"
    |  val jarbasename="%s"
    |}
    |""".stripMargin.format(version, projectname, jarexe.split("[.]").head) )
  Seq(file)
}

publishTo := Some(
     Resolver.sftp(
         "JAnalyse Repository",
         "www.janalyse.fr",
         "/home/tomcat/webapps-janalyse/repository"
     ) as("tomcat", new File(util.Properties.userHome+"/.ssh/id_rsa"))
)
