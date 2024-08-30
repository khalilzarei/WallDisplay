package com.hsi.walldisplay.helper

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hsi.walldisplay.helper.SessionManager.Companion.fontSize


class Calligrapher(context: Context) {
    private val mAssetManager: AssetManager = context.assets // AsetManger needs to create Typeface from a path
    private val mTypefaceMap: MutableMap<String, Typeface?> = HashMap() // fontPath vs Typeface map used for caching Typeface


    /**
     * Set font to every view that supports typeface of the given activity layout
     *
     * @param activity         Target activity
     * @param fontPath         Font file source path (Font must be in the assets directory of the application)
     * @param includeActionbar Flag to determine if the Actionbar title font need to be changed or not
     */
    fun setFont(activity: Activity, fontPath: String, includeActionbar: Boolean) {
        val typeface = cacheFont(fontPath)
        val rootView = if (includeActionbar) activity.window.decorView
        else (activity.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
        traverseView(rootView, typeface)
    }

    fun setFontSize(activity: Activity, fontSize: Float, includeActionbar: Boolean) {
        val rootView = if (includeActionbar) activity.window.decorView
        else (activity.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
        sizeView(rootView, fontSize)
    }


    fun setFontSize(view: View) {
        sizeView(view, fontSize)
    }

    /**
     * Set font to target view and it's child (if any)
     *
     * @param view     Target view
     * @param fontPath Font file source path (Font must be in the assets directory of the application)
     */
    fun setFont(view: View, fontPath: String?) {
        if (fontPath != null) {
            val typeface = cacheFont(fontPath)
            traverseView(view, typeface)
        }
    }


    /**
     * Traverse view recursively from the given target view
     *
     * @param view     Target view
     * @param typeface Typeface which needs to be set from the given target view to it's children
     */
    private fun traverseView(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val v = view.getChildAt(i)
                if (v is TextView) {
                    v.typeface = typeface
                }
                if (v is ViewGroup) {
                    traverseView(v, typeface)
                }
            }
        }
    }

    private fun sizeView(view: View, fontSize: Float) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val v = view.getChildAt(i)
                if (v is TextView) {
                    v.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
                }
                if (v is ViewGroup) {
                    sizeView(v, fontSize)
                }
            }
        }
    }


    /**
     * Creates a cached copy of a typeface from the given path
     *
     * @param fontPath Path from where a Typeface needs to be created
     * @return Cached copy of the typeface built from the font path
     */
    private fun cacheFont(fontPath: String): Typeface? {
        var cached = mTypefaceMap[fontPath]
        if (cached == null) {
            cached = Typeface.createFromAsset(mAssetManager, fontPath)
            mTypefaceMap[fontPath] = cached
        }
        return cached
    }

    private fun cacheFontSize(): Float {
        return fontSize
    }
}