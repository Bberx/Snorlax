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

package com.snorlax.snorlax.data.researcher

import com.google.firebase.storage.FirebaseStorage
import com.snorlax.snorlax.model.Researcher
import com.snorlax.snorlax.model.SocialMedia
import com.snorlax.snorlax.model.SocialMedia.SocialPlatform.*

/**
 * Researcher source, note that facebook is base on profile id not on username because it is static and not changeable
 */
object ResearcherSource {

    /**
     *  Firebase storage researcher image reference
     */
    private val firebaseStorage = FirebaseStorage.getInstance().reference.child("researchers/display_image/")

    /**
     *  List of developers, note it is separated so as to be put on another recyclerview
     */
    val developers = listOf(
        Researcher(
            "Oliver Rhyme G. Añasco",
            "App Developer",
            "Simple random coder",
            firebaseStorage.child("oliver.jpg"),
            listOf(
                SocialMedia(EMAIL, "oliveranasco@gmail.com"),
                SocialMedia(FACEBOOK, "100003776636938"),
                SocialMedia(TWITTER, "Oliver_Rhyme"),
                SocialMedia(INSTAGRAM, "justme_oliver")
            )
        )
    )

    /**
     *  List of researchers
     */
    val researchers = listOf(
        Researcher(
            "Dollrainple Gwyneth B. Dabalos",
            "Researcher",
            "Sun love rain and vice versa",
            firebaseStorage.child("dollrainple.jpg"),
            listOf(
                SocialMedia(EMAIL, "dgwyneth0623@gmail.com"),
                SocialMedia(FACEBOOK, "100007328164500"),
                SocialMedia(TWITTER, "gwynethdabalos"),
                SocialMedia(INSTAGRAM, "gwynethdabalos")
            )
        ),
        Researcher(
            "Trisha Kryzyl E. Madriaga",
            "Researcher",
            "I like warm hugs",
            firebaseStorage.child("trisha.jpg"),
            listOf(
                SocialMedia(EMAIL, "trshxkryzyl.ie@gmail.com"),
                SocialMedia(FACEBOOK, "100001111092899"),
                SocialMedia(TWITTER, "trshxkryzyl")
            )
        ),
        Researcher(
            "Abegail D. Borces",
            "Researcher",
            "Life is short. If you fail then learn, stand and move on",
            firebaseStorage.child("abegail.jpg"),
            listOf(
                SocialMedia(EMAIL, "borces.abegail.d@gmail.com"),
                SocialMedia(FACEBOOK, "100006557435370"),
                SocialMedia(TWITTER, "BORSATCHE"),
                SocialMedia(INSTAGRAM, "abegail.d.b")
            )
        ),
        Researcher(
            "Maria Isabel Jost T. Souribio",
            "Researcher",
            "We should be the change that this world needs.",
            firebaseStorage.child("maria.jpeg"),
            listOf(
                SocialMedia(EMAIL, "isabelsouribio@gmail.com"),
                SocialMedia(FACEBOOK, "100003596902195"),
                SocialMedia(TWITTER, "hiimsabss"),
                SocialMedia(INSTAGRAM, "siobelaaaaa")
            )
        ),
        Researcher(
            "Daniela Julia L. Ranara",
            "Researcher",
            "Sic Parvis Magna: Greatness from small beginnings",
            firebaseStorage.child("daniela.jpg"),
            listOf(
                SocialMedia(EMAIL, "juliaranara08@gmail.com"),
                SocialMedia(FACEBOOK, "100001495348080"),
                SocialMedia(TWITTER, "danielairvx"),
                SocialMedia(INSTAGRAM, "danielantcx")
            )
        )
    )
}