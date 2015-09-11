Release 1.2.2-SNAPSHOT
  - akka 2.3.13
  - scala 2.11.7 & 2.10.5


Release 1.2.1
  - scala 2.11.5
  - akka 2.3.9
  - paul philipps "sbt" launcher script added 
  - Sieve of Eratosthenes algorithm added
  - less autoboxing in isPrime & isPrimePara (checkUpTo) => performance enhancements

Release 1.2.0
  - scala 2.11.4
  - akka 2.3.8
  - scalatest 2.2.+
  - akka stream 1.0M2
  - StreamBasedPrimesGenerator algorithm added (refactoring required, not really finished)
    => Akka stream based primes computation with automatic back pressure managed.
    => But in this implementation the output order is not preserved...
  - ActorsPrimesGenerator algorithm
    => Here the order is preserved
/Users/dcr/Dev/primes/RELEASENOTES.md
