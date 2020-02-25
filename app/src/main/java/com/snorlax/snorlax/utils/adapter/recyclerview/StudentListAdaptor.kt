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

package com.snorlax.snorlax.utils.adapter.recyclerview


import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.callback.BaseStudentListener
import com.snorlax.snorlax.utils.callback.StudentEditListener
import com.snorlax.snorlax.utils.callback.StudentSelectListener
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_student_list.*

//import kotlinx.android.synthetic.main.item_student.view.student_image
//import kotlinx.android.synthetic.main.item_student.view.student_lrn
//import kotlinx.android.synthetic.main.item_student_list.view.*


class StudentListAdaptor(
    private val owner: Fragment,
    private val lock: Boolean,
    private val options: FirestoreRecyclerOptions<Student>,
    private val studentActionListener: BaseStudentListener,
    private val callback: (isEmpty: Boolean) -> Unit
) :
    FirestoreRecyclerAdapter<Student, StudentListAdaptor.StudentViewHolder>(options) {

    private val viewBinderHelper = ViewBinderHelper()

    init {
        viewBinderHelper.setOpenOnlyOne(true)
    }


    override fun onBindViewHolder(holder: StudentViewHolder, position: Int, model: Student) {

        viewBinderHelper.bind(holder.swipeRevealLayout, model.lrn)

        holder.displayName.text = model.displayName
        holder.lrn.text = model.lrn
        GlideApp.with(owner)
            .load(R.drawable.img_avatar)  // TODO load from storage
            .into(holder.image)

        holder.swipeRevealLayout.setLockDrag(lock)

        if (studentActionListener is StudentSelectListener) {
            holder.layout_main.setOnClickListener {
                studentActionListener.onSelectStudent(model)
            }
        } else if (studentActionListener is StudentEditListener) {
            holder.deleteButton.setOnClickListener {
                studentActionListener.deleteStudent(model)
            }
            holder.editButton.setOnClickListener {
                studentActionListener.editStudent(position, model, options)
            }
            if (!lock) {
                holder.layout_main.setOnClickListener {
                    if (holder.swipeRevealLayout.isOpened) holder.swipeRevealLayout.close(true)
                    else holder.swipeRevealLayout.open(true)
                }
            }
        }


    }

    override fun onDataChanged() {
        super.onDataChanged()
        callback(itemCount == 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflatedView = parent.inflate(R.layout.item_student_list)
        return StudentViewHolder(inflatedView)
    }

    class StudentViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        val displayName: TextView = student_displayName
        val lrn: TextView = student_lrn
        val image: CircleImageView = student_image
        val swipeRevealLayout: SwipeRevealLayout = layout_reveal
        val deleteButton: ImageButton = img_delete
        val editButton: ImageButton = img_edit


    }
}