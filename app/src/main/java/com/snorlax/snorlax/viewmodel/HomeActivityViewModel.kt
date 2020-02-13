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

package com.snorlax.snorlax.viewmodel

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.repositories.UserRepository
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.Constants.SECTION_LIST
import com.snorlax.snorlax.utils.caps
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class HomeActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository by lazy { UserRepository }

    private val localCacheSource: LocalCacheSource by lazy {
        LocalCacheSource.getInstance(
            application
        )
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

//    companion object {
//        private var instance: HomeActivityViewModel? = null
//
//        fun getInstance(context: Context) : HomeActivityViewModel {
//            instance?.let {
//                return it
//            }
//            instance = HomeActivityViewModel(context)
//            return  getInstance(context)
//        }
//    }

    fun logout(): Completable {
        return userRepository.logout()
//        return Completable.fromAction { userRepository.logout() }.andThen {
//            FirebaseAuth.AuthStateListener {auth ->
//                if (auth.currentUser == null) it.onComplete()
//            }
//        }
//         Completable.create {emitter ->
//            FirebaseAuth.AuthStateListener {
//                if (it.currentUser == null) emitter.onComplete()
//            }
//        }
//        return Completable.fromAction { userRepository.logout() }
//            .andThen(localCacheSource.removeToCache())
//            .subscribeOn(Schedulers.io())
    }


    fun getUser() = userRepository.currentUser(getApplication())

    fun getRole(user: User): String {
        val builder: StringBuilder = StringBuilder()

        val section = SECTION_LIST.getValue(user.section)
        builder.append("${section.grade_level}-${section.display_name} ")

        builder.append("(${user.accType.caps(Locale.getDefault())})")

        return builder.toString()
    }

    fun getUserPhoto(): Uri {
        return firebaseAuth.currentUser!!.photoUrl!!
    }

}