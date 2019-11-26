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


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.adapter.recyclerview.AttendanceAdaptor
import com.snorlax.snorlax.utils.getTodayDate
import com.snorlax.snorlax.viewmodel.AttendanceViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_attendance.*
import kotlinx.android.synthetic.main.fragment_attendance.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class AttendanceFragment : Fragment() {

    private val viewModel = AttendanceViewModel.getInstance()

    private val disposables = CompositeDisposable()

    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select which day…")
        .setCalendarConstraints(viewModel.bounds)
        .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        .build()


//    private val adapter = AttendanceAdaptor(this, SortedList(Attendance::class.java, callback))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val rootView = inflater.inflate(R.layout.fragment_attendance, container, false)

        rootView.picker_container.setOnClickListener { showDatePickerDialog() }
        rootView.attendance_list.layoutManager = LinearLayoutManager(context!!)

        datePicker.addOnPositiveButtonClickListener {
            viewModel.selectedTimeObservable.onNext(it)
        }

        rootView.attendance_list.adapter = AttendanceAdaptor()

        rootView.label_relative_time.text = viewModel.getRelativeDateString(getTodayDate().time)
        rootView.label_date.text = dateFormat.format(getTodayDate())

        val relativeTime = viewModel.selectedTimeObservable
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                rootView.label_relative_time.text = viewModel.getRelativeDateString(it)
                rootView.label_date.text = dateFormat.format(Date(it))
            }


        val firebase = viewModel.selectedTimeObservable
            .flatMap { viewModel.getAttendance(context!!, Date(it)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (attendance_list.adapter as AttendanceAdaptor).updateData(it) }

        disposables.addAll(firebase, relativeTime)

        return rootView

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.selectedTimeObservable.onNext(getTodayDate().time)
    }

    private fun showDatePickerDialog() {
        datePicker.show(activity!!.supportFragmentManager, "DatePicker")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

}
