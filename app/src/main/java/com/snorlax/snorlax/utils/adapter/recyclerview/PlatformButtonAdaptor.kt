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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.SocialMedia
import com.snorlax.snorlax.model.SocialMedia.SocialPlatform.EMAIL
import com.snorlax.snorlax.model.SocialMedia.SocialPlatform.FACEBOOK
import com.snorlax.snorlax.utils.customTab.CustomTabHelper
import com.snorlax.snorlax.utils.inflate
import kotlinx.android.synthetic.main.button_item.view.*

class PlatformButtonAdaptor(private val socialMedia: List<SocialMedia>) :
    RecyclerView.Adapter<PlatformButtonAdaptor.ButtonHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonHolder {
        val view = parent.inflate(R.layout.button_item)
        return ButtonHolder(view)
    }

    override fun getItemCount() = socialMedia.size

    override fun onBindViewHolder(holder: ButtonHolder, position: Int) {

        val currentPlatform = socialMedia[position]

        val platform = currentPlatform.type
        val socialMediaID = currentPlatform.link

        val url = platform.getPrefix() + socialMediaID

        val tabIntent = CustomTabsIntent.Builder().apply {
            setToolbarColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary))
        }.build()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        val customTabHelper = CustomTabHelper()
        val packageName = customTabHelper.getPackageNameToUse(holder.itemView.context, url)


        val shouldUserCustomTab =
            packageName != null && !customTabHelper.hasSpecializedHandlerIntents(
                holder.itemView.context,
                intent
            )

        holder.platformButton.apply {

            when (platform) {
                EMAIL -> {
                    setOnClickListener {
                        Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse(platform.getPrefix())
                            putExtra(
                                Intent.EXTRA_EMAIL,
                                arrayOf(socialMediaID)
                            )
                            context.startActivity(this)
                        }
                    }
                }
                FACEBOOK -> {
                    setOnClickListener {
                        try {
                            val fbInfo = context.packageManager.getApplicationInfo(
                                "com.facebook.katana",
                                0
                            )
                            if (fbInfo.enabled) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("fb://profile/$socialMediaID")
                                    )
                                )
                            }
                        } catch (fbNotFound: PackageManager.NameNotFoundException) {

                            if (shouldUserCustomTab) {
                                tabIntent.intent.setPackage(packageName)
                                tabIntent.launchUrl(context, Uri.parse(url))
                            } else {
                                context.startActivity(intent)
                            }
//                            Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse(platform.getPrefix() + socialMedia.link)
//                            ).apply {
//                                context.startActivity(this)
//                            }
                        }
                    }
                }
                else -> {
                    setOnClickListener {
                        if (shouldUserCustomTab) {
                            tabIntent.intent.setPackage(packageName)
                            tabIntent.launchUrl(context, Uri.parse(url))
                        } else {
                            context.startActivity(intent)
                        }


//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse(platform.getPrefix() + socialMedia.link)
//                        ).apply {
//                            context.startActivity(this)
//                        }
                    }
                }

            }
            maxLines = 1
            text = platform.getNameDisplay()
            icon = ContextCompat.getDrawable(context, platform.getIconId())
        }
    }

    inner class ButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var platformButton: MaterialButton = itemView.platform_button
    }
}