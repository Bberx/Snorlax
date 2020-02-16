package com.snorlax.snorlax.utils

import android.view.View
import android.view.animation.AlphaAnimation

fun View.fadeIn(doOnStart: (() -> Unit)? = null, doOnEnd: (() -> Unit)? = null) {
    val fadeIn = AlphaAnimation(0f, 1f).apply {
        duration = 200
//        setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationRepeat(animation: Animation?) {}
//            override fun onAnimationEnd(animation: Animation?) {
//                doOnEnd?.invoke()
//            }
//
//            override fun onAnimationStart(animation: Animation?) {
//                doOnStart?.invoke()
//            }
//        })
    }
    startAnimation(fadeIn)
}

fun View.fadeOut(doOnStart: (() -> Unit)? = null, doOnEnd: (() -> Unit)? = null) {
    val fadeOut = AlphaAnimation(1f, 0f).apply {
        duration = 200
//        setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationRepeat(animation: Animation?) {}
//            override fun onAnimationEnd(animation: Animation?) {
//                doOnEnd?.invoke()
//            }
//
//            override fun onAnimationStart(animation: Animation?) {
//                doOnStart?.invoke()
//            }
//        })
    }
    startAnimation(fadeOut)
}