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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_attendance.view.*
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdaptor(private val context: Context) :
    ListAdapter<Attendance, AttendanceAdaptor.AttendanceHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Attendance>() {
            override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
                return oldItem.lrn == newItem.lrn
            }

            override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
                return oldItem.lrn == newItem.lrn &&
                        oldItem.time_in == newItem.time_in &&
                        oldItem.student == newItem.student
            }
        }
    }

    private val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceHolder {
        val inflatedView = parent.inflate(R.layout.item_attendance)
        return AttendanceHolder(inflatedView)
    }

    class AttendanceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentLogo: CircleImageView = itemView.student_image
        val studentName: TextView = itemView.student_displayName
        val studentLrn: TextView = itemView.student_lrn
        val studentTimeIn: TextView = itemView.label_time_in
    }

    override fun onBindViewHolder(holder: AttendanceHolder, position: Int) {
        val currentAttendance = getItem(position)

        val student = currentAttendance.student

        holder.studentTimeIn.text =
            clockFormat.format(currentAttendance.time_in.toDate())
        holder.studentLrn.text = student.lrn
        holder.studentName.text = student.displayName

        GlideApp.with(holder.studentLogo)
            .load(R.drawable.img_avatar)
            .into(holder.studentLogo)

        currentAttendance.isLate?.let {
            if (it) {
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.lateColor
                    )
                )
            } else {
                holder.itemView.setBackgroundColor(0)
            }
        } ?: run {
            holder.itemView.setBackgroundColor(0)
        }

    }
}