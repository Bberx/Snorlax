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

class FormResult(builder: Builder) {

    val results: ArrayList<Result> = builder.resultTemp


    data class Result(val message: Message.MessageType)

    class Builder {

        val resultTemp = ArrayList<Result>()

        fun addResult(result: Result): Builder {
            resultTemp.add(result)
            return this
        }

        fun build(): FormResult {
            return FormResult(this)
        }
    }

    fun isOverallSuccess(): Boolean {
        for (result in results) {
            if (result.message is Message.Error) {
                return false
            }
        }
        return true
    }

//    fun getErrors(): ArrayList<Result>? {
//        val errorList = arrayListOf<Result>()
//        for (error in results) {
//            if (error.message is Message.Error)
//                errorList.add(error)
//        }
//        return if (errorList.size == 0) null
//        else errorList
//    }

    class Message {

        interface MessageType

        enum class Valid : MessageType {
            EMAIL_VALID,
            PASSWORD_VALID,
            PASSWORD_CONFIRM_VALID,
            FIRST_NAME_VALID,
            LAST_NAME_VALID,
            SECTION_VALID,
            ACC_TYPE_VALID;
        }

        enum class Error : MessageType {
            PASSWORD_DO_NOT_MATCH,
            PASSWORD_TOO_SHORT,
            EMAIL_FORMAT_ERROR,
            FIRST_NAME_INVALID,
            LAST_NAME_INVALID,
            BOTH_NAMES_INVALID,
            EMAIL_ALREADY_TAKEN,
            SECTION_INVALID,
            ACC_TYPE_INVALID
        }

        enum class Item { EMAIL, PASSWORD, PASSWORD_CONFIRM, FIRST_NAME, LAST_NAME, ACC_TYPE, SECTION }
    }
}


