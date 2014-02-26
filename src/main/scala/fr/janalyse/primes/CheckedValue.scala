package fr.janalyse.primes

case class CheckedValue[NUM](
  value: NUM,
  isPrime: Boolean,
  digitCount: Long,
  nth: NUM)(implicit numops: Integral[NUM])

object CheckedValue {
  def first[NUM](implicit numops: Integral[NUM]): CheckedValue[NUM] = {
    import numops._
    CheckedValue(one + one, true, 1, one)
  }
}
