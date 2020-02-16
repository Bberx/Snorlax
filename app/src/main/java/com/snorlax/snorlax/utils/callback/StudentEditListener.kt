package com.snorlax.snorlax.utils.callback

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.model.Student
import io.reactivex.disposables.CompositeDisposable

abstract class StudentEditListener : BaseStudentListener {
    protected val editDisposable: CompositeDisposable = CompositeDisposable()
    protected val deleteDisposable: CompositeDisposable = CompositeDisposable()
    abstract fun editStudent(
        position: Int,
        student: Student,
        options: FirestoreRecyclerOptions<Student>
    )

    abstract fun deleteStudent(student: Student)
}