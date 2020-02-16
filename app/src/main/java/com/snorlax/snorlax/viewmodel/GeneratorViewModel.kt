package com.snorlax.snorlax.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.getSystemService
import com.snorlax.snorlax.model.BarcodeBitmap
import com.snorlax.snorlax.utils.FileUtils
import com.snorlax.snorlax.utils.barcode.BarcodeUtils
import com.snorlax.snorlax.utils.barcode.writeToFile
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class GeneratorViewModel(application: Application) : BaseStudentViewModel(application) {
    val barcodeBitmapManualObservable = BehaviorSubject.create<BarcodeBitmap>()
    val barcodeBitmapClassObservable = BehaviorSubject.create<BarcodeBitmap>()
    val buttonObservable = BehaviorSubject.create<Boolean>()

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

    fun outputFileName(outputLocation: Uri) = FileUtils.getFileName(getApplication(), outputLocation)

    fun deleteFile(location: Uri) {
        if (!DocumentsContract.deleteDocument(
                getApplication<Application>().contentResolver,
                location
            )
        ) deleteFile(location)
    }

    fun getSaveImageIntent(lrn: String, name: String? = null) = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        val fileName: String = if (name == null) "${lrn}_barcode.png" else "${name}-${lrn}_barcode.png"
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "image/png"
        putExtra(Intent.EXTRA_TITLE, fileName)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }

    fun isEmpty(document: Uri) = FileUtils.isFileEmpty(getApplication(), document)

    fun saveToClipboard(lrn: String) {
        val clipboard = getApplication<Application>().getSystemService<ClipboardManager>()!!
        val clipData = ClipData.newPlainText("LRN", lrn)
        clipboard.setPrimaryClip(clipData)
    }
}
