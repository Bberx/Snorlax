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

package com.snorlax.snorlax.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import com.snorlax.snorlax.BeabotApp
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.model.Student
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

class ScanViewModel(application: Application) : AndroidViewModel(application) {

//    private val disposable = CompositeDisposable()

    private val cacheSource = LocalCacheSource.getInstance(application)
    //    private val cameraSource = CameraSource()
    private val firestore = FirebaseFirestoreSource.getInstance()

    val permissionObservable = PublishSubject.create<Boolean>()

//    fun startCamera(owner: LifecycleOwner) = cameraSource.startCamera(owner)
//    fun getCameraPreview() = cameraSource.getPreviewObservable()


//    val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//
//        // In our analysis, we care more about the latest image than
//        // analyzing *every* image
//        setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//        setTargetAspectRatio(AspectRatio.RATIO_4_3)
//
////            setTargetResolution(Size(1920, 1080))
//    }.build()
//    val barcodeAnalyzer = ImageAnalysis(analyzerConfig)

//    fun startCamera(lifecycleOwner: LifecycleOwner) {
//        if (CameraX.isBound(barcodeAnalyzer)) {
//            CameraX.unbind(barcodeAnalyzer)
//            CameraX.bindToLifecycle(lifecycleOwner, barcodeAnalyzer)
//        } else CameraX.bindToLifecycle(lifecycleOwner, barcodeAnalyzer)
//    }
//    fun getBarcode() : Flowable<FirebaseVisionBarcode> {
//
//        return Flowable.create({
//            barcodeAnalyzer.setAnalyzer(Executor { command ->
//                // Use a worker thread for image analysis to prevent glitches
////                Handler(analyzerThread.looper).apply { post(command) }
////                Completable
////                    .fromRunnable(command)
////                    .toFlowable<Unit>()
////                    .onBackpressureDrop()
////                    .subscribeOn(Schedulers.computation())
////                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
////                    .subscribe()
//
//                disposable.add(Flowable.fromCallable{command.run()}
//                    .onBackpressureDrop()
//                    .subscribeOn(Schedulers.computation())
////                    .doOnComplete { Log.d("Threading", " barcode ${Thread.currentThread().name}") }
//                    .subscribe())
//
//            }, BarcodeAnalyzer(it))
//        }, BackpressureStrategy.DROP)
//    }

    fun analyzeLRN(owner: Activity, lrn: String): Maybe<Student> {
        // Checks if LRN is valid
        if (lrn.length != 12) return Maybe.empty()

        return firestore.getStudentList(owner, cacheSource.getUserCache()!!.section)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMapMaybe { studentList ->
                Log.d("Threading", "analyze lrn1 ${Thread.currentThread().name}")
                Maybe.create<Student> { emitter ->
                    val lrnList = studentList.map { it.lrn }
                    if (lrn in lrnList) {
                        emitter.onSuccess(studentList[lrnList.indexOf(lrn)])
                    } else emitter.onComplete()
                    Log.d("Threading", "analyze lrn2 ${Thread.currentThread().name}")
                }
            }
    }

//    companion object {
//        private var M_INSTANCE: ScanViewModel? = null
//        fun getInstance(owner: Fragment): ScanViewModel {
//            M_INSTANCE?.let {
//                return it
//            }
//            M_INSTANCE = ScanViewModel(owner)
//            return getInstance(owner)
//        }
//    }


    fun requestPermission(fragment: Fragment, calledFromOnStart: Boolean) {
        Dexter.withActivity(fragment.activity!!)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(SnorlaxPermissionListener(fragment, calledFromOnStart))
            .onSameThread()
            .check()
    }

    fun addAttendance(owner: Activity, attendanceList: List<Attendance>): Completable {
        return firestore.addAttendance(owner, getCurrentSection(), attendanceList)
    }

//    fun getStudentRef(context: Context, student: Student): Single<DocumentReference> {
//        return cacheSource.getUserCache(context).flatMapSingle {
//            firestore.getStudentByLrn(it.section, student)
//        }
//    }

    fun isAutoTimeEnabled(): Boolean {
        val autoTime = Settings.Global.getInt(
            getApplication<BeabotApp>().contentResolver,
            Settings.Global.AUTO_TIME, 0
        )
        return autoTime == 1
    }

    fun isAutoTimeZoneEnabled(): Boolean {
        val autoTimeZone = Settings.Global.getInt(
            getApplication<BeabotApp>().contentResolver,
            Settings.Global.AUTO_TIME_ZONE, 0
        )
        return autoTimeZone == 1
    }

    fun getCurrentTime(): Date {

        return Calendar.getInstance().time
    }

    private fun getCurrentSection(): String {
        return cacheSource.getUserCache()!!.section
    }

//    fun getStudent(lrn: String): Student {
//        val studentObservable = Single.create<Student> { emitter ->
//            firestore.getDocumentReference(getCurrentSection(), lrn).get().addOnSuccessListener {
//                val student = it.toObject(Student::class.java)
//                    ?: throw NullPointerException("No student found with the given LRN")
//                emitter.onSuccess(student)
//            }.addOnFailureListener {
//                emitter.onError(it)
//            }
//        }
//
//        return studentObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).blockingGet()
//    }



    private inner class SnorlaxPermissionListener(
        private val fragment: Fragment,
        private val fromOnStart: Boolean
    ) :
        BasePermissionListener() {

        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
            permissionObservable.onNext(true)
        }

        override fun onPermissionRationaleShouldBeShown(
            permission: PermissionRequest?,
            token: PermissionToken?
        ) {
            token!!.continuePermissionRequest()
        }

        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
            if (response!!.isPermanentlyDenied) {
                if (!fromOnStart) {
                    Snackbar.make(
                        fragment.requireView(),
                        fragment.getString(R.string.msg_allow_permission_in_settings),
                        8000
                    )
                        .setAction(R.string.label_settings) {
                            val context: Context = it.context
                            val snorlaxSettings = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.packageName)
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                addCategory(Intent.CATEGORY_DEFAULT)
                            }
                            context.startActivity(snorlaxSettings)
                        }
                        .setActionTextColor(
                            ContextCompat.getColor(
                                fragment.context!!,
                                R.color.alertColor
                            )
                        )
                        .show()
                    permissionObservable.onNext(false)
                } else permissionObservable.onNext(false)
            } else permissionObservable.onNext(false)

        }
    }
}

