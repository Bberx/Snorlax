/*
 * Copyright 2019 Oliver Rhyme G. Añasco
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.snorlax.snorlax.views

import android.content.Context
import android.view.TextureView
import android.view.ViewGroup


class CameraPreview(context: Context) :
    ViewGroup(context) {
    private val mTextureView: TextureView = TextureView(context)
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // crop view

        val actualPreviewWidth = resources.displayMetrics.widthPixels
        val actualPreviewHeight = resources.displayMetrics.heightPixels
        mTextureView.layout(0, 0, actualPreviewWidth, actualPreviewHeight/2)
//        mTextureView.translationY = -actualPreviewHeight/3F
    }

// camera methods
    init {
        addView(mTextureView)
    }

}