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

import android.app.AlertDialog
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.startHomeActivity
import com.snorlax.snorlax.utils.validator.FormResult.Message.Item.EMAIL
import com.snorlax.snorlax.utils.validator.FormResult.Message.Item.PASSWORD
import com.snorlax.snorlax.utils.validator.FormValidator
import com.snorlax.snorlax.utils.validator.getErrorMessage
import com.snorlax.snorlax.utils.validator.getMessageItem
import com.snorlax.snorlax.viewmodel.LoginViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credentials.*
import kotlinx.android.synthetic.main.diag_forgot_password.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : Fragment() {


    private lateinit var viewModel: LoginViewModel

    private lateinit var vibrator: Vibrator

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_login, container, false)

        vibrator = context!!.getSystemService()!!

        view.btn_login.clicks()
            .subscribe(viewModel.loginButtonObservable)
        view.input_email.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawEmailObservable)
        view.input_password.textChanges().map { it.toString().trim() }
            .subscribe(viewModel.rawPasswordObservable)

        view.btn_forgot_password.setOnClickListener {
            showForgotDialog()
        }
        view.btn_create_account.setOnClickListener {
            (requireActivity() as CredentialsActivity).view_pager.currentItem = 1
        }


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObservables()
    }

    private fun showForgotDialog() {
        val forgotDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setIcon(R.drawable.ic_password_reset)
            .setTitle(getString(R.string.label_reset_your_password))
            .setMessage(getString(R.string.msg_forgot_password))
            .setView(R.layout.diag_forgot_password)
            .setPositiveButton(getString(R.string.btn_send_password_reset_email), null)
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .create()

        forgotDialog.setOnShowListener { dialog ->


            forgotDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val textLayout = forgotDialog.text_layout_input_forgot_password
                val loadingView = forgotDialog.reauth_password_loading

                textLayout.error = null
                loadingView.visibility = View.VISIBLE

                viewModel
                    .sendPasswordReset(forgotDialog.input_forgot_password.text.toString().trim())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        loadingView.visibility = View.GONE
                        dialog.dismiss()
                        showMessage(getString(R.string.msg_password_reset_success))
                    }, {
                        loadingView.visibility = View.GONE
                        if (it is IllegalArgumentException) {
                            textLayout.error = getString(R.string.err_invalid_email)
                        } else {
                            textLayout.error = it.localizedMessage!!
                        }
                    })
            }
        }


        forgotDialog.setCanceledOnTouchOutside(false)
        forgotDialog.show()

    }

    private fun initObservables() {
        disposables.apply {

            add(viewModel.loginButtonObservable.subscribe {

                val results = viewModel.validateFields(
                    input_email.text.toString(),
                    input_password.text.toString()
                )

                if (results.isOverallSuccess()) {
                    btn_login.startAnimation()
                    add(
                        viewModel.login(
                            input_email.text.toString(),
                            input_password.text.toString()
                        )
//                            .flatMapCompletable { viewModel.addUserToCache(context!!, it) }
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
                                                showMessage(
                                                    error.localizedMessage ?: "Logout failed"
                                                )
                                            })
                                    }
                                }
                                btn_login.revertAnimation()
                                it.localizedMessage?.let { message ->
                                    showMessage(message)
                                }
                            })
                    )
                } else {
                    btn_login.revertAnimation()
                    add(
                        Observable.merge(
                            viewModel.rawEmailObservable.map {
                                FormValidator.isValidEmail(it)
                            }, viewModel.rawPasswordObservable.map {
                                FormValidator.isValidPassword(it)
                            }
                        ).subscribe {
                            when (it.message.getMessageItem()) {
                                EMAIL ->
                                    text_layout_email.error = it.message.getErrorMessage()
                                PASSWORD ->
                                    text_layout_password.error = it.message.getErrorMessage()
                                else -> {
                                    // DO NOTHING
                                }
                            }
                        }
                    )

                    for (error in results.results) {
                        when (error.message.getMessageItem()) {
                            EMAIL -> text_layout_email.error = error.message.getErrorMessage()
                            PASSWORD -> text_layout_password.error =
                                error.message.getErrorMessage()
                            else -> {
                                // DO NOTHING
                            }
                        }
                    }
                }
            })


            // Field validity observable
            add(viewModel.fieldErrorsObservable.subscribe { result ->
                when (result.message.getMessageItem()) {
                    EMAIL -> text_layout_email.error = result.message.getErrorMessage()
                    PASSWORD -> text_layout_password.error = result.message.getErrorMessage()
                    else -> {
                        // DO NOTHING
                    }
                }
            })
        }
    }

    private fun showMessage(text: String) {
        Snackbar.make(view!!, text, Snackbar.LENGTH_LONG).show()
    }


    override fun onDestroy() {
//        btn_login.dispose()
        super.onDestroy()
        disposables.dispose()
    }
}