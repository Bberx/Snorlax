package com.snorlax.snorlax.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.getSystemService
import com.facebook.shimmer.ShimmerFrameLayout
import com.snorlax.snorlax.R
import kotlin.math.ceil

class ShimmerListProgress @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShimmerFrameLayout(context, attrs, defStyleAttr) {

    private fun getSkeletonRowCount(): Int {
        val pxHeight = getDeviceHeight()
        val skeletonRowHeight =
//            view.measureAndGetHeight()
            context.resources.getDimension(R.dimen.item_height).toInt()
        return ceil((pxHeight / skeletonRowHeight).toDouble()).toInt()
    }

    private fun getDeviceHeight(): Int {
        return context.resources.displayMetrics.heightPixels
    }


    fun setLayoutChild(id: Int) {
        val inflater = context.getSystemService<LayoutInflater>()!!

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            repeat(getSkeletonRowCount()) {
                val view = inflater.inflate(id, null)
                addView(view, it)
            }
        }
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(layout, params)
    }


}