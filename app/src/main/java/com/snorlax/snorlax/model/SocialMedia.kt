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

package com.snorlax.snorlax.model

import com.snorlax.snorlax.R

data class SocialMedia(val type: SocialPlatform, val link: String) {
    enum class SocialPlatform {
        EMAIL, FACEBOOK, INSTAGRAM, TWITTER;

        fun getNameDisplay() : String {
            return when (this) {
                EMAIL -> "Email"
                FACEBOOK -> "Facebook"
                INSTAGRAM -> "Instagram"
                TWITTER -> "Twitter"
            }
        }

        fun getPrefix() : String {
            return when (this) {
                EMAIL -> "mailto:"
                FACEBOOK -> "https://www.facebook.com/profile.php?id="
                INSTAGRAM -> "https://instagram.com/"
                TWITTER -> "https://twitter.com/"
            }
        }

        fun getIconId(): Int {
            return when (this) {
                EMAIL -> R.drawable.ic_email
                FACEBOOK -> R.drawable.ic_facebook
                INSTAGRAM -> R.drawable.ic_instagram
                TWITTER -> R.drawable.ic_twitter
            }
        }
    }
}