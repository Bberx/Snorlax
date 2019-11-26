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

package com.snorlax.snorlax.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Completable
import io.reactivex.Single


class FirebaseAuthSource private constructor() {

    companion object {
        private val M_INSTANCE: FirebaseAuthSource = FirebaseAuthSource()

        fun getInstance(): FirebaseAuthSource = M_INSTANCE
    }

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun sendResetPasswordEmail(email: String) : Completable {
        return Completable.create {emitter ->
            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }

    }

    fun login(email: String, password: String): Single<FirebaseUser> =
        Single.create { emitter ->
            if (mAuth.currentUser == null) {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { emitter.onSuccess(it.user!!) }
                    .addOnFailureListener { emitter.onError(it) }
            } else emitter.onError(Throwable("Already logged in with user: ${currentUser()!!.email}"))
        }

    fun register(email: String, password: String): Single<FirebaseUser> =
        Single.create { emitter ->
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (!emitter.isDisposed) {
                    if (it.isSuccessful) emitter.onSuccess(it.result!!.user!!)
                    else emitter.onError(it.exception!!)
                }
            }
        }

    fun logout() = mAuth.signOut()


    fun currentUser() = mAuth.currentUser
}