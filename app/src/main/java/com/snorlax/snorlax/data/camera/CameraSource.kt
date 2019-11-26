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

package com.snorlax.snorlax.data.camera

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.snorlax.snorlax.data.barcode.BarcodeAnalyzer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor


class CameraSource(lifecycleOwner: LifecycleOwner): LifecycleObserver {

    //    val cameraContent : PublishSubject<SurfaceTexture> = PublishSubject.create()
//
//
//    fun startCamera() {
//
//        val previewConfig = PreviewConfig.Builder().apply {
//            setTargetAspectRatio(Rational(1, 1))
//            setTargetResolution(Size(720, 720))
//        }.build()
//
//        val preview = Preview(previewConfig)
//
//
//        preview.setOnPreviewOutputUpdateListener {
//            Toast.makeText(mContext, "In camera source", Toast.LENGTH_SHORT).show()
//            cameraContent.onNext(it.surfaceTexture)
//
////            val parent = viewFinder.parent as ViewGroup
////            parent.removeView(viewFinder)
////            parent.addView(viewFinder, 0)
////
////            viewFinder.surfaceTexture = it.surfaceTexture
////            updateTransform()
//        }
//
//        // Setup image analysis pipeline that scans barcode
//        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//
//            // Use a worker thread for image analysis to prevent glitches
//            val analyzerThread = HandlerThread("BarcodeAnalysis").apply { start() }
//            setCallbackHandler(Handler(analyzerThread.looper))
//
//            // In our analysis, we care more about the latest image than
//            // analyzing *every* image
//            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//            setTargetAspectRatio(Rational(1, 1))
//            setTargetResolution(Size(640, 640))
//        }.build()
//
//
//
//        // Build the image analysis use case and instantiate our analyzer
////        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
////            analyzer = BarcodeAnalyzer(callback)
////        }
//        Toast.makeText(mContext, "In camera source bind", Toast.LENGTH_SHORT).show()
//        CameraX.bindToLifecycle(mContext as LifecycleOwner, preview)
//    }
//
//    fun getTextureObservable() : Observable<SurfaceTexture> {
//        startCamera()
//        return  cameraContent
//    }
//
////    fun updateTransform() {
////        val matrix = Matrix()
////
////        val centerX = viewFinder.width / 2f
////        val centerY = viewFinder.height / 2f
////
////        val rotationDegrees = when (viewFinder.display.rotation) {
////            Surface.ROTATION_0 -> 0
////            Surface.ROTATION_90 -> 90
////            Surface.ROTATION_180 -> 180
////            Surface.ROTATION_270 -> 270
////            else -> return
////        }
////
////        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
////    }

//    companion object {
//        private var instance: CameraSource? = null
//
//        fun getInstance(): CameraSource {
//            instance?.let {
//                return it
//            }
//            instance = CameraSource()
//            return getInstance()
//        }
//
//        // Setup preview case config
////        private val previewConfig = PreviewConfig.Builder().apply {
//////                        setTargetAspectRatio(AspectRatio.RATIO_16_9)
////
//////            setTargetAspectRatioCustom(Rational(9, 16))
//////            setTargetRotation(Surface.ROTATION_90)
//////            setTargetResolution(Size(1280, 720))
////
//////            setTargetAspectRatioCustom(Rational(16, 9))
//////            setTargetResolution(Size(1920, 1080))
//////            setTargetRotation(Surface.ROTATION_180)
////
////
////        }.build()
////
////        private val preview = Preview(previewConfig)
//
//
//        // Setup image analysis pipeline that scans barcode
////        private val analyzerConfig = ImageAnalysisConfig.Builder().apply {
////
////            // In our analysis, we care more about the latest image than
////            // analyzing *every* image
////            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
////            setTargetAspectRatio(AspectRatio.RATIO_4_3)
////
//////            setTargetResolution(Size(1920, 1080))
////        }.build()
////
////        // Build the image analysis use case and instantiate our analyzer
////        private val barcodeAnalyzer = ImageAnalysis(analyzerConfig)
//    }

//    fun getPreviewObservable(): Observable<Preview.PreviewOutput> {
//        return Observable.create { observableEmitter ->
//            preview.setOnPreviewOutputUpdateListener {
//                if (!observableEmitter.isDisposed) {
//                    observableEmitter.onNext(it)
//                }
//            }
//        }
//    }

    companion object {
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {

            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            setTargetAspectRatio(AspectRatio.RATIO_4_3)

//            setTargetResolution(Size(1920, 1080))
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        private val barcodeAnalyzer = ImageAnalysis(analyzerConfig)
    }


    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

//    private var barcodeAnalyzer: ImageAnalysis? = null
    private val disposable = CompositeDisposable()

    fun getBarcodeObservable(): Flowable<FirebaseVisionBarcode> {
        return Flowable.create({
            barcodeAnalyzer.setAnalyzer(Executor { command ->
                // Use a worker thread for image analysis to prevent glitches
//                Handler(analyzerThread.looper).apply { post(command) }
//                Completable
//                    .fromRunnable(command)
//                    .toFlowable<Unit>()
//                    .onBackpressureDrop()
//                    .subscribeOn(Schedulers.computation())
//                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
//                    .subscribe()

                disposable.add(Flowable.fromCallable{command.run()}
                    .onBackpressureDrop()
                    .subscribeOn(Schedulers.computation())
//                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
                    .subscribe())

            }, BarcodeAnalyzer(it))
        }, BackpressureStrategy.DROP)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroyObservable() {
        disposable.dispose()
    }

    fun startCamera(ownerActivity: LifecycleOwner) {
//        preview.setTargetRotation(Surface.ROTATION_90)
//        if (CameraX.isBound(preview)) {
//            CameraX.unbind(preview)
//            CameraX.bindToLifecycle(ownerActivity, preview)
//        } else CameraX.bindToLifecycle(ownerActivity, preview)



        if (CameraX.isBound(barcodeAnalyzer)) {
            CameraX.unbind(barcodeAnalyzer)
            CameraX.bindToLifecycle(ownerActivity, barcodeAnalyzer)
        } else CameraX.bindToLifecycle(ownerActivity, barcodeAnalyzer)
    }
}