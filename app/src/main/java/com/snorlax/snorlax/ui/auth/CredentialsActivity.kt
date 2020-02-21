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
import com.snorlax.snorlax.R
import com.snorlax.snorlax.data.firebase.FirebaseAuthSource
import com.snorlax.snorlax.utils.adapter.framepager.AuthenticationViewPagerAdapter
import com.snorlax.snorlax.utils.startHomeActivity
import kotlinx.android.synthetic.main.activity_credentials.*


class CredentialsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

//        disposables.add(FirebaseAuthSource.getInstance().loggedOut()
//            .subscribeOn(Schedulers.io())
//            .observeOn(Schedulers.io())
//            .subscribe {
//                if (it == false) startHomeActivity()
//            }
//        )
        FirebaseAuthSource.currentUser()?.let {
            startHomeActivity()
            super.onCreate(savedInstanceState)
            return
        }
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credentials)

        view_pager.adapter = AuthenticationViewPagerAdapter(this, supportFragmentManager)
        tabs.setupWithViewPager(view_pager)
//        view_pager.adapter = AuthenticationPageAdapter(this)
//        tabs.setupWithViewPager(view_pager)
//        TabLayoutMediator(tabs, view_pager) { tab: TabLayout.Tab, index: Int ->
//            tab.text = when (index) {
//                0 -> getString(R.string.act_log_in)
//                1 -> getString(R.string.act_register)
//                else -> throw IllegalStateException("Invalid page index")
//            }
//        }.attach()
    }



}
