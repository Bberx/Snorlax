package com.snorlax.snorlax.utils.callback

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.model.Student
import io.reactivex.disposables.CompositeDisposable

interface StudentEditListener: BaseStudentListener  {
    val editDisposable: CompositeDisposable
    val deleteDisposable: CompositeDisposable
     fun editStudent(
        position: Int,
        student: Student,
        options: FirestoreRecyclerOptions<Student>
    )

    fun deleteStudent(student: Student)
}