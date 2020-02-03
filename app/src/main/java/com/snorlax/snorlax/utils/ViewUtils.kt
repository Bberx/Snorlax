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

package com.snorlax.snorlax.utils

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.snorlax.snorlax.ui.auth.CredentialsActivity
import com.snorlax.snorlax.ui.home.HomeActivity


//fun Activity.startHomeActivity(): Disposable =
//
//    firestoreSource.getAdmin(userRepository.currentUser()!!.uid).subscribe{
//        (application as SnorlaxApp).currentAdmin = it
//        Intent(this, HomeActivity::class.java).also {intent ->
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
//        }
//    }

fun Activity.exitApp() {
    val homeIntent = Intent(Intent.ACTION_MAIN).also {
        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        it.addCategory(Intent.CATEGORY_HOME)
    }
    startActivity(homeIntent)
//    exitProcess(0)
}

fun Activity.startHomeActivity() {
    val intent = Intent(this, HomeActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finishAndRemoveTask()
}


fun Activity.startLoginActivity() {
    val intent = Intent(this, CredentialsActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

//        finish()
    }
    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finishAndRemoveTask()
}


fun ViewGroup?.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this?.context).inflate(layoutRes, this, attachToRoot)

//fun ViewGroup?.inflate(view: View, attachToRoot: Boolean = false): View {
//    return LayoutInflater.from(this?.context).i
//}
