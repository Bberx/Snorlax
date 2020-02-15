package com.snorlax.snorlax.ui.home.generate


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.snorlax.snorlax.R
import com.snorlax.snorlax.utils.adapter.viewpager.GeneratorAdapter
import com.snorlax.snorlax.viewmodel.GeneratorViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_generate.view.*

class GeneratorFragment : Fragment() {

    private lateinit var viewModel: GeneratorViewModel
    private val manualDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.generate_viewpager.adapter = GeneratorAdapter(this)

        TabLayoutMediator(
            view.generate_tab,
            view.generate_viewpager
        ) { tab: TabLayout.Tab, index: Int ->
            when (index) {
                0 -> {
                    tab.setIcon(R.drawable.ic_class)
                    tab.setText(R.string.label_class)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_barcode)
                    tab.setText(R.string.label_generate)
                }
            }
        }.attach()


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[GeneratorViewModel::class.java]
        // TODO: Use the ViewModel


    }

}
