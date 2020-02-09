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


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.callback.StudentActionListener
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_student.view.student_displayName
import kotlinx.android.synthetic.main.item_student.view.student_image
import kotlinx.android.synthetic.main.item_student.view.student_lrn
import kotlinx.android.synthetic.main.item_student_list.view.*


class StudentListAdaptor(
    private val activity: Activity,
    private val lock: Boolean,
    private val options: FirestoreRecyclerOptions<Student>,
    private val studentActionListener: StudentActionListener,
    private val callback: (size: Int) -> Unit
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
        GlideApp.with(activity)
            .load(R.drawable.img_avatar)  // TODO load from storage
            .into(holder.image)
        holder.swipeRevealLayout.setLockDrag(lock)

        if (!lock) {
            holder.itemView.layout_main.setOnClickListener {
                if (holder.swipeRevealLayout.isOpened) holder.swipeRevealLayout.close(true)
                else holder.swipeRevealLayout.open(true)
            }
        }

        holder.deleteButton.setOnClickListener {
            studentActionListener.deleteStudent(model)
        }
        holder.editButton.setOnClickListener {
            studentActionListener.editStudent(position, model, options)
        }

    }

    override fun onDataChanged() {
        super.onDataChanged()
        callback(itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflatedView = parent.inflate(R.layout.item_student_list)
        return StudentViewHolder(inflatedView)
    }

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val displayName: TextView = itemView.student_displayName
        val lrn: TextView = itemView.student_lrn
        val image: CircleImageView = itemView.student_image
        val swipeRevealLayout: SwipeRevealLayout = itemView.layout_reveal
        val deleteButton: ImageButton = itemView.img_delete
        val editButton: ImageButton = itemView.img_edit


    }


//    private fun deleteStudent(student: Student) {
//        MaterialAlertDialogBuilder(activity)
//            .setTitle("Delete student")
//            .setIcon(R.drawable.ic_delete)
//            .setMessage("Are you sure you want to delete <b>${student.name["first"]} ${student.name["last"]}</b>? This action is irreversible. Retype your password to continue")
//            .setPositiveButton(android.R.string.yes) { _, _ ->
//                viewModel.deleteStudent(student)
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({
//                        Snackbar.make(
//                            activity.students_list,
//                            "Student deleted",
//                            Snackbar.LENGTH_SHORT
//                        )
//                            .show()
//                    }, {
//                        Snackbar.make(
//                            activity.students_list,
//                            it.localizedMessage!!,
//                            Snackbar.LENGTH_SHORT
//                        )
//                            .show()
//                    })
//            }
//            .setNegativeButton(android.R.string.no, null)
//            .show()
//    }
//
//    private fun editStudent(position: Int, student: Student) {
//        val alertDialog = MaterialAlertDialogBuilder(
//            activity,
//            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
//        )
//            .setTitle("Edit student")
//            .setIcon(R.drawable.ic_edit)
//            .setView(R.layout.diag_add_student)
//            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
//
//                val alert = (dialogInterface as AlertDialog)
//
//                if (alert.input_first_name.text!!.isNotEmpty() &&
//                    alert.input_last_name.text!!.isNotEmpty() /*&&
//                    alert.input_lrn.text!!.isNotEmpty()*/
//                ) {
//                    Completable.create { emitter ->
//                        options.snapshots.getSnapshot(position).reference.set(
//                            Student(
//                                mapOf(
//                                    "first" to alert.input_first_name.text.toString().trim(),
//                                    "last" to alert.input_last_name.text.toString().trim()
//                                ), alert.input_lrn.text.toString().trim()
//                            )
//                        ).addOnSuccessListener {
//                            emitter.onComplete()
//                        }.addOnFailureListener {
//                            emitter.onError(it)
//                        }
//                    }
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({
//                            Snackbar.make(
//                                activity.students_list,
//                                "Student edited",
//                                Snackbar.LENGTH_SHORT
//                            )
//                                .show()
//                        }, {
//                            Snackbar.make(
//                                activity.students_list,
//                                it.localizedMessage!!,
//                                Snackbar.LENGTH_SHORT
//                            ).show()
//                        })
//                }
//
//            }
//            .setNegativeButton(android.R.string.no, null)
//            .create()
//
//        alertDialog.show()
//        alertDialog.input_first_name.setText(student.name["first"])
//        alertDialog.input_last_name.setText(student.name["last"])
//        alertDialog.input_lrn.setText(student.lrn)
//        alertDialog.input_lrn.isEnabled = false
//        alertDialog.text_layout_lrn.isEnabled = false
//
//
//    }
}