package com.snorlax.snorlax.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.inflate
import com.snorlax.snorlax.utils.toPx
import com.snorlax.snorlax.viewmodel.BaseStudentViewModel
import com.snorlax.snorlax.views.ShimmerListProgress
import kotlinx.android.synthetic.main.fragment_students.*
import kotlinx.android.synthetic.main.fragment_students.view.*
import kotlinx.android.synthetic.main.layout_empty_list.view.*

abstract class BaseStudentFragment : Fragment() {

    abstract val viewModel: BaseStudentViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View

    abstract val adaptor: StudentListAdaptor

    protected lateinit var recyclerOptions: FirestoreRecyclerOptions<Student>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_students, container, false)

    }

    protected val callback = { isEmpty: Boolean ->
        val frame = student_list_container
        if (isEmpty) {
            if (!frame.contains(emptyView)) {
                frame.removeAllViews()
                frame.addView(emptyView)
            }
        } else {
            if (!frame.contains(recyclerView)) {
                frame.removeAllViews()
                frame.addView(recyclerView)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frame = view.student_list_container
        val shimmerView = ShimmerListProgress(requireContext()).apply {
            setLayoutChild(R.layout.shimmer_layout_student)
        }
        frame.addView(shimmerView)

        recyclerOptions =
            FirestoreRecyclerOptions.Builder<Student>()
                .setLifecycleOwner(this)
                .setQuery(viewModel.getStudentQuery(), Student::class.java)
                .build()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView = RecyclerView(requireContext()).apply {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(16.toPx())
            layoutManager = LinearLayoutManager(requireContext())
        }

        emptyView = student_list_container.inflate(R.layout.layout_empty_list).apply {
            val resolution =
                if (viewModel.canAddStudent()) getString(R.string.msg_reso_add_students) else context.getString(
                    R.string.msg_reso_ask_teacher
                )
            this.label_empty.text = getString(R.string.msg_empty_student_list, resolution)
        }
        recyclerView.adapter = adaptor
    }
}