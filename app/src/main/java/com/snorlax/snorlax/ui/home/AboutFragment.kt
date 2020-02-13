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

package com.snorlax.snorlax.ui.home


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.BuildConfig
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.researcher.ResearcherSource
import com.snorlax.snorlax.utils.Constants.GITHUB_URL
import com.snorlax.snorlax.utils.adapter.recyclerview.AboutAdaptor
import com.snorlax.snorlax.utils.customTab.CustomTabHelper
import com.snorlax.snorlax.utils.inflate
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_about.view.*
import kotlinx.coroutines.delay

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = container.inflate(R.layout.fragment_about)

        rootView.label_app_version.text =
            String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME)

        rootView.team_container.layoutManager = object : LinearLayoutManager(context!!) {
            override fun canScrollVertically() = false
        }
        rootView.team_container.adapter = AboutAdaptor(ResearcherSource.researchers)

        val tabIntent = CustomTabsIntent.Builder().apply {
            setToolbarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
        }.build()

        val customTabHelper = CustomTabHelper()
        val packageName = customTabHelper.getPackageNameToUse(context!!, GITHUB_URL)

        rootView.btn_github.setOnClickListener {
            if (packageName == null) {
                Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)).apply {
                    requireContext().startActivity(this)
                }
            } else {
                tabIntent.intent.setPackage(packageName)
                tabIntent.launchUrl(context!!, Uri.parse(GITHUB_URL))
            }


        }

        rootView.credits_container.layoutManager = object : LinearLayoutManager(context!!) {
            override fun canScrollVertically() = false
        }
        rootView.credits_container.adapter = AboutAdaptor(ResearcherSource.developers)



        return rootView

    }

}
