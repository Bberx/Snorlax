package com.snorlax.snorlax.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.Px
import kotlin.math.roundToInt

@Px
fun Int.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()
}