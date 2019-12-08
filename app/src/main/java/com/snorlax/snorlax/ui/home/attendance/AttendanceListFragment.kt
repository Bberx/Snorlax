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


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.utils.adapter.recyclerview.AttendanceAdaptor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_attendance_list.view.*

/**
 * A simple [Fragment] subclass.
 */
class AttendanceListFragment(private val attendance: Observable<List<Attendance>>) : Fragment() {

//    private lateinit var viewModel: AttendanceViewModel

    private val disposables = CompositeDisposable()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        viewModel = ViewModelProviders.of(parentFragment!!)[AttendanceViewModel::class.java]
//    }

    // TODO show an empty layout when there is no data
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_attendance_list, container, false)

        val adapter = AttendanceAdaptor()

        rootView.attendance_list.layoutManager = LinearLayoutManager(requireContext())
        rootView.attendance_list.adapter = adapter

//        val firebase = viewModel.selectedTimeObservable
//            .flatMap { viewModel.getAttendance(Date(it)) }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                (rootView.attendance_list.adapter as AttendanceAdaptor).updateData(it)
////                rootView.attendance_pager.currentItem = timeToPosition(it)
//            }

        val attendanceDisposable = attendance
            .subscribeOn(Schedulers.io())
            .subscribe {
                adapter.updateData(it)
//            if (it.isEmpty()) {
//                rootView.attendance_list.visibility = View.GONE
////                rootView.label_empty.visibility = View.VISIBLE
//            } else {
//                rootView.attendance_list.visibility = View.VISIBLE
//                rootView.label_empty.visibility = View.GONE
//            }
            }

        disposables.add(attendanceDisposable)



        return rootView
    }

//    fun updateData(attendance: Observable<List<Attendance>>) {
//
//        val attendanceDisposable = attendance.subscribe {
//            (attendance_list.adapter as AttendanceAdaptor).updateData(it)
//        }
//
//        disposables.add(attendanceDisposable)
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

}
