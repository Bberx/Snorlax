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

import com.snorlax.snorlax.model.LateData
import com.snorlax.snorlax.model.Section

object Constants {

    const val PREFS_KEY = "com.snorlax.snorlax.USER_KEY"
    const val USER_KEY = "user"

    const val GITHUB_URL = "https://github.com/Bberx/Snorlax"

    val ACCOUNT_TYPES = listOf("Beadle", "Teacher")
    val SECTION_LIST = mapOf(
        "eco" to Section("Eco", 7, LateData(27300)),
        "elle" to Section("Elle", 7, LateData(27300)),
        "jam" to Section("Jam", 7, LateData(27300)),
        "jyn" to Section("Jyn", 7, LateData(27300)),
        "mac" to Section("Mac", 7, LateData(27300)),
        "artz" to Section("Artz", 8, LateData(27300)),
        "carmz" to Section("Carmz", 8, LateData(27300)),
        "joanz" to Section("Joanz", 8, LateData(27300)),
        "linz" to Section("Linz", 8, LateData(27300)),
        "vearz" to Section("Vearz", 8, LateData(27300)),
        "charz" to Section("Charz", 9, LateData(27300)),
        "jazz" to Section("Jazz", 9, LateData(27300)),
        "kimz" to Section("Kimz", 9, LateData(27300)),
        "maze" to Section("Maze", 9, LateData(27300)),
        "salz" to Section("Salz", 9, LateData(27300)),
        "lloydy" to Section("Lloydy", 10, LateData(27300)),
        "ly" to Section("Ly", 10, LateData(27300)),
        "ranz" to Section("Ranz", 10, LateData(27300)),
        "rose" to Section("Rose", 10, LateData(27300)),
        "ryl" to Section("Ryl", 10, LateData(27300)),
        "exactness" to Section("Exactness", 11, LateData(27300)),
        "generosity" to Section("Generosity", 11, LateData(27300)),
        "humility" to Section("Humility", 11, LateData(27300)),
        "resilience" to Section("Resilience", 11, LateData(27300)),
        "sincerity" to Section("Sincerity", 11, LateData(27300)),
        "steadfastness" to Section("Steadfastness", 11, LateData(27300)),
        "excellence" to Section("Excellence", 12, LateData(27300)),
        "gratitude" to Section("Gratitude", 12, LateData(27300)),
        "honor" to Section("Honor", 12, LateData(27300)),
        "respect" to Section("Respect", 12, LateData(27300)),
        "self_discipline" to Section("Self-Discipline", 12, LateData(27300)),
        "service" to Section("Service", 12, LateData(27300))

    )
}