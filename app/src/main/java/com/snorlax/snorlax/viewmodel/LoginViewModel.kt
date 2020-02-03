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


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.firebase.FirebaseAuthSource
import com.snorlax.snorlax.data.repositories.UserRepository
import com.snorlax.snorlax.utils.validator.FormResult
import com.snorlax.snorlax.utils.validator.FormValidator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository.getInstance()

    private val firebaseAuth = FirebaseAuthSource.getInstance()

    private val localCacheSource: LocalCacheSource by lazy {
        LocalCacheSource.getInstance(application)
    }


    val loginButtonObservable: PublishSubject<Unit> = PublishSubject.create()

    val rawEmailObservable: PublishSubject<String> = PublishSubject.create()
    val rawPasswordObservable: PublishSubject<String> = PublishSubject.create()

    private val emailObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val passwordObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")


    private val emailErrorObservable: Observable<FormResult.Result> = emailObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidEmail(it)
        }

    private val passwordErrorObservable: Observable<FormResult.Result> = passwordObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidPassword(it)
        }

    val fieldErrorsObservable: Observable<FormResult.Result> =
        Observable.merge(emailErrorObservable, passwordErrorObservable)

    fun validateFields(email: String, password: String): FormResult {
        return FormResult.Builder().apply {
            addResult(FormValidator.isValidEmail(email.trim()))
            addResult(FormValidator.isValidPassword(password))
        }.build()
    }

    fun sendPasswordReset(email: String) = firebaseAuth.sendResetPasswordEmail(email)

    fun logout(): Completable {
        return Completable.fromAction { userRepository.logout() }
            .andThen(localCacheSource.removeToCache())
            .subscribeOn(Schedulers.io())
    }

    fun login(email: String, password: String): Completable = userRepository.login(email, password)
        .flatMapCompletable { localCacheSource.addToCache(it) }
        .subscribeOn(Schedulers.io())


}