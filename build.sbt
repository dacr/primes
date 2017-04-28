import Tools._

name := "primes"

version := penvOrElse("PRIMES_REV", "1.2.2-SNAPSHOT")

organization :="fr.janalyse"
organizationHomepage := Some(new URL("http://www.janalyse.fr"))

scalaVersion := "2.12.2"
crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.2")


// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")


mainClass in assembly := Some("fr.janalyse.primes.Main")
assemblyJarName := "primes.jar"


libraryDependencies ++= Seq(
//   "com.typesafe.akka" %% "akka-actor"               % "2.3.13"
//  ,"com.typesafe.akka" %% "akka-stream-experimental" % "1.0"
  "org.slf4j"          % "slf4j-api"                % "1.7.25"
  ,"org.scalatest"     %% "scalatest"                % "3.0.3" % "test"
)

initialCommands in console := """
  |import fr.janalyse.primes._
  |val pgen=new PrimesGenerator[Long]
  |""".stripMargin


sourceGenerators in Compile += Def.task {
  val dir = (sourceManaged in Compile).value
  val file = dir / "primes" / "MetaInfo.scala"
  val projectVersion = version.value
  val projectName = name.value
  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val buildDate = sdf.format(new java.util.Date())
  val jarExe = assemblyJarName.value
  val jarBasename = jarExe.split("[.]").head
  IO.write(file,
    s"""package primes
       |object MetaInfo { 
       |  val version="$projectVersion"
       |  val buildDate="$buildDate"
       |  val projectName="$projectName"
       |  val jarbasename="%$jarBasename"
       |}
       |""".stripMargin)
  Seq(file)
}.taskValue
