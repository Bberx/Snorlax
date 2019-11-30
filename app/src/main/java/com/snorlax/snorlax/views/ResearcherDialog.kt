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

package com.snorlax.snorlax.views

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Researcher
import com.snorlax.snorlax.utils.adapter.recyclerview.PlatformButtonAdaptor
import com.snorlax.snorlax.utils.glide.GlideApp
import kotlinx.android.synthetic.main.diag_researcher_layout.*


class ResearcherDialog(context: Context, private val researcher: Researcher) :
    AlertDialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.diag_researcher_layout)

        GlideApp.with(context)
            .load(researcher.displayImage)
            .placeholder(R.drawable.img_avatar)
//            .transition(DrawableTransitionOptions.withCrossFade())
            .into(researcher_image)

        researcher_name.text = researcher.name
        researcher_role.text = researcher.role
        researcher.bio?.let {
            researcher_bio.text = it
        } ?: run {
            researcher_bio.visibility = View.GONE
        }




        buttonRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//        buttonRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        buttonRecycler.adapter = PlatformButtonAdaptor(researcher.socialMedia)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}