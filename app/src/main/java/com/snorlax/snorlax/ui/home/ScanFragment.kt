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


import android.content.Intent
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.view.CameraView
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.camera_placeholder.view.*
import kotlinx.android.synthetic.main.fragment_scan.*
import kotlinx.android.synthetic.main.fragment_scan.view.*
import java.util.*
import java.util.concurrent.Executor

class ScanFragment : Fragment() {

    private lateinit var viewModel: ScanViewModel

    private val studentScannerAdaptor: StudentScannerAdaptor by lazy {
        StudentScannerAdaptor()
    }

    private val vibrator: Vibrator by lazy {
        requireContext().getSystemService<Vibrator>()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this)[ScanViewModel::class.java]
        } ?: throw RuntimeException("Invalid Activity")

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

        val cameraView = CameraView(requireContext()).apply {
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

        }.build()
        val barcodeAnalyzer = ImageAnalysis(analyzerConfig)

        val camera = viewModel.permissionObservable
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                if (it && viewModel.isAutoTimeEnabled() && viewModel.isAutoTimeZoneEnabled()) {
                    if (CameraX.isBound(barcodeAnalyzer)) {
                        CameraX.unbind(barcodeAnalyzer)
                        CameraX.bindToLifecycle(this, barcodeAnalyzer)
                    } else CameraX.bindToLifecycle(this, barcodeAnalyzer)
                    cameraView.bindToLifecycle(this)
                } else {
                    CameraX.unbindAll()
                    rootView.camera_frame.removeAllViews()

                    val attrs = intArrayOf(R.attr.selectableItemBackground)
                    val typedArray: TypedArray = requireActivity().obtainStyledAttributes(attrs)
                    val backgroundResource = typedArray.getResourceId(0, 0)
                    typedArray.recycle()
                    rootView.camera_frame.setBackgroundResource(backgroundResource)

                    fun getErrorMessage(): String = when {
                        !viewModel.isAutoTimeEnabled() && !viewModel.isAutoTimeZoneEnabled() -> {
                            getString(R.string.msg_enable_auto_time_and_time_zone)
                        }
                        !viewModel.isAutoTimeEnabled() && viewModel.isAutoTimeZoneEnabled() -> {
                            getString(R.string.msg_enable_auto_time)
                        }
                        viewModel.isAutoTimeEnabled() && !viewModel.isAutoTimeZoneEnabled() -> {
                            getString(R.string.msg_enable_auto_time_zone)
                        }
                        else -> {
                            getString(R.string.msg_enable_camera_permission)
                        }
                    }

                    fun getErrorClickListener(): View.OnClickListener {
                        return when {
                            !viewModel.isAutoTimeEnabled() || !viewModel.isAutoTimeZoneEnabled() -> View.OnClickListener {
                                startActivityForResult(Intent(Settings.ACTION_DATE_SETTINGS), 3000)
                            }
                            else -> View.OnClickListener {
                                viewModel.requestPermission(this@ScanFragment, false)
                            }
                        }
                    }
                    rootView.camera_frame.addView(cameraPlaceholderView.apply {
                        error_message.text = getErrorMessage()
                    })
                    rootView.camera_frame.setOnClickListener(getErrorClickListener())

//                    if (!viewModel.isAutoTimeEnabled()) {
//                        rootView.camera_frame.addView(cameraPlaceholderView.apply {
//                            //                            this.error_message.text = if (!viewModel.isAutoTimeEnabled()) getString(R.string.msg_enable_automatic_time) else if (!viewModel.isAutoTimeZoneEnabled()) "Please enable automatic time zone"
//                            this.error_message.text = getErrorMessage()
//
//
//                            rootView.camera_frame.setOnClickListener {
//                                startActivityForResult(Intent(Settings.ACTION_DATE_SETTINGS), 69)
//                            }
//                        })
//                    } else if (!viewModel.isAutoTimeZoneEnabled()) {
//
//                    } else {
//                        rootView.camera_frame.addView(cameraPlaceholderView.apply {
//                            this.error_message.text = getString(R.string.msg_enable_camera_permission)
//                            rootView.camera_frame.setOnClickListener {
//                                viewModel.requestPermission(this@ScanFragment, false)
//                            }
//                        })
//                    }
                }
            }
            .subscribe {
                if (it) {
                    val parent = rootView.camera_frame
                    parent.setOnClickListener(null)
                    parent.setBackgroundResource(0)
                    parent.removeAllViews()

                    parent.addView(cameraView, 0)
                }
            }

        rootView.student_log_list.layoutManager = LinearLayoutManager(context)
        rootView.student_log_list.adapter = studentScannerAdaptor

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
                    disposables.add(Completable.fromAction { command.run() }
                        .subscribeOn(Schedulers.computation())
//                        .onBackpressureDrop()
//                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
                        .subscribe())

                }, BarcodeAnalyzer(it))
            }, BackpressureStrategy.DROP)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMapMaybe { viewModel.analyzeLRN(it.displayValue!!) }
                .map { student ->
                    Log.d("Threading", "mapping ${Thread.currentThread().name}")
                    Pair(student !in studentScannerAdaptor.studentList.map { it.first }, student)
                }
                .doOnNext { shouldAdd ->
                    if (shouldAdd.first) {
                        studentScannerAdaptor.studentList.add(
                            Pair(shouldAdd.second, viewModel.getCurrentTime().time)
                        )
                    }
                }
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.first) {
                        student_log_list.post {
                            studentScannerAdaptor.notifyDataSetChanged()
                        }
                        vibrate()
                        addAttendance()
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
                    viewModel.getStudent(it.first.lrn),
//                    it.first,
                    it.first.lrn
                )
            )
        }

        disposables.add(viewModel.addAttendance(attendanceEntry)
            .subscribeBy(onError = {
                Snackbar.make(requireView(), it.localizedMessage ?: "", Snackbar.LENGTH_LONG).show()
            }))
//            .subscribe({
////            Snackbar.make(requireView(), "Attendance saved", Snackbar.LENGTH_LONG).show()
//        }, {
//            Snackbar.make(requireView(), it.localizedMessage ?: "", Snackbar.LENGTH_LONG).show()
//        }))
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

        disposables.dispose()
        super.onDestroy()
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
