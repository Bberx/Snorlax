package com.snorlax.snorlax.utils.callback

import com.snorlax.snorlax.model.Student

abstract class StudentSelectListener : BaseStudentListener {
    abstract fun onSelectStudent(student: Student)
}