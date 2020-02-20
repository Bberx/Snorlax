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

package com.snorlax.snorlax.ui.home.attendance


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.Constants
import com.snorlax.snorlax.utils.TimeUtils.getMonthDate
import com.snorlax.snorlax.utils.TimeUtils.getTodayDateUTC
import com.snorlax.snorlax.utils.TimeUtils.positionToTime
import com.snorlax.snorlax.utils.TimeUtils.timeToPosition
import com.snorlax.snorlax.utils.adapter.viewpager.AttendancePageAdapter
import com.snorlax.snorlax.utils.exception.TemplateNotFoundException
import com.snorlax.snorlax.viewmodel.AttendanceViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_attendance.*
import kotlinx.android.synthetic.main.fragment_attendance.view.*
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class AttendanceFragment : Fragment() {

    companion object {

        private const val TAG = "AttendanceFragment"
        private const val REQUEST_CREATE_DOCX = 5
    }

    private lateinit var viewModel: AttendanceViewModel

    private val disposables = CompositeDisposable()

    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private val fileNameDateFormat = SimpleDateFormat("MMMM-yyyy", Locale.getDefault())

//    private val datePicker: MaterialDatePicker<Long> by lazy {
//        MaterialDatePicker.Builder.datePicker()
//            .setTitleText(R.string.label_which_day)
//            .setCalendarConstraints(
//                CalendarConstraints.Builder()
//                    .setOpenAt(getTodayDateUTC().time)
//                    .setStart(0)
//                    .setEnd(getTodayDateUTC().time)
//                    .setValidator(object : CalendarConstraints.DateValidator {
//                        var date: Long = 0
//                        override fun isValid(date: Long): Boolean {
//                            this.date = date
//                            return date <= getTodayDateUTC().time && date >= 0
//                        }
//
//                        override fun writeToParcel(dest: Parcel?, flags: Int) {
//                            dest?.writeInt(if (date <= getTodayDateUTC().time) 1 else 0)
//                        }
//
//                        override fun describeContents() = 0
//                    })
//                    .build()
//            )
//            .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
//            .setSelection(viewModel.selectedTimeObservable.value)
//            .build()
//    }


//    private val adapter = AttendanceAdaptor(this, SortedList(Attendance::class.java, callback))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this)[AttendanceViewModel::class.java]
        } ?: throw IllegalStateException("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_attendance, container, false)

        rootView.picker_container.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_which_day)
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setOpenAt(viewModel.selectedTimeObservable.value ?: getTodayDateUTC().time)
                        .setStart(0)
                        .setEnd(getTodayDateUTC().time)
                        .setValidator(object : CalendarConstraints.DateValidator {
                            var date: Long = 0
                            override fun isValid(date: Long): Boolean {
                                this.date = date
                                return date <= getTodayDateUTC().time && date >= 0
                            }

                            override fun writeToParcel(dest: Parcel, flags: Int) {
                                dest.writeInt(if (date <= getTodayDateUTC().time) 1 else 0)
                            }

                            override fun describeContents() = 0
                        })
                        .build()
                )
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .setSelection(viewModel.selectedTimeObservable.value)
                .build()


            picker.showNow(childFragmentManager, "DatePicker")
            picker.addOnPositiveButtonClickListener { viewModel.selectedTimeObservable.onNext(it) }

//            showDatePickerDialog()
        }

//        rootView.attendance_list.layoutManager = LinearLayoutManager(context!!)


        rootView.btn_export.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                val selectedDate = positionToTime(rootView.attendance_pager.currentItem)
                val fileName =
                    "Attendance-${Constants.SECTION_LIST.getValue(viewModel.section).display_name}-${fileNameDateFormat.format(
                        selectedDate
                    )}.docx"

                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                putExtra(Intent.EXTRA_TITLE, fileName)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }
            startActivityForResult(intent, REQUEST_CREATE_DOCX)
        }

