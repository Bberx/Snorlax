package com.snorlax.snorlax.utils.barcode

import android.graphics.Bitmap
import androidx.annotation.Px
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.snorlax.snorlax.model.BarcodeBitmap
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.FileOutputStream
import java.io.IOException


private const val WHITE = -1
private const val BLACK = 0

fun BitMatrix.toBitmap(): Bitmap {
    val pixels = IntArray(width * height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (this[x, y]) BLACK else WHITE
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}

fun BarcodeBitmap.writeToFile(fileOutputStream: FileOutputStream): Completable {
    return Single.fromCallable { bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream) }
        .subscribeOn(Schedulers.io())
        .flatMapCompletable {
            if (it) Completable.complete() else Completable.error(IOException("Failed writing to file"))
        }

}

object BarcodeUtils {
    @Throws(WriterException::class, IllegalArgumentException::class)
    fun encodeToBitmap(content: String, width: Int = 1200, height: Int = 300): BarcodeBitmap =
        BarcodeBitmap(encode(content, width, height).toBitmap(), content)


    @Throws(WriterException::class, IllegalArgumentException::class)
    fun encode(content: String?, @Px width: Int = 1200, @Px height: Int = 300): BitMatrix =
        Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height)
}