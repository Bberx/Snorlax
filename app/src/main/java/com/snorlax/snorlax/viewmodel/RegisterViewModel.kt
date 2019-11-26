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

import android.content.Context
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.data.repositories.UserRepository
import com.snorlax.snorlax.model.User
import com.snorlax.snorlax.utils.Constants.SECTION_LIST
import com.snorlax.snorlax.utils.validator.FormResult
import com.snorlax.snorlax.utils.validator.FormValidator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*

class RegisterViewModel(context: Context) {

    private val userRepository = UserRepository.getInstance(context)

    private val localCacheSource: LocalCacheSource by lazy {
        LocalCacheSource.getInstance()
    }

//    companion object {
//        private var instance: RegisterViewModel? = null
//
//        fun getInstance(context: Context) : RegisterViewModel {
//            instance?.let {
//                return it
//            }
//            instance = RegisterViewModel(context)
//            return getInstance(context)
//        }
//    }

    val registerButtonObservable: PublishSubject<Unit> = PublishSubject.create()

    val rawEmailObserver: PublishSubject<String> = PublishSubject.create()
    val rawPasswordObserver: BehaviorSubject<String> = BehaviorSubject.create()
    val rawPasswordConfirmObserver: BehaviorSubject<String> = BehaviorSubject.create()
    val rawFirstNameObserver: PublishSubject<String> = PublishSubject.create()
    val rawLastNameObserver: PublishSubject<String> = PublishSubject.create()
    val rawSectionObserver: PublishSubject<String> = PublishSubject.create()
    val rawAccTypeObserver: PublishSubject<String> = PublishSubject.create()

    private val emailObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val passwordObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val passwordConfirmObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val firstNameObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val lastNameObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val sectionObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val accTypeObserver: BehaviorSubject<String> = BehaviorSubject.createDefault("")

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

    private val passwordConfirmErrorObservable: Observable<FormResult.Result> =
        Observable.combineLatest(
            passwordObserver
                .filter { it.isNotEmpty() },
            passwordConfirmObserver
                .filter { it.isNotEmpty() },
            BiFunction { password, passwordConfirm ->
                FormValidator.isValidPasswordConfirm(password, passwordConfirm)
            })

    // TODO fix
//    private val passwordNotSameErrorObservable: Observable<FormResult.Result> =
//        passwordObserver.withLatestFrom(
//            passwordConfirmObserver,
//            BiFunction<String, String, FormResult.Result> { password, passwordConfirm ->
//                FormValidator.isValidPassword(password, passwordConfirm)
//            }
//        )
    private val firstNameErrorObservable: Observable<FormResult.Result> = firstNameObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidFirstName(it.trim())
        }

    private val lastNameErrorObservable: Observable<FormResult.Result> = lastNameObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidLastName(it.trim())
        }

    private val sectionErrorObservable: Observable<FormResult.Result> = sectionObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidSection(it.trim())
        }

    private val accTypeErrorObservable: Observable<FormResult.Result> = accTypeObserver
        .filter { it.isNotEmpty() }
        .map {
            FormValidator.isValidAccType(it.trim())
        }

//    val credentialsObservable: Observable<Completable> =
//        Observable.combineLatest(
//            emailObserver,
//            passwordObserver,
//            passwordConfirmObserver,
//            firstNameObserver,
//            lastNameObserver,
//            sectionObserver,
//            accTypeObserver,
//            Function7 { email, password, _, firstName, lastName, section, accType ->
//                userRepository.register(
//                    email,
//                    password,
//                    "$firstName $lastName",
//                    getSectionCode(section),
//                    accType
//                )
//            })

    private fun getSectionCode(sectionRaw: String): String {
        val sectionName = sectionRaw.substring(sectionRaw.lastIndexOf(" ") + 1, sectionRaw.length)
        for (sectionEntry in SECTION_LIST) {
            if (sectionName == sectionEntry.value.display_name) {
                return sectionEntry.key
            }
        }
        return ""
    }

//    val registerObservable: Observable<Completable> =
//        registerButtonObservable.withLatestFrom(credentialsObservable,
//            BiFunction { _, completer ->
//                completer
//            })

//    val resultObservable: Observable<Boolean> =
//        Observable.combineLatest(
//            emailErrorObservable,
//            passwordErrorObservable,
//            firstNameErrorObservable,
//            lastNameErrorObservable,
//            sectionErrorObservable,
//            accTypeErrorObservable,
//            Function6 { email, password, firstName, lastName, section, accType ->
//                FormResult.Builder().apply {
//                    addResult(email)
//                    addResult(password)
//                    addResult(firstName)
//                    addResult(lastName)
//                    addResult(section)
//                    addResult(accType)
//                }.build().isOverallSuccess()
//            }
//        )


    val fieldErrorsObservable: Observable<FormResult.Result> =
        Observable.merge(
            listOf(
                emailErrorObservable,
                passwordErrorObservable,
                passwordConfirmErrorObservable,
                firstNameErrorObservable,
                lastNameErrorObservable,
                sectionErrorObservable,
                accTypeErrorObservable
            )
        )

    fun validateFields(
        firstName: String,
        lastName: String,
        email: String,
        section: String,
        accType: String,
        password: String,
        passwordConfirm: String
    ): FormResult {
        return FormResult.Builder().apply {
            addResult(FormValidator.isValidEmail(email.trim()))
            addResult(FormValidator.isValidPassword(password))
            addResult(FormValidator.isValidPasswordConfirm(password, passwordConfirm))
            addResult(FormValidator.isValidFirstName(firstName.trim()))
            addResult(FormValidator.isValidLastName(lastName.trim()))
            addResult(FormValidator.isValidAccType(accType.trim()))
            addResult(FormValidator.isValidSection(section.trim()))

        }.build()
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        section: String,
        accType: String
    ) = userRepository.register(
        email.trim(),
        password,
        "${firstName.trim()} ${lastName.trim()}",
        getSectionCode(section).trim(),
        accType.trim().toLowerCase(Locale.ROOT)
    )

    fun getCurrentUser(context: Context) = userRepository.currentUser(context)

    fun addUserToCache(context: Context, user: User) : Completable {
        return localCacheSource.addToCache(context, user)
    }

    fun clearDisposable() {
        userRepository.clearDisposables()
    }
}