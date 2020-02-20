package com.snorlax.snorlax.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.Px
import org.apache.poi.util.Units
import kotlin.math.roundToInt

@Px
fun Int.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()
}

fun Double.cmToEMU(): Double {
    return this * Units.EMU_PER_CENTIMETER
}

fun Double.emuToTwips(): Double {
    return (this * 20.0) / Units.EMU_PER_POINT
}