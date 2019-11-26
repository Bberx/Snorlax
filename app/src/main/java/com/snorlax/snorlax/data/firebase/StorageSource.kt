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

package com.snorlax.snorlax.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class StorageSource private constructor() {

    companion object {
        val instance = StorageSource()
    }

    private val firestoreStorage = FirebaseStorage.getInstance().reference

    val defaultAvatarURL = Uri.parse("https://firebasestorage.googleapis.com/v0/b/attendance-scanner-snorlax.appspot.com/o/users%2Fdefault%2Fdefault_avatar.jpg?alt=media&token=5002a14e-6efb-4f74-bd5b-e5e7ef022666")

}