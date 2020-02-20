package com.snorlax.snorlax.utils.processor

import android.graphics.Bitmap
import android.util.Log
import com.snorlax.snorlax.model.BarcodeBitmap
import com.snorlax.snorlax.model.Section
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.barcode.BarcodeUtils
import com.snorlax.snorlax.utils.cmToEMU
import com.snorlax.snorlax.utils.emuToTwips
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.poi.xwpf.usermodel.*
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.math.BigInteger
import kotlin.math.roundToInt

class BarcodeProcessor(document: XWPFDocument) : BaseProcessor(document) {

    companion object {
        private const val BARCODE_HEIGHT_CM = 1.5
        private const val BARCODE_WIDTH_CM = 6.0
    }

    fun processTable(section: Section, students: List<Student>): Single<XWPFDocument> {
        val process = Completable.concatArray(
            formatTable(students.size),
            populateTable(students),
            adjustSize()
        ).subscribeOn(Schedulers.io())

        return Completable.mergeArray(populateHeader(section), process).toSingleDefault(document)
    }

    private fun writeBitmap(barcode: BarcodeBitmap, run: XWPFRun): Completable {

        val pout = PipedOutputStream()
        val pin = PipedInputStream(pout)

        val writeThread = Completable.fromAction {
            pout.use {
                barcode.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }.subscribeOn(Schedulers.computation())

        val pictureThread = Completable.fromAction {
            pin.use {
                run.addPicture(
                    it,
                    Document.PICTURE_TYPE_PNG,
                    barcode.value,
                    BARCODE_WIDTH_CM.cmToEMU().roundToInt(),
                    BARCODE_HEIGHT_CM.cmToEMU().roundToInt()
                )
            }
        }.subscribeOn(Schedulers.io())

        return Completable.mergeArray(writeThread, pictureThread).subscribeOn(Schedulers.io())
    }

    private fun populateHeader(section: Section): Completable {
        return Completable.fromAction {
            val gradeField = document.paragraphs[0].runs[1]!!
            gradeField.setText("${section.grade_level}-${section.display_name}", 0)
            gradeField.underline = UnderlinePatterns.SINGLE
        }
    }

    private fun formatTable(numberOfStudents: Int): Completable {
        return Completable.fromAction {
            repeat(numberOfStudents) {
                table.createRow()
            }
        }
    }

    private fun populateTable(students: List<Student>): Completable {
        return Completable.fromAction {
            students.forEachIndexed { index, student ->
                val row = table.rows[index + 1]
                val studentNamePara = row.getCell(0).paragraphs[0]
                val studentLRNPara = row.getCell(1).paragraphs[0]
                val studentBarcodePara = row.getCell(2).paragraphs[0]

//                setSingleLineSpacing(studentCell.paragraphs[0])

                fun XWPFParagraph.xyCenter() {
                    ctp.pPr = ctp.addNewPPr()
                    ctp.pPr.jc = ctp.pPr.addNewJc()
                    ctp.pPr.jc.`val` = STJc.CENTER
                }

                studentNamePara.createRun().run {
                    fontFamily = "Arial"
                    fontSize = 10
                    setText(student.displayName)
                }

                studentLRNPara.createRun().run {
                    fontFamily = "Arial"
                    fontSize = 10
                    setText(student.lrn)
                }
                studentLRNPara.xyCenter()

                val barcodeRun = studentBarcodePara.createRun()
                studentBarcodePara.xyCenter()

                writeBitmap(BarcodeUtils.encodeToBitmap(student.lrn), barcodeRun).blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun adjustSize(): Completable {
        return Completable.fromAction {
            // Set table width to fill printable area
            table.ctTbl.tblPr.tblW.w = BigInteger.valueOf(5000)
            table.ctTbl.tblPr.tblW.type = STTblWidth.PCT
//            table.rows[0].isRepeatHeader = true

            table.rows.forEachIndexed { index, row ->
                row.tableCells.forEach {cell ->
                    cell.verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
                }

                // If row is not header
                if (index != 0) {
                    row.height =
                        (BARCODE_HEIGHT_CM.cmToEMU().emuToTwips() + 0.25.cmToEMU().emuToTwips()).roundToInt()
                }
            }
        }
    }
}