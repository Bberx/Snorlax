package com.snorlax.snorlax.ui.home.generate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.snorlax.snorlax.R
import com.snorlax.snorlax.viewmodel.GeneratorManualViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_generator_manual.*
import kotlinx.android.synthetic.main.fragment_generator_manual.view.*

class GeneratorManualFragment : Fragment() {

    private lateinit var viewModel: GeneratorManualViewModel
    private val barcodeDisposable = CompositeDisposable()

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
                if (error == null) viewModel.updateBarcode(lrnText!!)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GeneratorManualViewModel::class.java]
        // TODO: Use the ViewModel
    }

    private fun lrnError(lrn: String?): String? {
        return if (lrn.isNullOrBlank() || lrn.isNullOrEmpty()) "Please input the LRN to generate"
        else {
            if (lrn.length == 12 && lrn.isDigitsOnly()) null
            else "Please enter a valid LRN"
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeDisposable += viewModel.barcodeBitmapObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                image_generate.setImageBitmap(it)
            }
    }

    override fun onPause() {
        super.onPause()
        barcodeDisposable.clear()
    }
}
