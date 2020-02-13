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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Attendance
import com.snorlax.snorlax.utils.TimeUtils
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_attendance.view.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class AttendanceAdaptor : RecyclerView.Adapter<AttendanceAdaptor.AttendanceHolder>() {

    companion object {
        private const val ITEM_EMPTY = 1
        private const val ITEM_DEFAULT = 2
    }

    // TODO: Coding flow recycler view
    private val mAttendance: SortedList<Attendance> =
        SortedList(Attendance::class.java, object : SortedList.Callback<Attendance>() {
            override fun areItemsTheSame(item1: Attendance?, item2: Attendance?) =
                item1?.lrn == item2?.lrn

            override fun onMoved(fromPosition: Int, toPosition: Int) =
                notifyItemMoved(fromPosition, toPosition)

            override fun onChanged(position: Int, count: Int) =
                notifyItemRangeChanged(position, count)

            override fun onInserted(position: Int, count: Int) =
                notifyItemRangeInserted(position, count)

            override fun onRemoved(position: Int, count: Int) =
                notifyItemRangeRemoved(position, count)


            override fun compare(o1: Attendance?, o2: Attendance?) =
                o1!!.time_in.compareTo(o2!!.time_in)

            override fun areContentsTheSame(oldItem: Attendance?, newItem: Attendance?) =
                (oldItem!!.lrn == newItem!!.lrn) &&
                        (oldItem.student == newItem.student) &&
                        (oldItem.time_in == newItem.time_in)
        })
    private val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun updateData(attendance: List<Attendance>) {
//        Completable.fromAction { mAttendance.replaceAll(attendance) }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe()

            mAttendance.replaceAll(attendance)

    }


    override fun getItemCount(): Int {
//        return if (mAttendance.size() == 0) 1
//        else mAttendance.size()
        return mAttendance.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceHolder {
//        return if (viewType == ITEM_EMPTY) {
//            val inflatedView = parent.inflate(R.layout.layout_empty_list)
//            EmptyHolder(inflatedView)
//        } else {
        val inflatedView = parent.inflate(R.layout.item_attendance)
        return AttendanceHolder(inflatedView)
//        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mAttendance.size() == 0) ITEM_EMPTY else ITEM_DEFAULT
    }

    class AttendanceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentLogo: CircleImageView = itemView.student_image
        val studentName: TextView = itemView.student_displayName
        val studentLrn: TextView = itemView.student_lrn
        val studentTimeIn: TextView = itemView.label_time_in
    }

//    inner class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onBindViewHolder(holder: AttendanceHolder, position: Int) {
        val currentAttendance = mAttendance[position]

        val student = currentAttendance.student

        holder.studentTimeIn.text =
            clockFormat.format(currentAttendance.time_in.toDate())
        holder.studentLrn.text = student.lrn
        holder.studentName.text = student.displayName

        GlideApp.with(holder.studentLogo)
            .load(R.drawable.img_avatar)
            .into(holder.studentLogo)

    }
}