package com.snorlax.snorlax.viewmodel

import android.app.Application
import com.snorlax.snorlax.utils.barcode.BarcodeUtils

class GeneratorClassViewModel(application: Application) : BaseStudentViewModel(application) {


    fun encodeBarcode(lrn: String, width: Int, height: Int) =
        BarcodeUtils.encodeToBitmap(lrn, width, height)

}
