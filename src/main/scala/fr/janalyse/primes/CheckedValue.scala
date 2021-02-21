/*
 * Copyright 2013-2021 David Crosson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
