package com.snorlax.snorlax.ui.home.generate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.text.isDigitsOnly
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import com.snorlax.snorlax.R
import com.snorlax.snorlax.model.BarcodeBitmap
import com.snorlax.snorlax.viewmodel.GeneratorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_generator_manual.*
import kotlinx.android.synthetic.main.fragment_generator_manual.view.*
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit


class GeneratorManualFragment : Fragment() {

    private lateinit var viewModel: GeneratorViewModel
    private val barcodeDisposable = CompositeDisposable()

    private val manualDisposable = CompositeDisposable()
    private val saveDisposable = CompositeDisposable()


    private val drawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) {}

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

        override fun onDrawerClosed(drawerView: View) {}

        override fun onDrawerOpened(drawerView: View) {
            hideKeyboard()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generator_manual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.btn_generate.setOnClickListener {
            val lrnText = view.input_generate.text?.toString()
            lrnError(lrnText).let { error ->
                text_layout_generate.error = error
                if (error == null) viewModel.encodeBarcodeManual(lrnText!!)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[GeneratorViewModel::class.java]

        activity?.drawer_layout?.addDrawerListener(drawerListener)

        input_generate.textChanges().map {
            it.length == 12 && it.isDigitsOnly() && it.isNotBlank() && it.isNotEmpty()
        }.distinctUntilChanged().subscribe(viewModel.buttonObservable)

        val callback = object : GenerateManualBottomSheet.GenerateBottomSheetCallback {
            override fun onSaveImage() {
                startActivityForResult(
                    viewModel.getSaveImageIntent(viewModel.barcodeBitmapManualObservable.value!!.value),
                    GeneratorFragment.REQUEST_SAVE_IMAGE
                )
            }

            override fun onCopyToClipboard() {
                viewModel.saveToClipboard(viewModel.barcodeBitmapManualObservable.value!!.value)
                showResult(getString(R.string.msg_lrn_copied))
            }

        }

        image_generate.setOnClickListener {
            if (viewModel.barcodeBitmapManualObservable.hasValue()) {
                hideKeyboard()
                val bottomSheet = GenerateManualBottomSheet(callback)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }
    }

    private fun lrnError(lrn: String?): String? {
        return if (lrn.isNullOrBlank() || lrn.isNullOrEmpty()) getString(R.string.err_no_lrn)
        else {
            if (lrn.length == 12 && lrn.isDigitsOnly()) null
            else getString(R.string.err_invalid_lrn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GeneratorFragment.REQUEST_SAVE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let { intent ->
                        intent.data?.let { dataUri ->
                            saveImage(dataUri, viewModel.barcodeBitmapManualObservable.value!!)
                        }
                    }
                }
            }
        }
    }

    private fun saveImage(location: Uri, barcode: BarcodeBitmap) {
        saveDisposable += viewModel.saveImage(barcode, location)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    showResult(
                        getString(
                            R.string.msg_image_saved,
                            viewModel.outputFileName(location)
                        )
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

    override fun onResume() {
        super.onResume()
        barcodeDisposable += viewModel.barcodeBitmapManualObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                image_generate.setImageBitmap(it.bitmap)
                barcode_label.text = it.value
            }
        barcodeDisposable += viewModel.buttonObservable.subscribe {
            btn_generate.isEnabled = it
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeDisposable.clear()
        manualDisposable.clear()
        hideKeyboard()
        activity?.drawer_layout?.removeDrawerListener(drawerListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        saveDisposable.clear()
    }

    private fun showResult(message: String, length: Int = Snackbar.LENGTH_SHORT) {
        view?.let {
            Snackbar.make(it, message, length).show()
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(requireView().windowToken, 0)
        requireView().clearFocus()
    }
}
