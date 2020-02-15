package com.snorlax.snorlax.ui.home.generate

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.ui.home.BaseStudentFragment
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentSelectListener
import com.snorlax.snorlax.viewmodel.GeneratorClassViewModel
import kotlinx.android.synthetic.main.fragment_students.*
import kotlinx.android.synthetic.main.fragment_students.view.*
import kotlinx.android.synthetic.main.layout_barcode.*

class GeneratorClassFragment : BaseStudentFragment() {

    override lateinit var viewModel: GeneratorClassViewModel
    override lateinit var adaptor: StudentListAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[GeneratorClassViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.btn_action.setImageResource(R.drawable.ic_save)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        btn_action.setOnClickListener {
            Snackbar.make(requireView(), "Coming soon...", Snackbar.LENGTH_SHORT).show()
        }

        adaptor = StudentListAdaptor(this, true, recyclerOptions, object : StudentSelectListener() {
            override fun onSelectStudent(student: Student) {
                showBarcodeDialog(student.lrn)
            }
        }, callback)
        super.onActivityCreated(savedInstanceState)
    }

    private fun showBarcodeDialog(lrn: String) {
        val barcodeDialog = MaterialAlertDialogBuilder(requireContext())
//            .setIcon(R.drawable.ic_profile)
            .setTitle("LRN Barcode")
            .setView(R.layout.layout_barcode)
            .setNegativeButton(R.string.btn_ok, null)
            .setPositiveButton("Save to device", null)
            .create()

        barcodeDialog.setCanceledOnTouchOutside(false)

        barcodeDialog.setOnShowListener { dialog ->
            val barcodeImage = barcodeDialog.barcode_image
            barcodeDialog.barcode_label.text = lrn

            barcodeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { button ->
                dialog.dismiss()
                Snackbar.make(requireView(), "Coming soon...", Snackbar.LENGTH_SHORT).show()
            }

            barcodeImage.viewTreeObserver.addOnGlobalLayoutListener {
                barcodeImage.setImageBitmap(
                    viewModel.encodeBarcode(
                        lrn,
                        barcodeImage.width,
                        barcodeImage.height
                    )
                )
            }

            barcodeImage.setOnLongClickListener {
                Toast.makeText(requireContext(), "LRN: $lrn", Toast.LENGTH_LONG).show()
                return@setOnLongClickListener true
            }

        }

        barcodeDialog.show()

    }

}
