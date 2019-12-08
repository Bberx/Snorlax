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

package com.snorlax.snorlax

import android.app.Application

class BeabotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLStreamReader",
            "com.fasterxml.aalto.stax.StreamReaderImpl"
        )
    }
//
//    companion object {
//        init {
//            System.setProperty(
//                "org.apache.poi.javax.xml.stream.XMLInputFactory",
//                "com.fasterxml.aalto.stax.InputFactoryImpl"
//            )
//            System.setProperty(
//                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
//                "com.fasterxml.aalto.stax.OutputFactoryImpl"
//            )
//            System.setProperty(
//                "org.apache.poi.javax.xml.stream.XMLEventFactory",
//                "com.fasterxml.aalto.stax.EventFactoryImpl"
//            )
//            System.setProperty(
//                "org.apache.poi.javax.xml.stream.XMLStreamReader",
//                "com.fasterxml.aalto.stax.StreamReaderImpl"
//            )
//        }
//    }
}