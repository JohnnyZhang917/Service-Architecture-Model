package eu.pmsoft.sam.idgenerator


object Sieve {

  def primeNumber(position: Int): Int = ps(position)

  def isPrime(number : Int ) = ! ps.takeWhile( _ <= number ).filter( _ == number ).isEmpty

  private lazy val ps: Stream[Int] = 2 #:: Stream.from(3).filter(i =>
    ps.takeWhile(j => j * j <= i).forall(i % _ > 0))
}
