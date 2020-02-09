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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentActionListener
import com.snorlax.snorlax.utils.caps
import com.snorlax.snorlax.utils.exception.StudentAlreadyExistException
import com.snorlax.snorlax.utils.toPx
import com.snorlax.snorlax.viewmodel.StudentsViewModel
import com.snorlax.snorlax.views.ShimmerListProgress
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_add_student.*
import kotlinx.android.synthetic.main.diag_delete_student.*
import kotlinx.android.synthetic.main.fragment_students.*
import kotlinx.android.synthetic.main.fragment_students.view.*


/**
 * A simple [Fragment] subclass.
 */

class StudentsFragment : Fragment() {

    private lateinit var viewModel: StudentsViewModel

    private val disposables = CompositeDisposable()
    private val addDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StudentsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_students, container, false)
        val frame = rootView.student_list_container

        val shimmerView = ShimmerListProgress(requireContext()).apply {
            setLayoutChild(R.layout.shimmer_layout_student)
        }

        frame.addView(shimmerView)

        val recyclerView = RecyclerView(requireContext()).apply {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(16.toPx())
            this.layoutManager = layoutManager
            this.adapter = adapter
        }

        val emptyView =
            inflater.inflate(R.layout.layout_empty_list, frame, false).apply {
                val label = findViewById<TextView>(R.id.label_empty)
                val resolution =
                    if (viewModel.canAddStudent()) getString(R.string.msg_reso_add_students) else context.getString(
                        R.string.rmsg_reso_ask_teacher
                    )
                label.text = getString(R.string.msg_empty_student_list, resolution)
            }

        val studentActionListener = object : StudentActionListener() {
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
                            editDisposable.add(Completable.create { emitter ->
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
                                }))
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

                alertDialog.setOnDismissListener {
                    editDisposable.clear()
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
                    .setPositiveButton(R.string.btn_ok, null)
                    .setNegativeButton(android.R.string.no, null)
                    .create()

                deleteDialog.setOnShowListener { dialog ->
                    val passwordLayout = deleteDialog.text_layout_input_reauth_password
                    val passwordInput = deleteDialog.input_reauth_password
                    val loadingView = deleteDialog.reauth_password_loading

                    val positiveButton = deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    val passwordDisposable = passwordInput.textChanges().subscribe {
                        positiveButton.isEnabled = it.length >= 6
                    }
                    deleteDisposable.add(passwordDisposable)


                    positiveButton.setOnClickListener {
                        passwordLayout.error = null

                        loadingView.visibility = View.VISIBLE
                        val disposable = viewModel.reAuth(passwordInput.text.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                loadingView.visibility = View.GONE
                            }.andThen(viewModel.deleteStudent(student))
                            .subscribeBy(
                                onComplete = {
                                    dialog.dismiss()
                                    Snackbar.make(
                                        requireView(),
                                        getString(R.string.msg_student_deleted),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                },
                                onError = { error ->
                                    when (error) {
                                        is FirebaseAuthInvalidCredentialsException -> {
                                            loadingView.visibility = View.GONE
                                            if (error.errorCode == "ERROR_WRONG_PASSWORD") {
                                                passwordLayout.error =
                                                    getString(R.string.err_incorrect_password)
                                            } else passwordLayout.error = error.localizedMessage
                                        }
                                        is FirebaseTooManyRequestsException -> {
                                            loadingView.visibility = View.GONE
                                            passwordLayout.error = error.localizedMessage
                                        }
                                        else -> {
                                            dialog.dismiss()
                                            Snackbar.make(
                                                requireView(),
                                                getString(
                                                    R.string.err_unknown,
                                                    error.localizedMessage
                                                ), Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )
                        deleteDisposable.add(disposable)
                    }

                }
                deleteDialog.setCanceledOnTouchOutside(false)
                deleteDialog.setOnDismissListener {
                    deleteDisposable.clear()
                }
                deleteDialog.show()
            }
        }

        val recyclerOptions: FirestoreRecyclerOptions<Student> =
            FirestoreRecyclerOptions.Builder<Student>()
                .setLifecycleOwner(this)
                .setQuery(viewModel.getStudentQuery(), Student::class.java)
                .build()

        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter =
            StudentListAdaptor(
                activity!!,
                !viewModel.canAddStudent(),
                recyclerOptions,
                studentActionListener
            ) {
                if (it > 0) {
                    if (frame.indexOfChild(recyclerView) == -1) {
                        frame.removeAllViews()
                        frame.addView(recyclerView)
                    }
                } else {
                    if (frame.indexOfChild(emptyView) == -1) {
                        frame.removeAllViews()
                        frame.addView(emptyView)
                    }
                }

            }



        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.canAddStudent()) btn_add_student.setOnClickListener { showAddStudentDialog() }
        else btn_add_student.visibility = View.GONE
    }

    private fun getSpannedText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(text)
        }
    }

    private fun showAddStudentDialog() {
        val addStudentAlertDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        ).apply {
            setView(R.layout.diag_add_student)
            setPositiveButton("Add", null)
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
                        button.isEnabled = false

                        val disposable =
                            viewModel.studentExist(lrnInput.text.toString()).flatMapCompletable {
                                if (it) {
                                    Completable.error(StudentAlreadyExistException())
                                } else {
                                    viewModel.addStudent(
                                        Student(
                                            mapOf(
                                                Student.FIRST_NAME_VAL to firstNameInput.text.toString().trim().caps(),
                                                Student.LAST_NAME_VAL to lastNameInput.text.toString().trim().caps()
                                            ), lrnInput.text.toString().trim()
                                        )
                                    )
                                }
                            }.subscribeBy(
                                onError = { error ->
                                    if (error is StudentAlreadyExistException) {
                                        lrnLayout.error = "Student already exist"
                                        button.isEnabled = true
                                    } else {
                                        dialog.dismiss()
                                        Snackbar.make(
                                            requireView(),
                                            error.localizedMessage!!,
                                            Snackbar.LENGTH_LONG
                                        ).show()

                                    }
                                },
                                onComplete = {
                                    dialog.dismiss()
                                    Snackbar.make(
                                        requireView(),
                                        "Student added",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        addDisposable.add(disposable)
                    } else {
                        if (firstNameInput.text.isNullOrBlank()) {
                            firstNameLayout.error = "Please enter a valid first name"
                        }
                        if (lastNameInput.text.isNullOrBlank()) {
                            lastNameLayout.error = "Please enter a valid first name"
                        }
                        if (lrnInput.text!!.length != 12 || !lrnInput.text.toString().isDigitsOnly()) {
                            lrnLayout.error = "Please enter a valid LRN"
                        }
                    }
                }
        }

        addStudentAlertDialog.setCanceledOnTouchOutside(false)
        addStudentAlertDialog.setOnDismissListener {
            addDisposable.clear()
        }
        addStudentAlertDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
