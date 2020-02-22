package com.snorlax.snorlax.data.repositories

import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.LateData
import com.snorlax.snorlax.utils.Constants
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

object LateDataRepository {
    fun getLateDataSingle(section: String): Single<LateData> {
        val database = FirebaseFirestoreSource.getLateData(section).firstElement()
//            .flatMap {
//            if (it.late_time < 0) Maybe.empty()
//            else Maybe.just(it)
//        }
        val local = Single.just(Constants.SECTION_LIST.getValue(section).late_data!!)
        return Maybe.concat(database, local.toMaybe()).firstElement().toSingle()
    }

    fun getLateData(section: String): Observable<LateData> {
        return FirebaseFirestoreSource.getLateData(section)
    }

    fun updateLateData(section: String, lateData: LateData): Completable {
        return FirebaseFirestoreSource.updateLateData(section, lateData)
    }
}