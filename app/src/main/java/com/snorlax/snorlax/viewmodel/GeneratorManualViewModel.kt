package com.snorlax.snorlax.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.snorlax.snorlax.utils.barcode.BarcodeUtils
import io.reactivex.subjects.BehaviorSubject

class GeneratorManualViewModel : ViewModel() {
    val barcodeBitmapObservable = BehaviorSubject.create<Bitmap>()

    fun updateBarcode(lrn: String) {
        barcodeBitmapObservable.onNext(BarcodeUtils.encodeToBitmap(lrn))
    }
}
