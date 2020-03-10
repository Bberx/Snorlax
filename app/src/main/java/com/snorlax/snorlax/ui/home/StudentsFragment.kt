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

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentEditListener
import com.snorlax.snorlax.utils.capitalizeWords
import com.snorlax.snorlax.utils.exception.StudentAlreadyExistException
import com.snorlax.snorlax.utils.getSpannedText
import com.snorlax.snorlax.utils.hideKeyboard
import com.snorlax.snorlax.viewmodel.StudentsViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_add_student.*
import kotlinx.android.synthetic.main.diag_delete_student.*
import kotlinx.android.synthetic.main.fragment_students.*

class StudentsFragment : BaseStudentFragment() {

    override lateinit var viewModel: StudentsViewModel
    override lateinit var adaptor: StudentListAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StudentsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_action.setImageResource(R.drawable.ic_person_add)

        val studentActionListener = object : StudentEditListener {
            override val editDisposable: CompositeDisposable = CompositeDisposable()
            override val deleteDisposable: CompositeDisposable = CompositeDisposable()

            @SuppressLint("PrivateResource")
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
                        hideKeyboard(requireView())

                        val alert = (dialogInterface as AlertDialog)

                        if (alert.input_first_name.text!!.isNotEmpty() &&
                            alert.input_last_name.text!!.isNotEmpty() &&
                            alert.input_lrn.text!!.isNotEmpty()
                        ) {
                            editDisposable.add(Completable.create { emitter ->
                                val index = options.snapshots.indexOf(student)
                                options.snapshots.getSnapshot(index).reference.set(
                                    Student(
                                        mapOf(
                                            Student.FIRST_NAME_VAL to alert.input_first_name.text.toString().trim().capitalizeWords(),
                                            Student.LAST_NAME_VAL to alert.input_last_name.text.toString().trim().capitalizeWords()
                                        ), alert.input_lrn.text.toString().trim()
                                    )
                                ).addOnSuccessListener {
                                    emitter.onComplete()
                                }.addOnFailureListener {
                                    emitter.onError(it)
                                }
                            }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally {
                                    alert.dismiss()
                                    editDisposable.clear()
                                    hideKeyboard(requireView())
                                }
                                .subscribe({
                                    Snackbar.make(
                                        view,
                                        "Student edited",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }, {
                                    Snackbar.make(
                                        view,
                                        it.localizedMessage!!,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }))
                        }
                        hideKeyboard(requireView())
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int ->
                        hideKeyboard(requireView(), view)
                    }
                    .create()

                alertDialog.show()
                alertDialog.input_first_name.setText(student.name[Student.FIRST_NAME_VAL])
                alertDialog.input_last_name.setText(student.name[Student.LAST_NAME_VAL])
                alertDialog.input_lrn.setText(student.lrn)
                alertDialog.input_lrn.isEnabled = false
                alertDialog.text_layout_lrn.isEnabled = false

                alertDialog.setOnDismissListener {
                    editDisposable.clear()
                    hideKeyboard(requireView())
                }
            }

            @SuppressLint("RxSubscribeOnError", "PrivateResource")
            override fun deleteStudent(student: Student) {
                val deleteDialog = MaterialAlertDialogBuilder(
                    context,
                    R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
                )
                    .setTitle(getString(R.string.title_delete_student))
                    .setIcon(R.drawable.ic_delete)
                    .setMessage(
                            getString(
                                R.string.msg_delete_student,
                                "${student.name[Student.FIRST_NAME_VAL]} ${student.name[Student.LAST_NAME_VAL]}"
                            ).getSpannedText()
                    )
                    .setView(R.layout.diag_delete_student)
                    .setPositiveButton(R.string.btn_ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
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
                        hideKeyboard(requireView())
                        passwordLayout.error = null

                        loadingView.visibility = View.VISIBLE
                        val disposable = viewModel.reAuth(passwordInput.text.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                loadingView.visibility = View.GONE
                            }.andThen(viewModel.deleteStudent(student))
                            .doFinally {
                                deleteDisposable.clear()
                                hideKeyboard(requireView())
                            }
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
                        hideKeyboard(requireView())
                    }

                }
                deleteDialog.setCanceledOnTouchOutside(false)
                deleteDialog.setOnCancelListener {
                    hideKeyboard(requireView())
                }
                deleteDialog.setOnDismissListener {
                    deleteDisposable.clear()
                    hideKeyboard(requireView())
                }
                deleteDialog.show()
            }
        }

        adaptor = StudentListAdaptor(
            this,
                !viewModel.canAddStudent(),
                recyclerOptions,
            studentActionListener, callback
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.canAddStudent()) btn_action.setOnClickListener { showAddStudentDialog() }
        else btn_action.visibility = View.GONE
    }

//    private fun getSpannedText(text: String): Spanned {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
//        } else {
//            Html.fromHtml(text)
//        }
//    }

    @SuppressLint("PrivateResource")
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

        val addDisposable = CompositeDisposable()

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
                                                Student.FIRST_NAME_VAL to firstNameInput.text.toString().trim().capitalizeWords(),
                                                Student.LAST_NAME_VAL to lastNameInput.text.toString().trim().capitalizeWords()
                                            ), lrnInput.text.toString().trim()
                                        )
                                    )
                                }
                            }.doFinally {
                                addDisposable.clear()
                                hideKeyboard(requireView())
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
                        addDisposable += disposable
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
                    hideKeyboard(requireView())
                }
            addStudentAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                addStudentAlertDialog.dismiss()
                hideKeyboard(requireView())
            }
        }

        addStudentAlertDialog.setCanceledOnTouchOutside(false)
        addStudentAlertDialog.setOnDismissListener {
            addDisposable.clear()
            hideKeyboard(requireView())
        }
        addStudentAlertDialog.show()
    }
}