//        rootView.attendance_list.adapter = AttendanceAdaptor()

        rootView.label_relative_time.text = viewModel.getRelativeDateString(getTodayDateUTC())
        rootView.label_date.text = dateFormat.format(getTodayDateUTC())

        rootView.attendance_pager.adapter = AttendancePageAdapter(this) {
            viewModel.getAttendance(it)
        }

        rootView.attendance_pager.offscreenPageLimit = 1




        rootView.attendance_pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            var previousItem = rootView.attendance_pager.currentItem
            override fun onPageSelected(position: Int) {
                if (previousItem != position) {
                    viewModel.selectedTimeObservable.onNext(positionToTime(position).time)
                    previousItem = position
                }
            }

        })

        var smoothScroll = false
        val relativeTime = viewModel.selectedTimeObservable
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                rootView.attendance_pager.setCurrentItem(timeToPosition(it), smoothScroll)
                rootView.label_relative_time.text = viewModel.getRelativeDateString(Date(it))
                rootView.label_date.text = dateFormat.format(Date(it))
                smoothScroll = true
            }

//        val today = getTodayDateUTC().time
//        rootView.attendance_pager.setCurrentItem(timeToPosition(today), false)
//        rootView.label_relative_time.text =
//            viewModel.getRelativeDateString(Date(today))
//        rootView.label_date.text = dateFormat.format(Date(today))


        disposables.addAll(relativeTime)

        return rootView

    }

    private fun saveAttendance(outputLocation: Uri) {
        val saveAttendance = viewModel.saveAttendance(
            outputLocation,
            getMonthDate(Date(viewModel.selectedTimeObservable.value!!))
        ).doOnDispose {
                viewModel.deleteFile(outputLocation)
                Log.d("Threading", "${Thread.currentThread().name} onDispose")
            }.doOnError {
                Log.d("Threading", "${Thread.currentThread().name} onError")
                viewModel.deleteFile(outputLocation)

            }.doOnComplete {
                Log.d("Threading", "${Thread.currentThread().name} onComplete")
            }
            .unsubscribeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                showSuccessResult(
                    getString(
                        R.string.msg_attendance_saved,
                        viewModel.outputFileName(outputLocation)
                    ), outputLocation
                )
            }, onError = {
                when (it) {
                    is TemplateNotFoundException -> showResult(getString(R.string.err_template_not_found))

                    is FileNotFoundException -> showResult(getString(R.string.err_file_not_found))

                    is IOException -> showResult(
                        getString(
                            R.string.err_io_exception,
                            it.localizedMessage ?: "unknown error occurred."
                        )
                    )

                    is NoSuchElementException -> showResult(getString(R.string.err_unknown_section))

                    else -> {
                        showResult(
                            getString(
                                R.string.err_unknown,
                                it.localizedMessage ?: "no error message."
                            ), TimeUnit.SECONDS.toMillis(3).toInt()
                        )
                        Log.d("FIXME", it.message)
                    }
                }
            })

        disposables.add(saveAttendance)

    }

    private fun showResult(message: String, length: Int = Snackbar.LENGTH_SHORT) {
        view?.let {
            Snackbar.make(it, message, length).show()
        }
    }

    private fun showSuccessResult(message: String, location: Uri) {
        view?.let {
            val snackbar = Snackbar.make(it, message, 3000)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    location,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                snackbar.setAction(getString(R.string.btn_open)) {
                    startActivity(Intent.createChooser(intent, "Open attendance with"))
                }.setActionTextColor(Color.YELLOW)
                    .show()
            } else {
                snackbar.show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CREATE_DOCX -> {
                if (resultCode == Activity.RESULT_OK) {
//                    btn_export.btn_export.setImageDrawable(loading)
                    data?.data?.let {
                        saveAttendance(it)
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d(TAG, "Canceled")
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.selectedTimeObservable.onNext(getTodayDateUTC().time)

//        attendance_pager.setCurrentItem(timeToPosition(getTodayDate().time), false)
    }

//    private fun showDatePickerDialog() {
//        if (!datePicker.isVisible) {
//            datePicker.showNow(childFragmentManager, "DatePicker")
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
