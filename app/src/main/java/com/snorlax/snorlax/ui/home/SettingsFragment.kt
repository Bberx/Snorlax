package com.snorlax.snorlax.ui.home

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.Timestamp
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
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewmodel: SettingsViewModel
    private val disposable = CompositeDisposable()

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
                preference.saveTime(TimeUtils.hoursMinutesToSeconds(hourOfDay, minute))
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
                    if (data.who_uid == null) Single.just(null) else FirebaseFirestoreSource.getAdmin(
                        data.who_uid
                    ).map { it.displayName }
                Single.zip<String?, Timestamp?, Pair<String?, Timestamp?>>(
                    user,
                    Single.just(data.last_edit),
                    BiFunction(::Pair)
                )
            }.subscribeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { data: Pair<String?, Timestamp?> ->
                val who: String? = data.first
                val time: Timestamp? = data.second
                val timeFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())

                val timePref = preferenceManager.findPreference<TimePickerPreference>("lateTime")!!
                timePref.summaryProvider = Preference.SummaryProvider<TimePickerPreference> {
                    if (!who.isNullOrEmpty() && time != null) {
                        "Last changed by $who on ${timeFormat.format(time.toDate())}"
                    } else {
                        "Default late time"
                    }
                }
                timePref.isEnabled = true
            })
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }
}