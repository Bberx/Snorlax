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

package com.snorlax.snorlax.viewmodel

import android.app.Application
import android.content.res.AssetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.IOException

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val mFileDestination = MutableLiveData<String>()

    val fileDestination: LiveData<String>
        get() = mFileDestination

    @Throws(IOException::class)
    fun getTemplateFile(): XWPFDocument {
        try {
            val inputStream = getApplication<Application>().assets.open(
                "AttendanceSheetTemplate.docx",
                AssetManager.ACCESS_RANDOM
            )
            return XWPFDocument(inputStream)
        } catch (exception: IOException) {
            throw exception
        }
    }

    fun selectFile() {

    }

}