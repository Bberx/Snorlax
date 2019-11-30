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
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.Student
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class StudentsViewModel(application: Application) : AndroidViewModel(application) {

//    companion object {
//        private var instance: StudentsViewModel? = null
//
//        fun getInstance(context: Context) : StudentsViewModel {
//            instance?.let {
//                return it
//            }
//            instance = StudentsViewModel(context)
//            return getInstance(context)
//        }
//    }

    private val firebaseAuthSource = FirebaseAuthSource.getInstance()

    private val localCacheSource = LocalCacheSource.getInstance()

    private val firestore = FirebaseFirestoreSource.getInstance()

    val students = mutableListOf<Student>()

    fun addStudent(student: Student): Completable {
        return firestore.addStudent(getCurrentSection(), student)
    }

    fun reauth(password: String): Completable {
        return firebaseAuthSource.reauth(password)
    }

    fun getStudents() = firestore.getStudentList(getCurrentSection()).subscribeOn(Schedulers.io())

    fun getStudentQuery() = firestore.getStudentQuery(getCurrentSection())

    fun getCurrentSection(): String {
        return localCacheSource.getUserCache(getApplication())!!.section
    }

    fun deleteStudent(student: Student): Completable {
        return firestore.deleteStudent(getCurrentSection(), student)
    }

    fun canAddStudent(): Boolean {
        return (localCacheSource.getUserCache(getApplication())!!.accType.equals("teacher", true))
    }
}