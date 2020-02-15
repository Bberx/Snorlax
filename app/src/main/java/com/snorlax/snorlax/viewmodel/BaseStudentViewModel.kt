package com.snorlax.snorlax.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource

abstract class BaseStudentViewModel(application: Application) : AndroidViewModel(application) {
    protected val localCacheSource = LocalCacheSource.getInstance(application)
    protected val firestore = FirebaseFirestoreSource

    fun getStudentQuery() = firestore.getStudentQuery(getCurrentSection())

    protected fun getCurrentSection(): String = localCacheSource.getUserCache()!!.section

    fun canAddStudent(): Boolean =
        localCacheSource.getUserCache()!!.accType.equals("teacher", true)
}
