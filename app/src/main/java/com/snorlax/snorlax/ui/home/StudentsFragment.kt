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


import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthException
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentActionListener
import com.snorlax.snorlax.utils.caps
import com.snorlax.snorlax.viewmodel.StudentsViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_add_student.*
import kotlinx.android.synthetic.main.diag_delete_student.*
import kotlinx.android.synthetic.main.fragment_students.*


/**
 * A simple [Fragment] subclass.
 */

class StudentsFragment : Fragment(), StudentActionListener {

    // TODO get section of current user

    private lateinit var viewModel: StudentsViewModel

    private val disposables = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[StudentsViewModel::class.java]
    }

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


        if (viewModel.canAddStudent()) {
            btn_add_student.setOnClickListener {
                showAddStudentDialog()
            }
            //            layout_reveal.setLockDrag(false)

        } else {
            btn_add_student.visibility = View.GONE
        }


        disposables.add(
            viewModel.getStudentQuery()
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
                        !viewModel.canAddStudent(),
                        recyclerOptions,
                        this
                    )
                }, {
                    Toast.makeText(context!!, it.localizedMessage, Toast.LENGTH_LONG).show()
                })
        )
    }

    private fun getSpannedText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(text)
        }
    }

    override fun deleteStudent(student: Student) {
        val deleteDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.title_delete_student))
            .setIcon(R.drawable.ic_delete)
            .setMessage(
                getSpannedText(
                    getString(
                        R.string.msg_delete_student,
                        "${student.name[Student.FIRST_NAME_VAL]} ${student.name[Student.LAST_NAME_VAL]}"
                    )
                )
            )
            .setView(R.layout.diag_delete_student)
