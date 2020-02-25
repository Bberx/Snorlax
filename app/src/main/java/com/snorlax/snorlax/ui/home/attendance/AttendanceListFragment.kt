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
import android.widget.LinearLayout
import androidx.core.content.getSystemService
import androidx.core.view.contains
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.firebase.getStudent
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.model.ResolvedAttendance
import com.snorlax.snorlax.utils.adapter.recyclerview.AttendanceAdaptor
import com.snorlax.snorlax.utils.fadeIn
import com.snorlax.snorlax.utils.fadeOut
import com.snorlax.snorlax.utils.toPx
import com.snorlax.snorlax.viewmodel.AttendanceViewModel
import com.snorlax.snorlax.views.ShimmerListProgress
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_attendance_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class AttendanceListFragment(private val attendance: Observable<List<Attendance>>) : Fragment() {

    private val disposables = CompositeDisposable()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        viewModel = ViewModelProviders.of(parentFragment!!)[AttendanceViewModel::class.java]
//    }

    private lateinit var viewModel: AttendanceViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var adapter: AttendanceAdaptor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireParentFragment())[AttendanceViewModel::class.java]
        super.onActivityCreated(savedInstanceState)

        val frame = attendance_frame
        val loadingView = ShimmerListProgress(requireContext()).apply {
            setLayoutChild(R.layout.shimmer_layout_attendance)
        }

        frame.addView(loadingView)

        adapter = AttendanceAdaptor(requireContext())
        val layoutManager = LinearLayoutManager(requireContext())
        val inflater = requireContext().getSystemService<LayoutInflater>()!!
        recyclerView = RecyclerView(requireContext()).apply {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(16.toPx())
            this.layoutManager = layoutManager
            this.adapter = this@AttendanceListFragment.adapter
        }

        emptyView =
            inflater.inflate(R.layout.layout_empty_list, attendance_frame, false) as LinearLayout

    }

    private fun <A, B> List<A>.pmap(f: suspend (A) -> B): List<B> = runBlocking {
        map { async(Dispatchers.Default) { f(it) } }.map { it.await() }
    }

    private fun attachSubscriber() {
        val frame = attendance_frame
        val attendanceDisposable = attendance
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { rawList ->

                //                Log.d("test", "dd")
                lifecycleScope.launchWhenResumed {
                    val list = rawList.map {
                        ResolvedAttendance(it, it.student.getStudent())
                    }

                    if (list.isEmpty()) {
                        if (!frame.contains(emptyView)) {
                            if (frame.childCount != 0) {
                                frame[0].fadeOut()
                                frame.removeAllViews()
                            }
                            frame.addView(emptyView, 0)
                            emptyView.fadeIn()
                        }
                    } else {
                        if (!frame.contains(recyclerView)) {
                            if (frame.childCount != 0) {
                                frame[0].fadeOut()
                                frame.removeAllViews()
                            }
                            frame.addView(recyclerView, 0)
                            recyclerView.fadeIn()
                        }
                        adapter.submitList(list)
                    }
                }
            }, onError = {
                view?.let { view ->
                    Snackbar.make(
                        view,
                        it.localizedMessage ?: "Unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
        disposables += attendanceDisposable
    }

    override fun onResume() {
        super.onResume()
        attachSubscriber()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}
