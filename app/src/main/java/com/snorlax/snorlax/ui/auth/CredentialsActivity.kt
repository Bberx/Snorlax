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

package com.snorlax.snorlax.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.cache.LocalCacheSource
import com.snorlax.snorlax.utils.adapter.framepager.AuthenticationPageAdapter
import com.snorlax.snorlax.utils.startHomeActivity
import kotlinx.android.synthetic.main.activity_credentials.*


class CredentialsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_credentials)

        view_pager.adapter = AuthenticationPageAdapter(
            this,
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        tabs.setupWithViewPager(view_pager)
    }


    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().currentUser?.let {
            LocalCacheSource.getInstance().getUserCache(this)?.let {
                startHomeActivity()
            } ?: FirebaseAuth.getInstance().signOut()
        }
    }
}
