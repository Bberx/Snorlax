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

package com.snorlax.snorlax.data.files

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.snorlax.snorlax.utils.exception.TemplateNotFoundException
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileSource private constructor(private val application: Application) {

    companion object {
        private var instance: FileSource? = null

        fun getInstance(context: Context): FileSource {
            instance?.let { return it }
            instance = FileSource(context.applicationContext as Application)
            return getInstance(context)
        }
    }

    fun isFileEmpty(document: Uri) =
        DocumentFile.fromSingleUri(application, document)!!.length() == 0L

    fun getFileName(outputLocation: Uri) =
        DocumentFile.fromSingleUri(application, outputLocation)!!.name


    @Throws(FileNotFoundException::class)
    fun getFileOutputStream(document: Uri): FileOutputStream {
        return application.contentResolver.openOutputStream(document) as FileOutputStream

    }


    @Throws(TemplateNotFoundException::class)
    fun getTemplateDocument(): XWPFDocument {
        ZipSecureFile.setMinInflateRatio(0.0)
        var template: XWPFDocument? = null
        try {
            // fixme: set to right template
            application.assets.open("template/AttendanceSheetTemplate.docx").use { template = XWPFDocument(it) }
        } catch (error: IOException) {
            throw TemplateNotFoundException()
        }
        return template ?: throw TemplateNotFoundException()
    }

}

