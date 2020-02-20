/*
 * Copyright 2019 Oliver Rhyme G. Añasco
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.snorlax.snorlax.utils

import java.util.*

object TimeUtils {

    fun numOfDays(month: Date): Int {
        return GregorianCalendar(TimeZone.getTimeZone("UTC"))
            .apply { time = month }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }



    fun getTodayDateUTC(): Date {
        val rawCalendar = GregorianCalendar.getInstance()
        val safe = getCalendar()
        safe.set(
            rawCalendar.get(Calendar.YEAR),
            rawCalendar.get(Calendar.MONTH),
            rawCalendar.get(Calendar.DAY_OF_MONTH)
        )
        return safe.time
    }

//    fun getLocalTime(utcTime: Date) : Date {
//
//    }

    fun getMonthDate(date: Date): Date {
        val raw = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        raw.time = date
        val safe = getCalendar()
        safe.run {
            set(Calendar.YEAR, raw.get(Calendar.YEAR))
            set(Calendar.MONTH, raw.get(Calendar.MONTH))
        }
        return safe.time

    }
    fun getTodayDateLocal(): Date {
//            val calendar = Calendar.getInstance(TimeZone.getDefault())
//
//            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY))
//            calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE))
//            calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND))
//            calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND))

        val raw = GregorianCalendar.getInstance()
        raw.time = getTodayDateUTC()
        return raw.time
    }

    private fun getCalendar(): Calendar {
        val utc = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        return utc
    }

//        fun getTommorowDate(date: Date): Date {
//            val today = Calendar.getInstance().apply {
//                this.time = date
//            }
//            today.set(Calendar.SECOND, 86400)
//            return today.time
//        }
//
//        fun Date.getDayTimeStamp(): Date {
//            this.time = (this.time / 100_00) * 100_000
//            return this
//        }

    fun getMaxMonthDate(month: Date): Date =
        GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        time = month
        set(Calendar.DAY_OF_MONTH, getMaximum(Calendar.DAY_OF_MONTH))
    }.time

    fun timeToPosition(timeInMillis: Long): Int {
        return (timeInMillis / 86_400_000).toInt()
    }

    fun positionToTime(position: Int): Date {
        return Date(86_400_000 * position.toLong())
    }
}

