package com.snorlax.snorlax.utils.barcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.Px
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer


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

object BarcodeUtils {
    @Throws(WriterException::class, IllegalArgumentException::class)
    fun encodeToBitmap(content: String?, width: Int = 0, height: Int = 0): Bitmap =
        encode(content, width, height).toBitmap()


    @Throws(WriterException::class, IllegalArgumentException::class)
    fun encode(content: String?, @Px width: Int = 0, @Px height: Int = 0): BitMatrix =
        Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height)
}