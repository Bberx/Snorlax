package com.snorlax.snorlax.utils.preference

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    companion object {
        fun newInstance(key: String): TimePreferenceDialogFragmentCompat {
            val fragment = TimePreferenceDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString("key", key)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {

    }

}