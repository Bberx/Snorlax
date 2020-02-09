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

package com.snorlax.snorlax.utils.callback

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.model.Student
import io.reactivex.disposables.CompositeDisposable

abstract class StudentActionListener  {
    protected val editDisposable = CompositeDisposable()
    protected val deleteDisposable = CompositeDisposable()

    abstract fun editStudent(position: Int, student: Student, options: FirestoreRecyclerOptions<Student>)
    abstract fun deleteStudent(student: Student)
}