package com.snorlax.snorlax.ui.home

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.utils.TimeUtils
import com.snorlax.snorlax.utils.preference.TimePickerPreference
import com.snorlax.snorlax.utils.preference.TimePreferenceDialog
import com.snorlax.snorlax.viewmodel.SettingsViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_delete_student.*
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewmodel: SettingsViewModel
    private val disposable = CompositeDisposable()

    private val confirmationDisposable = CompositeDisposable()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is TimePickerPreference) {
            val pickerCallback = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                sensitiveAction {
                    preference.saveTime(TimeUtils.hoursMinutesToSeconds(hourOfDay, minute))
                }
            }
            TimePreferenceDialog(
                requireActivity(),
                pickerCallback,
                viewmodel.lateDataObservable.value!!
            ).show()
        } else {
            super.onDisplayPreferenceDialog(preference)
        }


    }

    override fun onStart() {
        super.onStart()

        viewmodel.listenLateData()
        disposable += viewmodel.lateDataObservable
            .flatMapSingle { data ->
                val user =
                    if (data.who_uid == null) Single.just("") else FirebaseFirestoreSource.getAdmin(
                        data.who_uid
                    ).map { it.displayName }
                val editTime =
                    if (data.last_edit == null) Single.just(Timestamp(Date(0))) else Single.just(
                        data.last_edit
                    )
                Single.zip<String, Timestamp, Pair<String, Timestamp>>(
                    user,
                    editTime,
                    BiFunction(::Pair)
                )
            }.subscribeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { data ->
                val who: String = data.first
                val time: Timestamp = data.second
                val timeFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())

                val timePref = preferenceManager.findPreference<TimePickerPreference>("lateTime")!!
                timePref.summaryProvider = Preference.SummaryProvider<TimePickerPreference> {
                    if (who.isNotEmpty() && time.toDate().time != 0L) {
                        "Last changed by $who on ${timeFormat.format(time.toDate())}"
                    } else {
                        "Default late time"
                    }
                }
                timePref.isEnabled = true
            }, onError = {
                // TODO implement
            })
    }

    @SuppressLint("RxSubscribeOnError", "PrivateResource")
    fun sensitiveAction(action: () -> Unit) {
        val confirmationDialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.label_sensitive_action)
            .setMessage(R.string.msg_sensitive_late_time)
            .setIcon(R.drawable.ic_alert)
            .setView(R.layout.diag_delete_student)
            .setPositiveButton(R.string.btn_ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        confirmationDialog.setOnShowListener { dialog ->
            val passwordLayout = confirmationDialog.text_layout_input_reauth_password
            val passwordInput = confirmationDialog.input_reauth_password
            val loadingView = confirmationDialog.reauth_password_loading

            val positiveButton = confirmationDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = confirmationDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            val passwordDisposable = passwordInput.textChanges().subscribe {
                positiveButton.isEnabled = it.length >= 6
            }
            confirmationDisposable.add(passwordDisposable)

            negativeButton.setOnClickListener {
                dialog.dismiss()
                Snackbar.make(
                    requireView(),
                    R.string.msg_late_time_not_saved,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            positiveButton.setOnClickListener {
                passwordLayout.error = null

                loadingView.visibility = View.VISIBLE
                val disposable = viewmodel.reAuth(passwordInput.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally {
                        loadingView.visibility = View.GONE
                    }.subscribeBy(
                        onComplete = {
                            action()
                            dialog.dismiss()
                            Snackbar.make(
                                requireView(),
                                R.string.msg_late_time_saved,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        },
                        onError = { error ->
                            when (error) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    if (error.errorCode == "ERROR_WRONG_PASSWORD") {
                                        passwordLayout.error =
                                            getString(R.string.err_incorrect_password)
                                    } else passwordLayout.error = error.localizedMessage
                                }
                                is FirebaseTooManyRequestsException -> {
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
                confirmationDisposable.add(disposable)
            }

        }
        confirmationDialog.setCanceledOnTouchOutside(false)
        confirmationDialog.setOnDismissListener {
            confirmationDisposable.clear()
        }
        confirmationDialog.show()
    }


    override fun onPause() {
        super.onPause()
        disposable.clear()
    }
}