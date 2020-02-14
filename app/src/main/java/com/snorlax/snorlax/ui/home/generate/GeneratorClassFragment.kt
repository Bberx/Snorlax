package com.snorlax.snorlax.ui.home.generate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.snorlax.snorlax.R
import com.snorlax.snorlax.viewmodel.GeneratorClassViewModel

class GeneratorClassFragment : Fragment() {

    private lateinit var viewModel: GeneratorClassViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generator_class, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GeneratorClassViewModel::class.java]
        // TODO: Use the ViewModel
    }

}
