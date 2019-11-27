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
import androidx.recyclerview.widget.RecyclerView
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.utils.inflate
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_student_scan.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StudentScannerAdaptor(private val context: Context) :
    RecyclerView.Adapter<StudentScannerAdaptor.StudentHolder>() {

    val studentList: ArrayList<Pair<Student, Long>> = ArrayList()

    private val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val inflatedView = parent.inflate(R.layout.item_student_scan)
        return StudentHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        val currentStudent = studentList[position]
        holder.displayName.text = currentStudent.first.displayName
        holder.lrn.text = currentStudent.first.lrn
        holder.timeIn.text = clockFormat.format(currentStudent.second)
        holder.image.setImageResource(R.mipmap.ic_launcher)

    }

    inner class StudentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val displayName: TextView = itemView.student_displayName
        val lrn: TextView = itemView.student_lrn
        val image: CircleImageView = itemView.student_image
        val timeIn: TextView = itemView.student_timeIn

    }

}


