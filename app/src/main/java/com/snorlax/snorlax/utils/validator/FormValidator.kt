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

import android.util.Patterns
import com.snorlax.snorlax.utils.Constants.ACCOUNT_TYPES
import com.snorlax.snorlax.utils.Constants.SECTION_LIST
import com.snorlax.snorlax.utils.validator.FormResult.Message.Error.*
import com.snorlax.snorlax.utils.validator.FormResult.Message.Valid.*

class FormValidator {
    companion object {
//        fun isValidLoginForm(loginForm: Form.LoginForm): FormResult {
//            return FormResult.Builder()
//                    .addResult(isValidEmail(loginForm.email))
//                    .addResult(isValidPassword(loginForm.password))
//                    .build()
//        }
//
//        fun isValidRegisterForm(registerForm: Form.RegisterForm): FormResult {
//            return FormResult.Builder()
//                    .addResult(isValidEmail(registerForm.email))
//                    .addResult(isValidPassword(registerForm.password, registerForm.passwordConfirm))
//                    .addResult(isValidName(registerForm.firstName, registerForm.lastName))
//                    .addResult(isValidAccType(registerForm.accountType))
//                    .build()
//        }

        fun isValidEmail(email: String): FormResult.Result {
            return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                FormResult.Result(EMAIL_VALID)
            } else {
                FormResult.Result(EMAIL_FORMAT_ERROR)
            }
        }

        fun isValidPassword(password: String): FormResult.Result {
            return if (password.length < 6)
                FormResult.Result(PASSWORD_TOO_SHORT)
            else
                FormResult.Result(PASSWORD_VALID)
        }

        fun isValidPasswordConfirm(password: String, confirmPassword: String): FormResult.Result {
            return if (password.contentEquals(confirmPassword))
                FormResult.Result(PASSWORD_CONFIRM_VALID)
            else FormResult.Result(PASSWORD_DO_NOT_MATCH)

        }

        fun isValidFirstName(firstName: String): FormResult.Result {
            return if (firstName.isEmpty()) FormResult.Result(FIRST_NAME_INVALID)
            else FormResult.Result(FIRST_NAME_VALID)
        }

        fun isValidLastName(lastName: String): FormResult.Result {
            return if (lastName.isEmpty()) FormResult.Result(LAST_NAME_INVALID)
            else FormResult.Result(LAST_NAME_VALID)
        }

        fun isValidSection(sectionRaw: String): FormResult.Result {
            val sectionList = SECTION_LIST.map { it.value.display_name }
            val section = sectionRaw.substring(sectionRaw.lastIndexOf(" ") + 1, sectionRaw.length)

            return if (section in sectionList) FormResult.Result(SECTION_VALID)
            else FormResult.Result(SECTION_INVALID)
        }

        fun isValidAccType(accRaw: String): FormResult.Result {
            return if (accRaw in ACCOUNT_TYPES) FormResult.Result(ACC_TYPE_VALID)
            else FormResult.Result(ACC_TYPE_INVALID)
        }
    }
}



