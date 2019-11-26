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

package com.snorlax.snorlax.data.barcode

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.reactivex.FlowableEmitter


class BarcodeAnalyzer(private val barcodeEmitter: FlowableEmitter<FirebaseVisionBarcode>) : ImageAnalysis.Analyzer {


    override fun analyze(image: ImageProxy, rotationDegrees: Int) {

            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_CODE_39)
                .build()

            val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

            val barcodeImage =
                FirebaseVisionImage.fromMediaImage(image.image!!, convertRotation(rotationDegrees))

            detector.detectInImage(barcodeImage)
                .addOnSuccessListener {
                    for (barcode in it) {
                        Log.d("BarcodeAnalysis", barcode.displayValue!!)
                        barcodeEmitter.onNext(barcode)
                    }
                }.addOnFailureListener {
                    barcodeEmitter.onError(it)
                }

//            lastAnalyzedTimestamp = currentTimestamp
//        }
    }

    private fun convertRotation(degrees: Int): Int {
        return when (degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> FirebaseVisionImageMetadata.ROTATION_0
        }
    }

}