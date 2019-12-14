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
import android.content.ContentResolver
import android.content.res.AssetManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileSource private constructor() {

    companion object {
        private var instance: FileSource? = null

        fun getInstance(): FileSource {
            instance?.let { return it }
            instance = FileSource()
            return getInstance()
        }
    }

    fun isFileEmpty(app: Application, document: Uri): Boolean {
        return DocumentFile.fromSingleUri(app, document)!!.length() == 0L
    }


    @Throws(FileNotFoundException::class)
    fun getFileOutputStream(contentResolver: ContentResolver, document: Uri): FileOutputStream {

//            contentResolver.openFileDescriptor(document, "rw")?.run {
//                val fileDescriptor = this.fileDescriptor
//                return FileOutputStream(fileDescriptor)
//            }
        return contentResolver.openOutputStream(document) as FileOutputStream
    }

    @Throws(IOException::class)
    fun getTemplateDocument(assets: AssetManager): XWPFDocument {
        ZipSecureFile.setMinInflateRatio(0.0)
        lateinit var template: XWPFDocument
        assets.open("AttendanceSheetTemplate.docx").use { template = XWPFDocument(it) }
        return template
    }
}