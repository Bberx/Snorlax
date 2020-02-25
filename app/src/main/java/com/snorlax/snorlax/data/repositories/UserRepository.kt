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

package com.snorlax.snorlax.data.repositories

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseAuthSource
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.data.firebase.StorageSource
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.capitalizeWords
import com.snorlax.snorlax.utils.updateAdminProfile
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

object UserRepository {

    //    companion object {
//
//        private var instance: UserRepository? = null
//
//        fun getInstance(context: Context): UserRepository {
//            instance?.let {
//                return it
//            }
//            instance = UserRepository(context)
//            return instance!!
//        }
//    }
//    companion object {
//        fun getInstance() = UserRepository()
//    }


    private val firebase: FirebaseAuthSource by lazy {
        FirebaseAuthSource
    }

//    private val localCache: LocalCacheSource by lazy {
//        LocalCacheSource.getInstance()
//    }


    private val firestore by lazy {
        FirebaseFirestoreSource
    }

    private val storage by lazy {
        StorageSource.instance
    }

    fun login(email: String, password: String) = firebase.login(email, password)
        .map { it.uid }
        .flatMap(firestore::getAdmin)
        .subscribeOn(Schedulers.io())

    fun register(
        _email: String,
        password: String,
        _name: String,
        section: String,
        accType: String
    ): Single<User> {

        val email = _email.toLowerCase(Locale.getDefault())
        val name = _name.capitalizeWords()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(storage.defaultAvatarURL)
            .build()

        return firebase.register(email, password).flatMap {
            it.updateAdminProfile(profileUpdates)
                .andThen(firestore.addAdmin(User(name, section, accType, it.uid, it.email!!)))
                .andThen(firestore.getAdmin(it.uid))
        }.subscribeOn(Schedulers.io())
    }

    fun currentUser(context: Context): Maybe<User> {
        val cache = LocalCacheSource.getInstance(context.applicationContext).getUserCache()

        val databaseUser =
            firestore.getAdmin(firebase.currentUser()!!.uid)
                .subscribeOn(Schedulers.io())

        cache?.let {
            return Maybe.just(it)
        }
        return databaseUser.toMaybe()
    }


//    fun logout(): Completable {
//        return Completable.fromAction {
//            firebase.logout()
//        }.andThen {
//            FirebaseAuth.getInstance().addAuthStateListener { auth ->
//                if (auth.currentUser == null) it.onComplete()
//            }
//        }
//    }

    @SuppressLint("RxLeakedSubscription")
    fun logout(context: Context): Completable {
        return Completable.fromAction {
            firebase.logout()
        }.andThen {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                if (auth.currentUser == null) {
                    LocalCacheSource.getInstance(context.applicationContext).removeToCache()
                        .subscribeBy(onComplete = {
                            it.onComplete()
                        }, onError = { error ->
                            it.onError(error)
                        })
                }
            }
        }
    }
}