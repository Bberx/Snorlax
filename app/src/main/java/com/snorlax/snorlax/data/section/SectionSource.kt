package com.snorlax.snorlax.data.section

import com.snorlax.snorlax.model.Section
import com.snorlax.snorlax.utils.Constants

object SectionSource {
//    enum class Source {
////        DATABASE,
//        LOCAL
//    }

    fun getSection(tag: String): Section {
        return Constants.SECTION_LIST.getValue(tag)
//            Source.DATABASE -> FirebaseFirestoreSource.getSection(tag).subscribeOn(Schedulers.io())

    }
}