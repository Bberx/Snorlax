package com.snorlax.snorlax.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseAuthSource
import com.snorlax.snorlax.data.repositories.LateDataRepository
import com.snorlax.snorlax.model.LateData
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val lateDataObservable = BehaviorSubject.create<LateData>()

    fun listenLateData() {
        LateDataRepository.getLateData(section).subscribe(lateDataObservable)
    }

    private val section: String
        get() = LocalCacheSource.getInstance(getApplication()).getUserCache()!!.section

    fun reAuth(password: String): Completable {
        return FirebaseAuthSource.reAuth(password)
    }
}