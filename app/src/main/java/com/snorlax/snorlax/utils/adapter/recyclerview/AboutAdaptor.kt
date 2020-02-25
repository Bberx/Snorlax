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
import androidx.recyclerview.widget.RecyclerView
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Researcher
import com.snorlax.snorlax.utils.glide.GlideApp
import com.snorlax.snorlax.utils.inflate
import com.snorlax.snorlax.views.ResearcherDialog
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_researcher.*

class AboutAdaptor(private val researcher: List<Researcher>) :
    RecyclerView.Adapter<AboutAdaptor.AboutHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutHolder {
        val rootView = parent.inflate(R.layout.item_researcher)
        return AboutHolder(rootView)
    }

    override fun getItemCount() = researcher.size


    override fun onBindViewHolder(holder: AboutHolder, position: Int) {
        holder.bind(researcher[position])
    }

    class AboutHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(item: Researcher) {
            GlideApp
                .with(containerView.context)
                .load(item.displayImage)
//            .apply(RequestOptions.bitmapTransform(CropCircleWithBorderTransformation()))
//            .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.img_avatar)
                .into(researcher_image)

            researcher_displayName.text = item.name
            researcher_role.text = item.role

            containerView.setOnClickListener {
                ResearcherDialog(containerView.context, item).show()
            }
        }


//            val image: CircleImageView = researcher_image
//            val displayName: TextView = researcher_displayName
//            val role: TextView = researcher_role


    }
}