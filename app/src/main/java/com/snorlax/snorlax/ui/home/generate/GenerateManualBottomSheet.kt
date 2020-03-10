package com.snorlax.snorlax.ui.home.generate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.snorlax.snorlax.R
import com.snorlax.snorlax.viewmodel.GeneratorViewModel
import kotlinx.android.synthetic.main.generator_bottom_sheet.view.*


class GenerateManualBottomSheet() : BottomSheetDialogFragment() {

    //    override fun setupDialog(dialog: Dialog, style: Int) {
//        super.setupDialog(dialog, style)
////        val rootView = View.inflate(requireContext(), R.layout.generate_bottom_sheet, null)
//    }
    private lateinit var viewModel: GeneratorViewModel

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
            viewModel.bottomSheetObservable.onNext(BottomSheetAction.COPY_BARCODE)
//            callback.onCopyToClipboard()
            dismissAllowingStateLoss()
        }
        view.action_save_image.setOnClickListener {
            viewModel.bottomSheetObservable.onNext(BottomSheetAction.SAVE_IMAGE)
            dismissAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(requireParentFragment().requireParentFragment())[GeneratorViewModel::class.java]
    }

    enum class BottomSheetAction {
        SAVE_IMAGE, COPY_BARCODE
    }
}