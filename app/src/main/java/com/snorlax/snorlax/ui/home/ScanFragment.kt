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

package com.snorlax.snorlax.ui.home


import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.view.CameraView
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.barcode.BarcodeAnalyzer
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentScannerAdaptor
import com.snorlax.snorlax.viewmodel.ScanViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.camera_placeholder.view.*
import kotlinx.android.synthetic.main.fragment_scan.view.*
import java.util.*
import java.util.concurrent.Executor


/**
 * A simple [Fragment] subclass.
 */
class ScanFragment : Fragment() {

    private lateinit var viewModel: ScanViewModel

    private val studentScannerAdaptor: StudentScannerAdaptor by lazy {
        StudentScannerAdaptor()
    }

//    private lateinit var cameraPlaceholderView: View
//
////    private lateinit var cameraPreview: TextureView
//
//    private lateinit var testView: CameraView

    private val vibrator: Vibrator by lazy {
        context!!.getSystemService<Vibrator>()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[ScanViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

    }

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_scan, container, false)

        val cameraPlaceholderView =
            inflater.inflate(R.layout.camera_placeholder, rootView.camera_frame, false)

        val testView = CameraView(context!!).apply {
            flash = FlashMode.OFF
            captureMode = CameraView.CaptureMode.IMAGE
            scaleType = CameraView.ScaleType.CENTER_CROP
            isPinchToZoomEnabled = false
        }

        rootView.camera_frame.addView(cameraPlaceholderView.apply {
            this.error_message.text = getString(R.string.msg_camera_loading)
            this.setBackgroundResource(0)
        }, 0)

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {

            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            setTargetAspectRatio(AspectRatio.RATIO_4_3)

//            setTargetResolution(Size(1920, 1080))
        }.build()
        val barcodeAnalyzer = ImageAnalysis(analyzerConfig)

        // fixme remove me
//        rootView.debug_save.setOnClickListener {
//            addAttendance()
//        }

        val camera = viewModel.permissionObservable
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                if (it) {
                    if (CameraX.isBound(barcodeAnalyzer)) {
                        CameraX.unbind(barcodeAnalyzer)
                        CameraX.bindToLifecycle(this, barcodeAnalyzer)
                    } else CameraX.bindToLifecycle(this, barcodeAnalyzer)
//                    viewModel.startCamera(this)
                    testView.bindToLifecycle(this)
                } else {
                    CameraX.unbindAll()
                    rootView.camera_frame.removeAllViews()
                    rootView.camera_frame.setOnClickListener {
                        viewModel.requestPermission(this, false)
                    }
                    val attrs = intArrayOf(R.attr.selectableItemBackground)
                    val typedArray: TypedArray = activity!!.obtainStyledAttributes(attrs)
                    val backgroundResource = typedArray.getResourceId(0, 0)
                    typedArray.recycle()
                    rootView.camera_frame.setBackgroundResource(backgroundResource)
                    rootView.camera_frame.addView(cameraPlaceholderView.apply {
                        this.error_message.text = getString(R.string.msg_camera_no_permission)
                    })
                }
            }
            .subscribe {
                if (it) {
                    val parent = rootView.camera_frame
                    parent.setOnClickListener(null)
                    parent.setBackgroundResource(0)
                    parent.removeAllViews()

                    parent.addView(testView, 0)
                }
            }
//            .flatMap { viewModel.getCameraPreview() }


//            .subscribe { result ->
//                val parent = camera_frame
////                val cameraPreview = CameraPreview(context!!)
//
//
//
//
//
////                cameraPreview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
////                    override fun onSurfaceTextureSizeChanged(
////                        surface: SurfaceTexture?,
////                        width: Int,
////                        height: Int
////                    ) {}
////                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
////                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = cameraPreview.surfaceTexture == null
////                    override fun onSurfaceTextureAvailable(
////                        surface: SurfaceTexture?,
////                        width: Int,
////                        height: Int
////                    ) { cameraPreview.surfaceTexture = result.surfaceTexture }
////                }
//
////                parent.setOnClickListener(null)
////                parent.setBackgroundResource(0)
////                parent.removeAllViews()
////
////                parent.addView(testView, 0)
//            }

        rootView.student_log_list.layoutManager = LinearLayoutManager(context)
        rootView.student_log_list.adapter = studentScannerAdaptor

        // TODO conflicts MVVM

        val barcode =
//            viewModel.getBarcode()
            Flowable.create<FirebaseVisionBarcode>({
                barcodeAnalyzer.setAnalyzer(Executor { command ->
                    // Use a worker thread for image analysis to prevent glitches
//                    val analyzerThread = HandlerThread("BarcodeAnalyzer")
//                Handler(analyzerThread.looper).apply { post(command) }
//                Completable
//                    .fromRunnable(command)
//                    .toFlowable<Unit>()
//                    .onBackpressureDrop()
//                    .subscribeOn(Schedulers.computation())
//                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
//                    .subscribe()

                    disposables.add(Flowable.fromCallable { command.run() }
                        .onBackpressureDrop()
                        .subscribeOn(Schedulers.computation())
//                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
                        .subscribe())

                }, BarcodeAnalyzer(it))
            }, BackpressureStrategy.DROP)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMapMaybe { viewModel.analyzeLRN(context!!, it.displayValue!!) }
                .map { student ->
                    Log.d("Threading", "mapping ${Thread.currentThread().name}")
                    Pair(student !in studentScannerAdaptor.studentList.map { it.first }, student)
                }
                .doOnNext { shouldAdd ->
                    if (shouldAdd.first) {
                        Log.d("Threading", "adding list ${Thread.currentThread().name}")
                        studentScannerAdaptor.studentList.add(
                            Pair(shouldAdd.second, viewModel.getCurrentTime().time)
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.first) {
                        studentScannerAdaptor.notifyDataSetChanged()
                        vibrate()
                        addAttendance()
                        Log.d("Threading", "data changed ${Thread.currentThread().name}")
                    }

                }, { Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show() })

        disposables.addAll(camera, barcode)

        return rootView
    }

    private fun addAttendance() {
        val attendanceEntry = studentScannerAdaptor.studentList.flatMap {
            listOf(
                Attendance(
                    Timestamp(Date(it.second)),
                    viewModel.getStudentDocumentReference(context!!, it.first.lrn),
                    it.first.lrn
                )
            )
        }

        disposables.add(viewModel.addAttendance(context!!, attendanceEntry).subscribe({
            Snackbar.make(view!!, "Success", Snackbar.LENGTH_LONG).show()
        }, {
            Snackbar.make(view!!, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
        }))
    }

    private fun vibrate() {
        val pattern: LongArray = longArrayOf(0, 50, 25, 50)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            vibrator.vibrate(pattern, -1)
        }
    }

    override fun onDestroy() {

        super.onDestroy()
        disposables.dispose()
    }

    override fun onStart() {
        super.onStart()
        viewModel.requestPermission(this, true)
    }

    override fun onStop() {
        super.onStop()
        CameraX.unbindAll()
    }
}
