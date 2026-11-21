ThisBuild / pomIncludeRepository   := { _ => false }
ThisBuild / publishMavenStyle      := true
ThisBuild / Test / publishArtifact := false
ThisBuild / releaseCrossBuild      := true
ThisBuild / versionScheme          := Some("semver-spec")

// -----------------------------------------------------------------------------
// sbt native Sonatype Central Portal support (sbt >= 1.11), credentials taken
// from SONATYPE_USERNAME / SONATYPE_PASSWORD environment variables (see .envrc)
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

// -----------------------------------------------------------------------------
releaseTagComment        := s"Releasing ${(ThisBuild / version).value}"
releaseCommitMessage     := s"Setting version to ${(ThisBuild / version).value}"
releaseNextCommitMessage := s"[ci skip] Setting version to ${(ThisBuild / version).value}"

// -----------------------------------------------------------------------------
import ReleaseTransformations.*
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"), // cross build: stage every scala version
  releaseStepCommand("sonaRelease"),                // then upload the whole staged bundle
  setNextVersion,
  commitNextVersion,
  pushChanges
)
