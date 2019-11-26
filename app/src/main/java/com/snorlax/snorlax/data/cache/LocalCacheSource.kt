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

import android.content.Context
import com.google.gson.Gson
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.Constants.PREFS_KEY
import com.snorlax.snorlax.utils.Constants.USER_KEY
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class LocalCacheSource private constructor() {

    companion object {
        private var instance : LocalCacheSource? = null

        fun getInstance() : LocalCacheSource {
            instance?.let {
                return it
            }

            instance = LocalCacheSource()
            return getInstance()
        }
    }

    fun addToCache(context: Context, user: User): Completable {
        val cache = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit()
        cache.putString(USER_KEY, Gson().toJson(user))
//
//        context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE).edit {
//            putString(USER_KEY, Gson().toJson(user))
//        }
//
        return Completable.create {
            if (cache.commit()) {
                it.onComplete()
            } else it.onError(Throwable("Could not write user to shared preferences"))
        }.subscribeOn(Schedulers.io())
    }

    fun getUserCache(context: Context): User? {
        val cache = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        return if (cache.contains(USER_KEY)) Gson().fromJson(cache.getString(USER_KEY, Gson().toJson(User())), User::class.java)
        else null
//            Gson().fromJson(cache.getString(USER_KEY, Gson().toJson(User())), User::class.java)
    }


}