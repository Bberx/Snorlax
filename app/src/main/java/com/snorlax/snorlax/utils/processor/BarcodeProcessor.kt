package com.snorlax.snorlax.utils.processor

import android.graphics.Bitmap
import com.snorlax.snorlax.model.BarcodeBitmap
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.Document
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFPicture
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.PipedInputStream
import java.io.PipedOutputStream

class BarcodeProcessor(document: XWPFDocument) : BaseProcessor(document) {

    fun writeBitmap(barcode: BarcodeBitmap, run: XWPFRun): Single<XWPFPicture> {
        return Single.create<XWPFPicture> { emitter ->
            val input = PipedInputStream()
            val output = PipedOutputStream(input)

            Completable.fromAction {
                barcode.bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }.subscribeOn(Schedulers.computation()).subscribe()

            val picture = run.addPicture(
                input,
                Document.PICTURE_TYPE_PNG,
                barcode.value,
                Units.pixelToEMU(barcode.bitmap.width),
                Units.pixelToEMU(barcode.bitmap.height)
            )
            emitter.onSuccess(picture)
        }.subscribeOn(Schedulers.io())
    }
}