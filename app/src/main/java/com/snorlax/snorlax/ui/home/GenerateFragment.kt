package com.snorlax.snorlax.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

import com.snorlax.snorlax.R
import com.snorlax.snorlax.viewmodel.GenerateViewModel
import kotlinx.android.synthetic.main.fragment_generate.*
import java.lang.Exception

class GenerateFragment : Fragment() {

    private lateinit var viewModel: GenerateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generate, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GenerateViewModel::class.java]
        // TODO: Use the ViewModel

        btn_generate.setOnClickListener {
            val writer = MultiFormatWriter()
            try {

                val bitMatrix = writer.encode(input_generate.text.toString(), BarcodeFormat.CODE_128, 400, 100)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)

                image_generate.setImageBitmap(bitmap)

            } catch (error: Exception) {
                text_layout_generate.error = error.localizedMessage
            }
        }
    }

}
