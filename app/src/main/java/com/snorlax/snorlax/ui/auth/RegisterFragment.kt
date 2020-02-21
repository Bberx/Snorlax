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

package com.snorlax.snorlax.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.Constants.ACCOUNT_TYPES
import com.snorlax.snorlax.utils.Constants.SECTION_LIST
import com.snorlax.snorlax.utils.startHomeActivity
import com.snorlax.snorlax.utils.validator.FormResult
import com.snorlax.snorlax.utils.validator.FormResult.Message.Item.*
import com.snorlax.snorlax.utils.validator.FormValidator
import com.snorlax.snorlax.utils.validator.getErrorMessage
import com.snorlax.snorlax.utils.validator.getMessageItem
import com.snorlax.snorlax.viewmodel.RegisterViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credentials.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*


/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val accTypes = ArrayAdapter(
            context!!,
            R.layout.dropdown_menu_popup_item,
            ACCOUNT_TYPES
        )
        val sectionList = ArrayAdapter(
            context!!,
            R.layout.dropdown_menu_popup_item,
            SECTION_LIST.map {
                "${it.value.grade_level} - ${it.value.display_name}"
            })

        view.input_account_type.setAdapter(accTypes)
        view.input_section.setAdapter(sectionList)

        view.input_first_name.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawFirstNameObserver)
        view.input_last_name.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawLastNameObserver)
        view.input_email.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawEmailObserver)
        view.input_section.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawSectionObserver)
        view.input_account_type.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawAccTypeObserver)
        view.input_password.textChanges().map { it.toString() }
            .subscribe(viewModel.rawPasswordObserver)
        view.input_password_confirm.textChanges().map { it.toString() }
            .subscribe(viewModel.rawPasswordConfirmObserver)
        view.btn_register.clicks()
            .subscribe(viewModel.registerButtonObservable)
        view.btn_have_account.setOnClickListener {
            val activity = requireActivity() as CredentialsActivity
            activity.view_pager.currentItem = 0
        }

        return view
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(requireView().windowToken, 0)
        requireView().clearFocus()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObservables()
    }

    private fun initObservables() {
        disposables.apply {
            add(viewModel.registerButtonObservable.subscribe {
                hideKeyboard()

                val results = viewModel.validateFields(
                    input_first_name.text.toString(),
                    input_last_name.text.toString(),
                    input_email.text.toString(),
                    input_section.text.toString(),
                    input_account_type.text.toString(),
                    input_password.text.toString(),
                    input_password_confirm.text.toString()
                )

                if (results.isOverallSuccess()) {
                    btn_register.startAnimation()

                    add(
                        viewModel.register(
                            input_email.text.toString(),
                            input_password.text.toString(),
                            input_first_name.text.toString(),
                            input_last_name.text.toString(),
                            input_section.text.toString(),
                            input_account_type.text.toString()
                        )
//                            .flatMapCompletable {
//                            viewModel.addUserToCache(context!!, it)
//                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                requireActivity().startHomeActivity()
                            }, {
                                FirebaseAuth.getInstance().apply {
                                    currentUser?.let {
                                        viewModel.logout()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({}, {error ->
                                                debugMessage(error.localizedMessage ?: "Logout failed")
                                            })
                                    }
                                }
                                btn_register.revertAnimation()
                                it.message?.let { message ->
                                    debugMessage(message)
                                }

                            })
                    )
                } else {
                    btn_register.revertAnimation()
                    add(Observable.merge(
                        listOf(
                            viewModel.rawEmailObserver.map {
                                FormValidator.isValidEmail(it)
                            },
                            viewModel.rawPasswordObserver.map {
                                FormValidator.isValidPassword(it)
                            },
                            Observable.combineLatest(
                                viewModel.rawPasswordObserver,
                                viewModel.rawPasswordConfirmObserver,
                                BiFunction<String, String, FormResult.Result> { password, passwordConfirm ->
                                    FormValidator.isValidPasswordConfirm(
                                        password,
                                        passwordConfirm
                                    )
                                }),

                            viewModel.rawFirstNameObserver.map {
                                FormValidator.isValidFirstName(it)
                            },
                            viewModel.rawLastNameObserver.map {
                                FormValidator.isValidLastName(it)
                            },
                            viewModel.rawSectionObserver.map {
                                FormValidator.isValidSection(it)
                            },
                            viewModel.rawAccTypeObserver.map {
                                FormValidator.isValidAccType(it)
                            })
                    ).subscribe {

                        when (it.message.getMessageItem()) {
                            EMAIL ->
                                text_layout_email.error = it.message.getErrorMessage()
                            PASSWORD ->
                                text_layout_password.error = it.message.getErrorMessage()
                            PASSWORD_CONFIRM ->
                                text_layout_password_confirm.error = it.message.getErrorMessage()
                            FIRST_NAME ->
                                text_layout_first_name.error = it.message.getErrorMessage()
                            LAST_NAME ->
                                text_layout_last_name.error = it.message.getErrorMessage()
                            SECTION ->
                                text_layout_section.error = it.message.getErrorMessage()
                            ACC_TYPE ->
                                text_layout_account_type.error = it.message.getErrorMessage()
                        }
                    }
                    )

                    for (error in results.results) {
                        when (error.message.getMessageItem()) {
                            EMAIL ->
                                text_layout_email.error = error.message.getErrorMessage()
                            PASSWORD ->
                                text_layout_password.error = error.message.getErrorMessage()
                            PASSWORD_CONFIRM ->
                                text_layout_password_confirm.error = error.message.getErrorMessage()
                            FIRST_NAME ->
                                text_layout_first_name.error = error.message.getErrorMessage()
                            LAST_NAME ->
                                text_layout_last_name.error = error.message.getErrorMessage()
                            SECTION ->
                                text_layout_section.error = error.message.getErrorMessage()
                            ACC_TYPE ->
                                text_layout_account_type.error = error.message.getErrorMessage()
                        }
                    }
                }
            })

            add(viewModel.fieldErrorsObservable.subscribe { result ->
                when (result.message.getMessageItem()) {
                    EMAIL ->
                        text_layout_email.error = result.message.getErrorMessage()
                    PASSWORD ->
                        text_layout_password.error = result.message.getErrorMessage()
                    PASSWORD_CONFIRM ->
                        text_layout_password_confirm.error = result.message.getErrorMessage()
                    FIRST_NAME ->
                        text_layout_first_name.error = result.message.getErrorMessage()
                    LAST_NAME ->
                        text_layout_last_name.error = result.message.getErrorMessage()
                    SECTION ->
                        text_layout_section.error = result.message.getErrorMessage()
                    ACC_TYPE ->
                        text_layout_account_type.error = result.message.getErrorMessage()

                }
            })
        }
    }

    private fun debugMessage(text: String) {
        Snackbar.make(view!!, text, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
//        btn_register.dispose()
        super.onDestroy()
        disposables.dispose()
    }
}