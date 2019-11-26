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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.viewmodel.StudentsViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_add_student.*
import kotlinx.android.synthetic.main.fragment_students.*


/**
 * A simple [Fragment] subclass.
 */
class StudentsFragment : Fragment() {

    // TODO get section of current user

    private val studentsViewModel = StudentsViewModel()

    private val disposables = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_students, container, false)


        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        if (studentsViewModel.canAddStudent(context!!)) {
            btn_add_student.setOnClickListener {
                showAddStudentDialog()
            }
            //            layout_reveal.setLockDrag(false)

        } else {
            btn_add_student.visibility = View.GONE
        }


        disposables.add(
            studentsViewModel.getStudentQuery(context!!)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val recyclerOptions: FirestoreRecyclerOptions<Student> =
                        FirestoreRecyclerOptions.Builder<Student>()
                            .setLifecycleOwner(this)
                            .setQuery(it, Student::class.java)
                            .build()

                    students_list.layoutManager = LinearLayoutManager(context!!)
                    students_list.adapter = StudentListAdaptor(
                        activity!!,
                        !studentsViewModel.canAddStudent(context!!),
                        recyclerOptions
                    )
                }, {
                    Toast.makeText(context!!, it.localizedMessage, Toast.LENGTH_LONG).show()
                })
        )
    }

    private fun showAddStudentDialog() {
        val addStudentAlertDialog = MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).apply {
            setView(R.layout.diag_add_student)
            setPositiveButton("Add") { dialog, _ ->
                val alertDialog = (dialog as AlertDialog)

                // TODO MVVM conflict
                if (alertDialog.input_first_name.text!!.isNotEmpty() &&
                    alertDialog.input_last_name.text!!.isNotEmpty() &&
                    alertDialog.input_lrn.text!! .isNotEmpty()
                ) {
                    if (alertDialog.input_lrn.text!!.length != 12) {
                        Toast.makeText(activity, "Please enter a valid LRN", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        studentsViewModel.addStudent(context,
                            Student(
                                mapOf(
                                    "first" to alertDialog.input_first_name.text.toString().trim(),
                                    "last" to alertDialog.input_last_name.text.toString().trim()
                                ), alertDialog.input_lrn.text.toString()
                            )
                        ).subscribe({
                            view?.let {
                                Snackbar.make(it, "Student added", Snackbar.LENGTH_SHORT).show()
                            }

                        }, { error ->
                            view?.let {
                                Snackbar.make(it, error.localizedMessage!!, Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        })
                    }
                }
            }
            setNegativeButton("Cancel", null)
            setTitle("Add Student")
            setIcon(R.drawable.ic_students)
        }.create()

        addStudentAlertDialog.setCanceledOnTouchOutside(false)
        addStudentAlertDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
