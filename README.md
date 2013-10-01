primes
======

Playing with primes using scala language. Draw Ulam spiral, ...

To generate a 1000x1000 ulam spiral use : 

$ sbt "run 1000"

It will generate a PNG image file named "ulam-spiral-500.png"


To starts the console and play with primes :

$ sbt console"

scala> sexyPrimeStream(4)
res3: primes.Primes.PInteger = 17

scala> primeStream.take(15).toList
res3: List[primes.Primes.PInteger] = List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)

scala> sexyPrimeStream.take(5).toList
res5: List[primes.Primes.PInteger] = List(5, 7, 11, 13, 17)

scala> isolatedPrimeStream.take(10).toList
res2: List[primes.Primes.PInteger] = List(23, 37, 47, 53, 67, 79, 83, 89, 97, 113)


and so one...
