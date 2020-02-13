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

package com.snorlax.snorlax.utils.adapter.framepager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.snorlax.snorlax.ui.auth.LoginFragment
import com.snorlax.snorlax.ui.auth.RegisterFragment

class AuthenticationPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
//    override fun getItem(position: Int): Fragment {
//        return when (position) {
//            0 -> LoginFragment()
//            1 -> RegisterFragment()
//            else -> null!!
//        }
//    }

    override fun getItemCount() = 2

//    override fun getPageTitle(position: Int): CharSequence =
//        when (position) {
//            0 -> mContext.resources.getString(R.string.act_log_in)
//            else -> mContext.resources.getString(R.string.act_register)
//        }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LoginFragment()
            1 -> RegisterFragment()
            else -> throw IllegalStateException("Invalid page position")
        }
    }
}