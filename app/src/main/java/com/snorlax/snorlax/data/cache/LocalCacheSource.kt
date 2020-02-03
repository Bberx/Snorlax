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

package com.snorlax.snorlax.data.cache

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.Constants.PREFS_KEY
import com.snorlax.snorlax.utils.Constants.USER_KEY
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.lang.RuntimeException

class LocalCacheSource private constructor(application: Context) {

    companion object {
        private var instance: LocalCacheSource? = null

        fun getInstance(application: Context): LocalCacheSource {
            instance?.let {
                return it
            }

            instance = LocalCacheSource(application)
            return getInstance(application)
        }

    }

    private val sharedPreferences =
        application.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    fun addToCache(user: User): Completable =
        Completable.create {
            val cache = sharedPreferences.edit()
            cache.putString(USER_KEY, Gson().toJson(user))

            if (cache.commit()) it.onComplete()
            else it.onError(RuntimeException("Could not write user to shared preferences"))
        }.subscribeOn(Schedulers.io())

    fun removeToCache(): Completable = Completable.create {emitter ->
        val cache = sharedPreferences.edit()
        cache.remove(USER_KEY)

        if (cache.commit()) emitter.onComplete()
        else emitter.onError(RuntimeException("Could not delete user"))
    }

    fun getUserCache(): User? {
        return Gson().fromJson(
            sharedPreferences.getString(
                USER_KEY, null
            ), User::class.java
        )
//            Gson().fromJson(cache.getString(USER_KEY, Gson().toJson(User())), User::class.java)
    }


}