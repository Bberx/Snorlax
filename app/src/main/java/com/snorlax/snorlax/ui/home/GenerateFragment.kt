package com.snorlax.snorlax.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.barcode.BarcodeUtils
import com.snorlax.snorlax.viewmodel.GenerateViewModel
import kotlinx.android.synthetic.main.fragment_generate.*

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
            text_layout_generate.error = null
            try {
                val bitmap = BarcodeUtils.encodeToBitmap(
                    input_generate.text.toString(),
                    width = 700,
                    height = 200
                )
                image_generate.setImageBitmap(bitmap)


            } catch (error: Exception) {
                text_layout_generate.error = error.localizedMessage
            }
        }
    }

}
