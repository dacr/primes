import AssemblyKeys._

seq(assemblySettings: _*)

name := "primes"

version := "v2013-09-29"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation" )

mainClass in assembly := Some("primes.Main")

jarName in assembly := "primes.jar"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.+" % "test"

libraryDependencies += "junit" % "junit" % "4.+" % "test"

initialCommands in console := """import primes._"""

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
