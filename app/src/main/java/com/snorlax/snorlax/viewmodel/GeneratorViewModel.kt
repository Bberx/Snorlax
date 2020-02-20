package com.snorlax.snorlax.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.getSystemService
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.BarcodeBitmap
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.Constants
import com.snorlax.snorlax.utils.FileUtils
import com.snorlax.snorlax.utils.barcode.BarcodeUtils
import com.snorlax.snorlax.utils.barcode.writeToFile
import com.snorlax.snorlax.utils.processor.BarcodeProcessor
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import kotlin.text.Typography.section

class GeneratorViewModel(application: Application) : BaseStudentViewModel(application) {
    val barcodeBitmapManualObservable = BehaviorSubject.create<BarcodeBitmap>()
    val barcodeBitmapClassObservable = BehaviorSubject.create<BarcodeBitmap>()
    val buttonObservable = BehaviorSubject.create<Boolean>()

    val section: String
        get() = LocalCacheSource.getInstance(getApplication()).getUserCache()!!.section

    fun encodeBarcodeManual(lrn: String) {
        barcodeBitmapManualObservable.onNext(BarcodeUtils.encodeToBitmap(lrn))
    }

    fun encodeBarcodeClass(lrn: String) {
        barcodeBitmapClassObservable.onNext(BarcodeUtils.encodeToBitmap(lrn))
    }

    fun saveImage(barcode: BarcodeBitmap, outputLocation: Uri): Completable {
        return barcode.writeToFile(FileUtils.getFileOutputStream(getApplication(), outputLocation))
            .subscribeOn(Schedulers.io())
    }

    fun outputFileName(outputLocation: Uri) =
        FileUtils.getFileName(getApplication(), outputLocation)

    fun deleteFile(location: Uri) {
        if (!DocumentsContract.deleteDocument(
                getApplication<Application>().contentResolver,
                location
            )
        ) deleteFile(location)
    }

    fun getSaveImageIntent(lrn: String, name: String? = null) =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            val fileName: String =
                if (name == null) "Barcode_${lrn}.png" else "Barcode_${name}-${lrn}.png"
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }

    private fun writeToFile(document: XWPFDocument, outputLocation: Uri) = Completable.fromAction {
        FileUtils.getFileOutputStream(getApplication(), outputLocation).use { stream ->
            document.use {
                it.write(stream)
            }
        }
    }.subscribeOn(Schedulers.io())

    fun saveClassBarcode(outputLocation: Uri): Completable {
        val students = FirebaseFirestoreSource.getStudentList(section).subscribeOn(Schedulers.io())

        return students.flatMap {
            BarcodeProcessor(
                FileUtils.getTemplateDocument(
                    getApplication(),
                    "BarcodeSheetTemplate.docx"
                )
            ).processTable(Constants.SECTION_LIST.getValue(section), it)
        }.subscribeOn(Schedulers.io()).flatMapCompletable { writeToFile(it, outputLocation) }
        // TODO: implement
    }

    fun isEmpty(document: Uri) = FileUtils.isFileEmpty(getApplication(), document)

    fun saveToClipboard(lrn: String) {
        val clipboard = getApplication<Application>().getSystemService<ClipboardManager>()!!
        val clipData = ClipData.newPlainText("LRN", lrn)
        clipboard.setPrimaryClip(clipData)
    }
}
