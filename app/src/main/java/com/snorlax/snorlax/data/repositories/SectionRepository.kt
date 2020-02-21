package com.snorlax.snorlax.data.repositories

import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.Section
import com.snorlax.snorlax.utils.Constants
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

object SectionRepository {
    enum class Source { DATABASE, LOCAL }

    fun getSection(tag: String, source: Source): Single<Section> {
        return when (source) {
            Source.DATABASE -> FirebaseFirestoreSource.getSection(tag).subscribeOn(Schedulers.io())
            Source.LOCAL -> Single.just(Constants.SECTION_LIST.getValue(tag))
        }
    }
}