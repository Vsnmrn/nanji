/*
 * Copyright 2019 Artyom Mironov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kazufukurou.nanji.model

class EnRuNumberToTextConverter(private val wordSource: (Int) -> String) {

  fun convert(num: Int): String = StringBuilder().apply {
    when (num) {
      in 0..20, 30, 40, 50, 60, 70, 80, 90, 100 -> append(wordSource(num))
      in 21..99 -> append(wordSource(num - num % 10)).append(" ").append(wordSource(num % 10))
    }
  }.toString()
}