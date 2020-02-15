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
import com.snorlax.snorlax.data.firebase.FirebaseAuthSource
import com.snorlax.snorlax.model.Student
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class StudentsViewModel(application: Application) : BaseStudentViewModel(application) {

    fun addStudent(student: Student): Completable {
        return firestore.addStudent(getCurrentSection(), student)
    }

    fun reAuth(password: String): Completable {
        return FirebaseAuthSource.reAuth(password)
    }

    fun deleteStudent(student: Student): Completable {
        return firestore.deleteStudent(getCurrentSection(), student)
    }

    fun studentExist(lrn: String): Single<Boolean> {
        return firestore.getStudentList(localCacheSource.getUserCache()!!.section)
            .subscribeOn(Schedulers.io())
            .map {
                val students = it.map { student -> student.lrn }
                lrn in students
            }
    }
}