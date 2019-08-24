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

import com.kazufukurou.nanji.hour12
import com.kazufukurou.nanji.hourOfDay
import com.kazufukurou.nanji.minute
import java.util.Calendar
import java.util.Locale

class TimeRu : Time {
  private val grammarSystem = TimeSystem(Locale("ru"))
  private val femaleNumberConverter = EnRuNumberToTextConverter { getWord(it, true) }
  private val maleNumberConverter = EnRuNumberToTextConverter { getWord(it, false) }

  override fun getPercentText(value: Int, digits: Boolean): String = grammarSystem.getPercentText(value, digits)

  override fun getDateText(cal: Calendar, digits: Boolean, era: Boolean): String {
    return grammarSystem.getDateText(cal, digits, era)
  }

  override fun getTimeText(cal: Calendar, digits: Boolean, twentyFour: Boolean, multiLine: Boolean): String {
    val hourValue = if (twentyFour) cal.hourOfDay else cal.hour12
    val minuteValue = cal.minute
    val hour = convert(hourValue, false, digits) + " " + getPlural(hourValue, "час", "часа", "часов")
    val minute = convert(minuteValue, true, digits) + " " + getPlural(minuteValue, "минута", "минуты", "минут")
    return hour + (if (multiLine) "\n" else " ") + minute
  }

  private fun getWord(digit: Int, female: Boolean) = when (digit) {
    0 -> "ноль"
    1 -> if (female) "одна" else "один"
    2 -> if (female) "две" else "два"
    3 -> "три"
    4 -> "четыре"
    5 -> "пять"
    6 -> "шесть"
    7 -> "семь"
    8 -> "восемь"
    9 -> "девять"
    10 -> "десять"
    11 -> "одиннадцать"
    12 -> "двенадцать"
    13 -> "тринадцать"
    14 -> "четырнадцать"
    15 -> "пятнадцать"
    16 -> "шестнадцать"
    17 -> "семнадцать"
    18 -> "восемнадцать"
    19 -> "девятнадцать"
    20 -> "двадцать"
    30 -> "тридцать"
    40 -> "сорок"
    50 -> "пятьдесят"
    else -> ""
  }

  private fun getPlural(num: Int, formOne: String, formTwo: String, formFive: String): String {
    val n10 = num % 10
    val n100 = num % 100
    return when {
      n10 == 1 && n100 != 11 -> formOne
      n10 in 2..4 && (n100 < 10 || n100 >= 20) -> formTwo
      else -> formFive
    }
  }

  private fun convert(num: Int, female: Boolean, digits: Boolean): String {
    return when {
      digits -> num.toString()
      female -> femaleNumberConverter.convert(num)
      else -> maleNumberConverter.convert(num)
    }
  }
}