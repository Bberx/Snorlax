package com.snorlax.snorlax.utils.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.DialogPreference
import com.google.firebase.Timestamp
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.repositories.LateDataRepository
import com.snorlax.snorlax.model.LateData
import com.snorlax.snorlax.utils.TimeUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class TimePickerPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        TypedArrayUtils.getAttr(
            context,
            R.attr.dialogPreferenceStyle,
            android.R.attr.dialogPreferenceStyle
        )
    )

    constructor(context: Context) : this(context, null)

    private val disposable = CompositeDisposable()

    fun saveTime(seconds: Long) {
        disposable += updateLateData(seconds)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            })
    }

    private fun updateLateData(seconds: Long): Completable {
        val data = LateData(
            seconds,
            LocalCacheSource.getInstance(context).getUserCache()!!.uid,
            Timestamp(TimeUtils.getTodayDateUTC())
        )
        return LateDataRepository.updateLateData(section, data)
    }

    private val section: String
        get() = LocalCacheSource.getInstance(context).getUserCache()!!.section

    override fun onDetached() {
        super.onDetached()
        disposable.clear()
    }
}