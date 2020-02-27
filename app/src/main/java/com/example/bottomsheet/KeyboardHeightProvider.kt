package com.example.bottomsheet

import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.android.synthetic.main.keyboard_popup.view.*


class KeyboardHeightProvider(private val activity: AppCompatActivity) : PopupWindow(activity),
    LifecycleObserver {

    private var resizableView: View
    private var parentView: View? = null
    private var lastKeyboardHeight = -1

    private var keyboardListeners = ArrayList<KeyboardListener>()

    private val globalLayoutListener by lazy(LazyThreadSafetyMode.NONE){
        ViewTreeObserver.OnGlobalLayoutListener {
            computeKeyboardState()
        }
    }

    init {
        contentView = View.inflate(activity, R.layout.keyboard_popup, null)
        resizableView = contentView.keyResizeContainer
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        parentView = activity.findViewById(android.R.id.content)
        parentView?.post {
            resizableView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            if (!isShowing && parentView?.windowToken != null) {
                showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        resizableView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        dismiss()
    }

    private fun computeKeyboardState() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        resizableView.getWindowVisibleDisplayFrame(rect)
        val orientation = activity.resources.configuration.orientation

        val keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
        KeyboardInfo.keyboardState =
            if (keyboardHeight > 0) KeyboardInfo.STATE_OPENED else KeyboardInfo.STATE_CLOSED
        if (keyboardHeight > 0) {
            KeyboardInfo.keyboardHeight = keyboardHeight
        }
        if (keyboardHeight != lastKeyboardHeight)
            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        lastKeyboardHeight = keyboardHeight
    }

    private val topCutoutHeight: Int
        get() {
            val decorView = activity.window.decorView ?: return 0
            var cutOffHeight = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val windowInsets = decorView.rootWindowInsets
                val displayCutout = windowInsets.displayCutout
                if (displayCutout != null) {
                    val list = displayCutout.boundingRects
                    for (rect in list) {
                        if (rect.top == 0) {
                            cutOffHeight += rect.bottom - rect.top
                        }
                    }
                }
            }
            return cutOffHeight
        }

    fun addKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.add(listener)
    }

    fun removeKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.remove(listener)
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardListeners.forEach {
            it.onHeightChanged(height)
        }
    }

    interface KeyboardListener {
        fun onHeightChanged(height: Int)
    }
}