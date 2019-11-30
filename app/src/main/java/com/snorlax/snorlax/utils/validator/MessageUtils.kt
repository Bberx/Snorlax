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

package com.snorlax.snorlax.utils.validator

import com.snorlax.snorlax.utils.validator.FormResult.Message.Error.*
import com.snorlax.snorlax.utils.validator.FormResult.Message.Item
import com.snorlax.snorlax.utils.validator.FormResult.Message.Item.*
import com.snorlax.snorlax.utils.validator.FormResult.Message.MessageType
import com.snorlax.snorlax.utils.validator.FormResult.Message.Valid.*

fun MessageType.getErrorMessage(): String? =
    when (this) {
        PASSWORD_DO_NOT_MATCH -> "Passwords do not match"
        PASSWORD_TOO_SHORT -> "Password is too short"
        EMAIL_FORMAT_ERROR -> "Invalid email address"
        FIRST_NAME_INVALID -> "Please enter a valid first displayName"
        LAST_NAME_INVALID -> "Please enter a valid last displayName"
        BOTH_NAMES_INVALID -> "Please enter a valid displayName"
        EMAIL_ALREADY_TAKEN -> "Email already taken"
        ACC_TYPE_INVALID -> "Please select from choices"
        SECTION_INVALID -> "Please select from choices"
        else -> null
    }

fun MessageType.getMessageItem(): Item? =
    when (this) {
        FIRST_NAME_INVALID -> FIRST_NAME
        LAST_NAME_INVALID -> LAST_NAME
        EMAIL_FORMAT_ERROR -> EMAIL
        PASSWORD_DO_NOT_MATCH -> PASSWORD_CONFIRM
        PASSWORD_TOO_SHORT -> PASSWORD
        ACC_TYPE_INVALID -> ACC_TYPE
        BOTH_NAMES_INVALID -> FIRST_NAME
        EMAIL_ALREADY_TAKEN -> EMAIL
        EMAIL_VALID -> EMAIL
        PASSWORD_VALID -> PASSWORD
        FIRST_NAME_VALID -> FIRST_NAME
        LAST_NAME_VALID -> LAST_NAME
        SECTION_VALID -> SECTION
        ACC_TYPE_VALID -> ACC_TYPE
        PASSWORD_CONFIRM_VALID -> PASSWORD_CONFIRM
        SECTION_INVALID -> SECTION
        else -> null
    }