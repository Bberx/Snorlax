package com.snorlax.snorlax.utils.callback

import com.snorlax.snorlax.model.Student

interface StudentSelectListener: BaseStudentListener  {
    fun onSelectStudent(student: Student)
}