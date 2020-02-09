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

package com.snorlax.snorlax.viewmodel

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.files.FileSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.Constants
import com.snorlax.snorlax.utils.TimeUtils.getTodayDateLocal
import com.snorlax.snorlax.utils.processor.WordProcessor
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.util.*

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val cache = LocalCacheSource.getInstance(application)

    private val firestore = FirebaseFirestoreSource.getInstance()

    private val fileSource = FileSource.getInstance(application)

    val selectedTimeObservable = BehaviorSubject.create<Long>()


//    companion object {
//        private var instance: AttendanceViewModel? = null
//        fun getInstance() : AttendanceViewModel {
//            instance?.let { return it }
//            instance = AttendanceViewModel()
//            return getInstance()
//        }
//    }

    // Only allow previous days
//    val bounds =
//        CalendarConstraints.Builder()
//            .setEnd(Calendar.getInstance().timeInMillis)
//            .setValidator(object : CalendarConstraints.DateValidator {
//                var date: Long = 0
//                override fun isValid(date: Long): Boolean {
//                    this.date = date
//                    return date <= Calendar.getInstance().timeInMillis
//                }
//
//                override fun writeToParcel(dest: Parcel?, flags: Int) {
//                    dest?.writeInt(if (date <= Calendar.getInstance().timeInMillis) 1 else 0)
//                }
//                override fun describeContents() = 0
//            })
//            .build()


    //    fun getStudentFromLrn(context: Context, lrn: String) : Single<Student> {
//        return cache.getUserCache(context).flatMap {admin ->
//            firestore.getStudentByLrn(admin.section, lrn)
//        }.toSingle()
//    }
//    fun getStudentDocumentReference(context: Context, lrn: String): DocumentReference =
//        firestore.getDocumentReference(getAdminSection(context), lrn)

    fun deleteFile(location: Uri) {
        if (!DocumentsContract.deleteDocument(
                getApplication<Application>().contentResolver,
                location
            )
        ) deleteFile(location)
    }

    fun isEmpty(document: Uri) = fileSource.isFileEmpty(document)

    fun outputFileName(outputLocation: Uri) = fileSource.getFileName(outputLocation)

    private fun saveToFile(document: XWPFDocument, outputLocation: Uri) =
        Completable.fromAction {
            fileSource.getFileOutputStream(outputLocation).use { stream ->
                document.use {
                    it.write(stream)
                }
            }
        }.subscribeOn(Schedulers.io())


    fun saveAttendance(owner: Activity, outputLocation: Uri, month: Date): Completable {

        val attendance = firestore.getMonthlyAttendance(owner, section, month)
        val student = firestore.getStudentList(owner, section)

        val lists = Single.zip(
            student,
            attendance,
            BiFunction<List<Student>, List<Attendance>, Pair<List<Student>, List<Attendance>>> { studentList, attendanceList ->
                Pair(studentList, attendanceList)
            })

        val word = lists.flatMap { list ->
            WordProcessor(
                fileSource.getTemplateDocument(),
                month
            ).processTable(
                Constants.SECTION_LIST.getValue(section),
                list.first,
                list.second
            )
        }.flatMapCompletable { saveToFile(it, outputLocation) }

        return word
    }

    fun getAttendance(owner: Activity, timestamp: Date) =
        firestore.getAttendance(owner, section, timestamp)

    val section: String
        get() = cache.getUserCache()!!.section


    fun getRelativeDateString(relative: Date): String {
        val relativeTime = getTodayDateLocal().time - relative.time

        val currentCalendar = GregorianCalendar.getInstance().apply {
            time = relative
        }

        val dayOfWeek = when (currentCalendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }

        if (relativeTime >= 0) {
            fun Long.countHowManyTime(unit: Long) = (this / unit).toInt()
            // Past
            when (val howManyDays = relativeTime.countHowManyTime(DateUtils.DAY_IN_MILLIS)) {
                0 -> return "Today, ${dayOfWeek.toLowerCase(Locale.getDefault())}…" // Today
                1 -> return "$dayOfWeek, yesterday…" // Today
                in 2..6 -> return "$dayOfWeek, $howManyDays days ago…"
                7 -> return "Last ${dayOfWeek.toLowerCase(Locale.getDefault())}⁠…"
                in 8..Int.MAX_VALUE -> {
                    return when (val howManyWeeks =
                        relativeTime.countHowManyTime(DateUtils.WEEK_IN_MILLIS)) {
                        1 -> "$dayOfWeek, last week…"
                        in 2..3 -> "$dayOfWeek, $howManyWeeks weeks ago…"
                        4 -> "$dayOfWeek, last month…"
                        else -> "$dayOfWeek,"
                    }
                }
                else -> return dayOfWeek
            }
        } else return dayOfWeek
    }

//    private fun dayOfWeek(): String = when (currentCalendar.get(Calendar.DAY_OF_WEEK)) {
//        Calendar.SUNDAY -> "Sunday"
//        Calendar.MONDAY -> "Monday"
//        Calendar.TUESDAY -> "Tuesday"
//        Calendar.WEDNESDAY -> "Wednesday"
//        Calendar.THURSDAY -> "Thursday"
//        Calendar.FRIDAY -> "Friday"
//        Calendar.SATURDAY -> "Saturday"
//        else -> ""
//    }

//    private fun Long.isNegative(): Boolean {
//        return (this < 0L)
//    }

//    private fun Long.countHowManyTime(unit: Long) = (this / unit).toInt()

}