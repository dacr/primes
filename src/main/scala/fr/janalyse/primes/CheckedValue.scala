package fr.janalyse.primes

case class CheckedValue[NUM](
  value: NUM,
  isPrime: Boolean,
  digitCount: Long,
  nth: NUM)(implicit numops: Integral[NUM])

object CheckedValue {
  def first[NUM](implicit numops: Integral[NUM]): CheckedValue[NUM] = {
    import numops._
    CheckedValue(value = one + one, isPrime = true, digitCount = 1, nth = one)
  }
  def apply[NUM](value: NUM, isPrime:Boolean, nth:NUM)(implicit numops: Integral[NUM]): CheckedValue[NUM] = {
    CheckedValue(value, isPrime, value.toString.length, nth)
  }
}
