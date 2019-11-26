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
import com.snorlax.snorlax.data.firebase.FirebaseFirestoreSource
import com.snorlax.snorlax.model.Student
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class StudentsViewModel {

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

    private val localCacheSource = LocalCacheSource.getInstance()

    private val firestore = FirebaseFirestoreSource.getInstance()

    val students = mutableListOf<Student>()

    fun addStudent(context: Context, student: Student): Completable {
        return firestore.addStudent(getCurrentSection(context), student)
    }

    fun getStudents(context: Context) = firestore.getStudentList(getCurrentSection(context)).subscribeOn(Schedulers.io())

    fun getStudentQuery(context: Context) = firestore.getStudentQuery(getCurrentSection(context))

    fun getCurrentSection(context: Context) : String {
        return localCacheSource.getUserCache(context)!!.section
    }

    fun deleteStudent(context: Context, student: Student): Completable {
        return firestore.deleteStudent(getCurrentSection(context), student)
    }

    fun canAddStudent(context: Context): Boolean {
        return (localCacheSource.getUserCache(context)!!.accType.equals("teacher", true))
    }
}