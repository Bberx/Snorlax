package com.snorlax.snorlax.ui.home.generate

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.BarcodeBitmap
import com.snorlax.snorlax.model.Student
import com.snorlax.snorlax.ui.home.BaseStudentFragment
import com.snorlax.snorlax.utils.Constants
import com.snorlax.snorlax.utils.adapter.recyclerview.StudentListAdaptor
import com.snorlax.snorlax.utils.callback.StudentSelectListener
import com.snorlax.snorlax.utils.exception.TemplateNotFoundException
import com.snorlax.snorlax.viewmodel.GeneratorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.diag_barcode.*
import kotlinx.android.synthetic.main.fragment_students.*
import kotlinx.android.synthetic.main.fragment_students.view.*
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class GeneratorClassFragment : BaseStudentFragment() {


    override lateinit var viewModel: GeneratorViewModel
    override lateinit var adaptor: StudentListAdaptor

    private val classDisposable = CompositeDisposable()
    private val saveBarcodeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[GeneratorViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.btn_action.setImageResource(R.drawable.ic_save)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        btn_action.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                val fileName =
                    "Barcode_List-${Constants.SECTION_LIST.getValue(viewModel.section).display_name}.docx"
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                putExtra(Intent.EXTRA_TITLE, fileName)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }
            startActivityForResult(intent, GeneratorFragment.REQUEST_SAVE_DOCX)
        }

        adaptor = StudentListAdaptor(this, true, recyclerOptions, object : StudentSelectListener() {
            override fun onSelectStudent(student: Student) {
                showBarcodeDialog(student.lrn, student.name.getValue(Student.LAST_NAME_VAL))
            }
        }, callback)
        super.onActivityCreated(savedInstanceState)
    }

    private fun showBarcodeDialog(lrn: String, name: String) {
        val barcodeDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.label_lrn_barcode)
            .setView(R.layout.diag_barcode)
            .setNegativeButton(R.string.btn_ok, null)
            .setPositiveButton(R.string.action_save_image, null)
            .create()

        barcodeDialog.setCanceledOnTouchOutside(false)


        barcodeDialog.setOnShowListener { dialog ->
            classDisposable += viewModel.barcodeBitmapClassObservable.subscribe {
                barcodeDialog.barcode_image.setImageBitmap(it.bitmap)
                barcodeDialog.barcode_label.text = it.value
            }
            viewModel.encodeBarcodeClass(lrn)

            barcodeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { button ->
                button.isEnabled = false
                startActivityForResult(
                    viewModel.getSaveImageIntent(lrn, name),
                    GeneratorFragment.REQUEST_SAVE_IMAGE
                )
                dialog.dismiss()
            }

            barcodeDialog.barcode_image.setOnLongClickListener {
                viewModel.saveToClipboard(lrn)
                Toast.makeText(requireContext(), R.string.msg_lrn_copied, Toast.LENGTH_LONG).show()
                return@setOnLongClickListener true
            }
        }
        barcodeDialog.show()
    }

    private fun showSuccessResult(message: String, location: Uri, isDocx: Boolean) {
        view?.let {
            val snackbar = Snackbar.make(it, message, 3000)
            val intent = if (isDocx) {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        location,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        location,
                        "image/png"
                    )
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
            }

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                snackbar.setAction(getString(R.string.btn_open)) {
                    startActivity(
                        Intent.createChooser(
                            intent,
                            if (isDocx) getString(R.string.act_open_attendance)
                            else getString(R.string.act_open_attendance)
                        )
                    )
                }.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.alertColor))
                    .show()
            } else {
                snackbar.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GeneratorFragment.REQUEST_SAVE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let { intent ->
                        intent.data?.let { dataUri ->
                            saveImage(dataUri, viewModel.barcodeBitmapClassObservable.value!!)
                        }
                    }
                }
            }
            GeneratorFragment.REQUEST_SAVE_DOCX -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let { intent ->
                        intent.data?.let { dataUri ->
                            saveClassBarcode(dataUri)
                        }
                    }
                }
            }
        }
    }

    private fun saveClassBarcode(outputLocation: Uri) {
        saveBarcodeDisposable += viewModel.saveClassBarcode(outputLocation)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                showSuccessResult(
                    getString(
                        R.string.msg_image_saved,
                        viewModel.outputFileName(outputLocation)
                    ), outputLocation, true
                )
//                showResult(
//                    getString(
//                        R.string.msg_image_saved,
//                        viewModel.outputFileName(outputLocation)
//                    )
//                )
            }, onError = {
                when (it) {
                    is TemplateNotFoundException -> showResult(getString(R.string.err_template_not_found))

                    is FileNotFoundException -> showResult(getString(R.string.err_file_not_found))

                    is IOException -> showResult(
                        getString(
                            R.string.err_io_exception,
                            it.localizedMessage ?: "unknown error occurred."
                        )
                    )

                    is NoSuchElementException -> showResult(getString(R.string.err_unknown_section))

                    else -> showResult(
                        getString(
                            R.string.err_unknown,
                            it.localizedMessage ?: "no error message."
                        ), TimeUnit.SECONDS.toMillis(3).toInt()
                    )
                }
                if (viewModel.isEmpty(outputLocation)) viewModel.deleteFile(outputLocation)
            })
    }

    private fun saveImage(location: Uri, barcode: BarcodeBitmap) {
        saveBarcodeDisposable += viewModel.saveImage(barcode, location)
            .doOnDispose { viewModel.deleteFile(location) }
            .unsubscribeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    showSuccessResult(
                        getString(
                            R.string.msg_image_saved,
                            viewModel.outputFileName(location)
                        ), location, false
                    )
                }, onError = {
                    when (it) {
                        is FileNotFoundException -> showResult(getString(R.string.err_file_not_found))
                        is IOException -> showResult(
                            getString(
                                R.string.err_io_exception,
                                it.localizedMessage ?: "unknown error occurred."
                            )
                        )
                        else -> showResult(
                            getString(
                                R.string.err_unknown,
                                it.localizedMessage ?: "no error message."
                            ), TimeUnit.SECONDS.toMillis(3).toInt()
                        )
                    }
                    if (viewModel.isEmpty(location)) viewModel.deleteFile(location)
                }
            )
    }

    private fun showResult(message: String, length: Int = Snackbar.LENGTH_SHORT) {
        view?.let {
            Snackbar.make(it, message, length).show()
        }
    }

    override fun onPause() {
        super.onPause()
        classDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveBarcodeDisposable.clear()
    }

}
