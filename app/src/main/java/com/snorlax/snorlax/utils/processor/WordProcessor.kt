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
import com.snorlax.snorlax.utils.TimeUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.poi.xwpf.usermodel.UnderlinePatterns
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*


class WordProcessor(private val document: XWPFDocument, private val month: Date) {

    private val numOfDays: Int by lazy {
        TimeUtils.numOfDays(month)
    }

    private val table = document.tables.first()
    private val currentDay: Int = GregorianCalendar.getInstance().apply {
        time = TimeUtils.getTodayDateLocal()
    }.get(Calendar.DAY_OF_MONTH)


    private fun setSingleLineSpacing(para: XWPFParagraph) {
        para.ctp.pPr = table.rows[0].getCell(0).paragraphs[0].ctp.pPr
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
        return Completable.fromAction {
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
        }.subscribeOn(Schedulers.io())
    }

    // 1. Correct number of students
    // 2. Correct number of days
    // TODO
    private fun formatTable(numberOfStudents: Int): Completable {
        return Completable.fromAction {
            repeat(numOfDays) {
                val day = it + 1
                val cell = table.rows[0].addNewTableCell()

                cell.paragraphs[0].createRun().run {
                    fontFamily = "Arial"
                    fontSize = 10
                    isBold = true
                    setText(day.toString())
                }
                setSingleLineSpacing(cell.paragraphs[0])
                cell.paragraphs[0].ctp.pPr.jc = cell.paragraphs[0].ctp.pPr.addNewJc()
                cell.paragraphs[0].ctp.pPr.jc.`val` = STJc.CENTER
            }

            repeat(numberOfStudents) {
                table.createRow()
            }
        }
    }

    // After formatTable
    // TODO
    private fun populateTable(students: List<Student>, attendance: List<Attendance>): Completable {
//        return Completable.complete()


        return Completable.fromAction {
            students.forEachIndexed { index, student ->
                val row = table.rows[index + 1]
                val studentCell = row.getCell(0)

                setSingleLineSpacing(studentCell.paragraphs[0])

                studentCell.paragraphs[0].createRun().run {
                    fontFamily = "Arial"
                    fontSize = 10
                    setText(student.displayName)
                }

                val studentAttendance = attendance.filter {
                    student == it.student
                }


                // Fill each day with "A" first
                repeat(currentDay) {
                    val cell = row.getCell(it + 1)
                    setSingleLineSpacing(cell.paragraphs[0])

                    cell.paragraphs[0].createRun().run {
                        fontFamily = "Arial"
                        fontSize = 10
                        setText("A")
                    }

                    cell.paragraphs[0].ctp.pPr.jc = cell.paragraphs[0].ctp.pPr.addNewJc()
                    cell.paragraphs[0].ctp.pPr.jc.`val` = STJc.CENTER

                    val day = it + 1
                    if (day == currentDay) {
                        repeat(numOfDays - day) { emptyDayIndex ->
                            setSingleLineSpacing(row.getCell(day + emptyDayIndex + 1).paragraphs[0])
                        }
                    }
                }

                studentAttendance.forEach {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        time = it.time_in.toDate()
                    }
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    row.getCell(day).paragraphs[0].runs[0].run {
                        setText("/", 0)
                    }
                }


            }
        }
    }

    // After populate table
    // Autofit name column

    // TODO
    private fun adjustSize(): Completable {
        return Completable.fromAction {
            table.rows.forEach {
                it.tableCells.forEach { cell ->
                    cell.verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
                }
            }

            // Set table to auto fit document size
            table.ctTbl.tblPr.tblW.w = BigInteger.valueOf(5000)
            table.ctTbl.tblPr.tblW.type = STTblWidth.PCT
        }
    }


}