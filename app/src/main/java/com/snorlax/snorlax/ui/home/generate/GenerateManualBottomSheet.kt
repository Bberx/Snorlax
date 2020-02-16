package com.snorlax.snorlax.ui.home.generate

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.snorlax.snorlax.R
import kotlinx.android.synthetic.main.generator_bottom_sheet.view.*


class GenerateManualBottomSheet(private val callback: GenerateBottomSheetCallback) : BottomSheetDialogFragment() {

//    override fun setupDialog(dialog: Dialog, style: Int) {
//        super.setupDialog(dialog, style)
////        val rootView = View.inflate(requireContext(), R.layout.generate_bottom_sheet, null)
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.generator_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.action_clipboard.setOnClickListener {
            callback.onCopyToClipboard()
            dismissAllowingStateLoss()
        }
        view.action_save_image.setOnClickListener {
            callback.onSaveImage()
            dismissAllowingStateLoss()
        }
    }

    interface GenerateBottomSheetCallback {
        fun onSaveImage()
        fun onCopyToClipboard()
    }
}