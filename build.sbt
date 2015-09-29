import Tools._

name := "primes"

version := penvOrElse("PRIMES_REV", "1.2.2-SNAPSHOT")

organization :="fr.janalyse"

organizationHomepage := Some(new URL("http://www.janalyse.fr"))

scalaVersion := "2.11.7"

// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.5", "2.11.7")

mainClass in assembly := Some("fr.janalyse.primes.Main")

jarName in assembly := "primes.jar"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.+" % "test"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.13"
   ,"com.typesafe.akka" %% "akka-stream-experimental" % "1.0"
   ,"org.slf4j"          % "slf4j-api"          % "1.7.+"
)

libraryDependencies += "junit" % "junit" % "4.+" % "test"


initialCommands in console := """
  |import fr.janalyse.primes._
  |val pgen=new PrimesGenerator[Long]
  |""".stripMargin


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

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
