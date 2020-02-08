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


import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.utils.adapter.recyclerview.AttendanceAdaptor
import com.snorlax.snorlax.views.ShimmerListProgress
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_attendance_list.view.*
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 */
class AttendanceListFragment(private val attendance: Observable<List<Attendance>>) : Fragment() {

//    private lateinit var viewModel: AttendanceViewModel

    private val disposables = CompositeDisposable()

    private fun Int.toPx(): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).roundToInt()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        viewModel = ViewModelProviders.of(parentFragment!!)[AttendanceViewModel::class.java]
//    }

    // TODO show an empty layout when there is no data
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        rootView.attendance_list.layoutManager = LinearLayoutManager(requireContext())
//        rootView.attendance_list.adapter = adapter

//        val firebase = viewModel.selectedTimeObservable
//            .flatMap { viewModel.getAttendance(Date(it)) }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                (rootView.attendance_list.adapter as AttendanceAdaptor).updateData(it)
////                rootView.attendance_pager.currentItem = timeToPosition(it)
//            }
        // todo Add shimmer
        val rootView = inflater.inflate(R.layout.fragment_attendance_list, container, false)

        val adapter = AttendanceAdaptor()
        val layoutManager = LinearLayoutManager(requireContext())
        val frame = rootView.attendance_frame

//        val emptyListView =
//            requireActivity().layoutInflater.inflate(R.layout.layout_empty_list, frame)


        val loadingView = ShimmerListProgress(requireContext()).apply {
            //            repeat(10) {
//                addView(TextView(requireContext()).apply {
//                    text = "Loading"
//                })
//            }
//            addView(TextView(requireContext()).apply {
//                text = "Loading"
//            })
            setLayoutChild(R.layout.shimmer_layout_attendance)
        }

        frame.addView(loadingView)

        val recyclerView = RecyclerView(requireContext()).apply {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(16.toPx())
            this.layoutManager = layoutManager
            this.adapter = adapter
        }

        val emptyView =
            inflater.inflate(R.layout.layout_empty_list, frame, false)

        val attendanceDisposable = attendance
            .subscribeOn(Schedulers.io())
            .subscribe {

                if (it.isEmpty()) {
                    // todo create empty placeholder
                    if (frame.indexOfChild(emptyView) == -1) {
                        frame.removeAllViews()
                        frame.addView(emptyView, 0)
                    }
                } else {
                    // todo create recycler view
                    if (frame.indexOfChild(recyclerView) == -1) {
                        frame.removeAllViews()
                        frame.addView(recyclerView, 0)
                    }
                    adapter.updateData(it)
                }

            }

        disposables.add(attendanceDisposable)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        val adapter = AttendanceAdaptor()
//        val layoutManager = LinearLayoutManager(requireContext())
//        val frame = requireView().attendance_frame
//
////        val emptyListView =
////            requireActivity().layoutInflater.inflate(R.layout.layout_empty_list, frame)
//
//        val recyclerView = RecyclerView(requireContext()).apply {
//            isVerticalFadingEdgeEnabled = true
//            setFadingEdgeLength(16.toPx())
//            this.layoutManager = layoutManager
//            this.adapter = adapter
//        }
//
//        val attendanceDisposable = attendance
//            .subscribeOn(Schedulers.io())
//            .subscribe {
//
//                if (it.isEmpty()) {
//                    // todo create empty placeholder
//
//                    val emptyListView =
//                        requireActivity().layoutInflater.inflate(R.layout.layout_empty_list, frame)
//                    if (frame.indexOfChild(emptyListView) == -1) {
//                        frame.removeAllViews()
////                        if (emptyListView.parent != null) {
////                            (emptyListView.parent as ViewGroup).removeView(emptyListView)
////                        }
//                        frame.addView(emptyListView, 0)
//                    }
//                } else {
//                    // todo create recycler view
//                    if (frame.indexOfChild(recyclerView) == -1) {
//                        frame.removeAllViews()
//                        frame.addView(recyclerView, 0)
//                    }
//                    adapter.updateData(it)
//                }
////            if (it.isEmpty()) {
////                rootView.attendance_list.visibility = View.GONE
//////                rootView.label_empty.visibility = View.VISIBLE
////            } else {
////                rootView.attendance_list.visibility = View.VISIBLE
////                rootView.label_empty.visibility = View.GONE
////            }
//            }
//
//        disposables.add(attendanceDisposable)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

}
