/*
 * Copyright 2013-2023 David Crosson
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

class NumericIterator[NUM](startFrom: NUM)(implicit numops: Integral[NUM]) extends Iterator[NUM] {
  import numops._
  private var num: NUM = startFrom - one

  override def next(): NUM = {
    num += one
    num
  }

  override def hasNext: Boolean = true
}
