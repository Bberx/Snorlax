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

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import com.snorlax.snorlax.BeabotApp
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.files.FileSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.utils.TimeUtils.getTodayDateLocal
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize with current date
    private val currentCalendar = Calendar.getInstance()

    private val cache = LocalCacheSource.getInstance()

    private val firestore = FirebaseFirestoreSource.getInstance()

    private val fileSource = FileSource.getInstance()

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
        DocumentsContract.deleteDocument(getApplication<BeabotApp>().contentResolver, location)
//        getApplication<Application>().contentResolver.delete(location, null, null)
    }

    fun isEmpty(document: Uri) = fileSource.isFileEmpty(getApplication(), document)

    fun exportAttendance(document: Uri): Completable {
        return Completable.create { emitter ->
            try {
                val template =
                    fileSource.getTemplateDocument(getApplication<BeabotApp>().resources.assets)

                val fileOutputStream = fileSource.getFileOutputStream(
                    getApplication<BeabotApp>().contentResolver,
                    document
                )

//                template.write(fileOutputStream)
//                template.close()
                fileOutputStream.use { stream ->
                    template.use {
                        it?.write(stream)
                    }
                }



                fileOutputStream?.close()

                emitter.onComplete()

            } catch (fileNotFound: FileNotFoundException) {
                emitter.onError(fileNotFound)
            } catch (ioException: IOException) {
                emitter.onError(ioException)
            }
        }
    }

    fun getAttendance(timestamp: Date) =
        firestore.getAttendanceQuery(getAdminSection(), timestamp)

    fun getAdminSection(): String {
        return cache.getUserCache(getApplication())!!.section
    }

//    fun getStudentList(context: Context): Single<List<Student>> {
//        return firestore.getStudentList(getAdminSection(context))
//            .subscribeOn(Schedulers.io())
//    }

    fun getRelativeDateString(relative: Date): String {
        val relativeTime = getTodayDateLocal().time - relative.time

        currentCalendar.timeInMillis = relative.time

        if (!relativeTime.isNegative()) {
            // Past
            when (val howManyDays = relativeTime.countHowManyTime(DateUtils.DAY_IN_MILLIS).first) {
                0 -> return "Today, ${dayOfWeek().toLowerCase(Locale.getDefault())}…" // Today
                1 -> return "${dayOfWeek()}, yesterday…" // Today
                in 2..6 -> return "${dayOfWeek()}, $howManyDays days ago…"
                7 -> return "Last ${dayOfWeek().toLowerCase(Locale.getDefault())}⁠…"
                in 8..Int.MAX_VALUE -> {
                    return when (val howManyWeeks =
                        relativeTime.countHowManyTime(DateUtils.WEEK_IN_MILLIS).first) {
                        1 -> "${dayOfWeek()}, last week…"
                        in 2..3 -> "${dayOfWeek()}, $howManyWeeks weeks ago…"
                        4 -> "${dayOfWeek()}, last month…"
                        else -> "${dayOfWeek()},"
                    }
                }
                else -> return ""
            }
        } else return ""
    }

    private fun dayOfWeek(): String = when (currentCalendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> ""
    }

    private fun Long.isNegative(): Boolean {
        return (this < 0L)
    }

    private fun Long.countHowManyTime(unit: Long): Pair<Int, Int> {
        return Pair((this / unit).toInt(), (this % unit).toInt())
    }
}