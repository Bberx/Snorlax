package com.snorlax.snorlax.utils.adapter.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_dashboard.*

class DashboardAdapter(
    options: FirestoreRecyclerOptions<Student>,
    private val callback: (Student) -> Unit
) :
    FirestoreRecyclerAdapter<Student, DashboardAdapter.StudentHolder>(options) {

    class StudentHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(model: Student, callback: (Student) -> Unit) {
            GlideApp.with(student_image)
                .load(model.imageUrl)
                .placeholder(R.drawable.img_avatar)
                .into(student_image)

            student_displayName.text = model.displayName
            student_lrn.text = model.lrn
            layout_main.setOnClickListener {
                callback(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StudentHolder(parent.inflate(R.layout.item_dashboard))

    override fun onBindViewHolder(holder: StudentHolder, position: Int, model: Student) =
        holder.bind(model, callback)

}