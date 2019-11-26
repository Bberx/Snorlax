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

import com.snorlax.snorlax.model.Section

object Constants {

    const val PREFS_KEY = "com.snorlax.snorlax.USER_KEY"
    const val USER_KEY = "user"

    const val GITHUB_URL = "https://github.com/Bberx/Snorlax"

    const val ATTENDANCE_DATABASE_NAME = "list"

    val ACCOUNT_TYPES = listOf("Beadle", "Teacher")
    val SECTION_LIST = mapOf(
        "eco" to Section("Eco", 7),
        "elle" to Section("Elle", 7),
        "jam" to Section("Jam", 7),
        "jyn" to Section("Jyn", 7),
        "mac" to Section("Mac", 7),
        "artz" to Section("Artz", 8),
        "carmz" to Section("Carmz", 8),
        "joanz" to Section("Joanz", 8),
        "linz" to Section("Linz", 8),
        "vearz" to Section("Vearz", 8),
        "charz" to Section("Charz", 9),
        "jazz" to Section("Jazz", 9),
        "kimz" to Section("Kimz", 9),
        "maze" to Section("Maze", 9),
        "salz" to Section("Salz", 9),
        "lloydy" to Section("Lloydy", 10),
        "ly" to Section("Ly", 10),
        "ranz" to Section("Ranz", 10),
        "rose" to Section("Rose", 10),
        "ryl" to Section("Ryl", 10),
        "exactness" to Section("Exactness", 11),
        "generosity" to Section("Generosity", 11),
        "humility" to Section("Humility", 11),
        "resilience" to Section("Resilience", 11),
        "sincerity" to Section("Sincerity", 11),
        "steadfastness" to Section("Steadfastness", 11),
        "excellence" to Section("Excellence", 12),
        "gratitude" to Section("Gratitude", 12),
        "honor" to Section("Honor", 12),
        "respect" to Section("Respect", 12),
        "self_discipline" to Section("Self-Discipline", 12),
        "service" to Section("Service", 12)

    )
}