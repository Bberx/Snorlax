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

package com.snorlax.snorlax.views

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.snorlax.snorlax.R
import com.snorlax.snorlax.viewmodel.ExportViewModel

class ExportDialog : DialogFragment() {
    companion object {
        private const val CREATE_DOCUMENT = 5
        private const val SELECT_FOLDER = 6
    }

    private lateinit var viewModel: ExportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[ExportViewModel::class.java]

//        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//

    }

    // TODO Select folder
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setTitle("Export attendance list")
            .setIcon(R.drawable.ic_save)
            .setMessage(getString(R.string.msg_export, getString(R.string.app_name)))
            .setPositiveButton(R.string.btn_ok) { _: DialogInterface, _: Int ->
                val fileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    // TODO rename me
                    putExtra(Intent.EXTRA_TITLE, "AttendanceSheet.docx")

                }
                startActivityForResult(fileIntent, SELECT_FOLDER)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

    }
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = container.inflate(R.layout.diag_export)
//        return rootView
//    }

}