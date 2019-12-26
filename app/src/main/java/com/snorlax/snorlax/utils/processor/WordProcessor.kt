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

package com.snorlax.snorlax.utils.processor

import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.model.Section
import com.snorlax.snorlax.model.Student
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.poi.xwpf.usermodel.UnderlinePatterns
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.text.SimpleDateFormat
import java.util.*


class WordProcessor(private val document: XWPFDocument, private val month: Date) {

    private val numOfDays: Int by lazy {
        GregorianCalendar(TimeZone.getTimeZone("UTC"))
            .apply { time = month }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Entry point
    fun processTable(
        section: Section,
        students: List<Student>,
        attendance: List<Attendance>
    ): Single<XWPFDocument> {

        // Process in sequence
        val process = Completable.concatArray(
            formatTable(students.size),
            populateTable(students, attendance),
            adjustSize()
        ).subscribeOn(Schedulers.io())

        // Process asynchronously
        return Completable.mergeArray(populateHeader(section), process)
            .toSingle { document }
            .subscribeOn(Schedulers.io())
    }

    private fun populateHeader(section: Section): Completable {
        return Completable.create { emitter ->
            try {
                val header = document.paragraphs[0]

                val gradeField = header.runs[1]!!
                val dateField = header.runs[3]!!

                gradeField.setText("${section.grade_level}-${section.display_name}", 0)
                dateField.setText(
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month),
                    0
                )

                gradeField.underline = UnderlinePatterns.SINGLE
                dateField.underline = UnderlinePatterns.SINGLE

                header.insertNewRun(2).setText(" ")
            } catch (error: Exception) {
                emitter.onError(error)
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
    }

    // 1. Correct number of students
    // 2. Correct number of days
    // TODO
    private fun formatTable(numberOfStudents: Int): Completable {

        return Completable.complete()
    }

    // After formatTable
    // TODO
    private fun populateTable(students: List<Student>, attendance: List<Attendance>): Completable {
        val table = document.tables[0]



        return Completable.complete()
//        return Completable.error(Throwable("Test"))
    }

    // After populate table
    // Autofit name column
    // TODO
    private fun adjustSize(): Completable {
        return Completable.complete()
    }


}