//            .setPositiveButton(android.R.string.yes) { dialogInterface, _ ->
//
//                val alert = (dialogInterface as AlertDialog)
//
//
//                viewModel.deleteStudent(student)
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({
//                        Snackbar.make(
//                            view!!,
//                            "Student deleted",
//                            Snackbar.LENGTH_SHORT
//                        )
//                            .show()
//                    }, {
//                        Snackbar.make(
//                            view!!,
//                            it.localizedMessage!!,
//                            Snackbar.LENGTH_SHORT
//                        )
//                            .show()
//                    })
//            }
            .setPositiveButton(R.string.btn_ok, null)
            .setNegativeButton(android.R.string.no, null)
            .create()

        deleteDialog.setOnShowListener { dialog ->

            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val textLayout = deleteDialog.text_layout_input_reauth_password
                val loadingView = deleteDialog.reauth_password_loading

                textLayout.error = null
                loadingView.visibility = View.VISIBLE

                viewModel.reauth(deleteDialog.input_reauth_password.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        loadingView.visibility = View.GONE
                        viewModel.deleteStudent(student)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                dialog.dismiss()
                                Snackbar.make(
                                    view!!,
                                    "Student deleted",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }, {
                                dialog.dismiss()
                                Snackbar.make(
                                    view!!,
                                    "${it.localizedMessage} Please try again",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            })
                    }, {
                        loadingView.visibility = View.GONE
                        if (it is FirebaseAuthException) {
                            it.let {
                                Log.d("ErrorCode", it.errorCode)
                            }
                        }
//                        if (it is IllegalArgumentException) {
//                            textLayout.error = getString(R.string.err_msg_not_valid_email)
//                        } else {
                        textLayout.error = it.localizedMessage!!
//                        }
                    })
            }

        }

        deleteDialog.setCanceledOnTouchOutside(false)
        deleteDialog.show()
    }

    override fun editStudent(
        position: Int,
        student: Student,
        options: FirestoreRecyclerOptions<Student>
    ) {
        val alertDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setTitle("Edit student")
            .setIcon(R.drawable.ic_edit)
            .setView(R.layout.diag_add_student)
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->

                val alert = (dialogInterface as AlertDialog)

                if (alert.input_first_name.text!!.isNotEmpty() &&
                    alert.input_last_name.text!!.isNotEmpty() &&
                    alert.input_lrn.text!!.isNotEmpty()
                ) {
                    Completable.create { emitter ->
                        options.snapshots.getSnapshot(position).reference.set(
                            Student(
                                mapOf(
                                    Student.FIRST_NAME_VAL to alert.input_first_name.text.toString().trim(),
                                    Student.LAST_NAME_VAL to alert.input_last_name.text.toString().trim()
                                ), alert.input_lrn.text.toString().trim()
                            )
                        ).addOnSuccessListener {
                            emitter.onComplete()
                        }.addOnFailureListener {
                            emitter.onError(it)
                        }
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Snackbar.make(
                                view!!,
                                "Student edited",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }, {
                            Snackbar.make(
                                view!!,
                                it.localizedMessage!!,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        })
                }

            }
            .setNegativeButton(android.R.string.no, null)
            .create()

        alertDialog.show()
        alertDialog.input_first_name.setText(student.name[Student.FIRST_NAME_VAL])
        alertDialog.input_last_name.setText(student.name[Student.LAST_NAME_VAL])
        alertDialog.input_lrn.setText(student.lrn)
        alertDialog.input_lrn.isEnabled = false
        alertDialog.text_layout_lrn.isEnabled = false


    }

    private fun showAddStudentDialog() {
        val addStudentAlertDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        ).apply {
            setView(R.layout.diag_add_student)
            setPositiveButton("Add", null)
//            { dialog, _ ->
//                val alertDialog = (dialog as AlertDialog)
//
//                // TODO MVVM conflict
//                if (alertDialog.input_first_name.text!!.isNotEmpty() &&
//                    alertDialog.input_last_name.text!!.isNotEmpty() &&
//                    alertDialog.input_lrn.text!!.isNotEmpty()
//                ) {
//                    if (alertDialog.input_lrn.text!!.length != 12) {
//                        Toast.makeText(activity, "Please enter a valid LRN", Toast.LENGTH_LONG)
//                            .show()
//                    } else {
//                        viewModel.addStudent(
//                            Student(
//                                mapOf(
//                                    "first" to alertDialog.input_first_name.text.toString().trim(),
//                                    "last" to alertDialog.input_last_name.text.toString().trim()
//                                ), alertDialog.input_lrn.text.toString()
//                            )
//                        ).subscribe({
//                            view?.let {
//                                Snackbar.make(it, "Student added", Snackbar.LENGTH_SHORT).show()
//                            }
//
//                        }, { error ->
//                            view?.let {
//                                Snackbar.make(it, error.localizedMessage!!, Snackbar.LENGTH_LONG)
//                                    .show()
//                            }
//                        })
//                    }
//                }
//            }
            setNegativeButton("Cancel", null)
            setTitle("Add Student")
            setIcon(R.drawable.ic_students)
        }.create()

        addStudentAlertDialog.setOnShowListener { dialog ->

            addStudentAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener { button ->

                    val lrnLayout = addStudentAlertDialog.text_layout_lrn
                    val firstNameLayout = addStudentAlertDialog.text_layout_first_name
                    val lastNameLayout = addStudentAlertDialog.text_layout_last_name

                    val lrnInput = addStudentAlertDialog.input_lrn
                    val firstNameInput = addStudentAlertDialog.input_first_name
                    val lastNameInput = addStudentAlertDialog.input_last_name

                    lrnLayout.error = null
                    firstNameLayout.error = null
                    lastNameLayout.error = null

                    if (firstNameInput.text!!.isNotEmpty() &&
                        lastNameInput.text!!.isNotEmpty() &&
                        lrnInput.text!!.length == 12 && lrnInput.text.toString().isDigitsOnly()
                ) {
//                    if (lrnInput.text!!.length != 12 || !lrnInput.text.toString().isDigitsOnly()) {
////                        Toast.makeText(activity, "Please enter a valid LRN", Toast.LENGTH_LONG)
////                            .show()
//                        lrnLayout.error = "Please enter a valid LRN"
//                    } else {
                        (button as Button).isEnabled = false
                        viewModel.addStudent(
                            Student(
                                mapOf(
                                    Student.FIRST_NAME_VAL to firstNameInput.text.toString().trim().caps(),
                                    Student.LAST_NAME_VAL to lastNameInput.text.toString().trim().caps()
                                ), lrnInput.text.toString()
                            )
                        ).subscribe({
                            dialog.dismiss()
                            view?.let {
                                Snackbar.make(it, "Student added", Snackbar.LENGTH_SHORT).show()
                            }

                        }, { error ->
                            dialog.dismiss()
                            view?.let {
                                Snackbar.make(it, error.localizedMessage!!, Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        })
//                    }
                    } else {
                        if (firstNameInput.text.isNullOrBlank()) {
                            firstNameLayout.error = "Please enter a valid first name"
                        }
                        if (lastNameInput.text.isNullOrBlank()) {
                            lastNameLayout.error = "Please enter a valid first name"
                    }
                        if (lrnInput.text!!.length != 12 || !lrnInput.text.toString().isDigitsOnly()) {
//                        Toast.makeText(activity, "Please enter a valid LRN", Toast.LENGTH_LONG)
//                            .show()
                            lrnLayout.error = "Please enter a valid LRN"
                        }
                    }
                }
        }

        addStudentAlertDialog.setCanceledOnTouchOutside(false)
        addStudentAlertDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
