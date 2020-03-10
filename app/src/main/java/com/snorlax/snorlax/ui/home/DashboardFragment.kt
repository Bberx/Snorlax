package com.snorlax.snorlax.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentSelectListener
import com.snorlax.snorlax.viewmodel.DashboardViewModel
import kotlinx.android.synthetic.main.fragment_students.*

class DashboardFragment : BaseStudentFragment() {

    override lateinit var viewModel: DashboardViewModel
    override lateinit var adaptor: StudentListAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listener = object : StudentSelectListener {
            override fun onSelectStudent(student: Student) {
                Toast.makeText(requireContext(), "You've selected $student", Toast.LENGTH_SHORT).show()
            }
        }
        adaptor = StudentListAdaptor(
            this,
            true,
            recyclerOptions,
            listener, callback
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn_action.visibility = View.GONE
    }


}
