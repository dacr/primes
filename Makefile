all: unit-tests

dependency-check:
	sbt dependencyUpdates

unit-tests:
	sbt +test

format:
	sbt scalafmtAll scalafmtSbt
