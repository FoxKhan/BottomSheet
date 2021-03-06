package com.example.bottomsheet

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import com.example.bottomsheet.KeyboardInfo.keyboardHeight
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_dialog.*
import kotlinx.android.synthetic.main.bottom_dialog.view.*


class BottomDialog : BottomSheetDialogFragment() {

    private var state = NOTHING

    private var currentAnimation: ValueAnimator? = null

    override fun getTheme(): Int = R.style.BottomDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("SDS", "onSlide, slideOffset: $slideOffset")
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("SDS", "onStateChanged, newState: $newState")

//                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
//                    dialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN;

            }
        })
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.bottom_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button1.setOnClickListener {
            toggleState()
        }
        button2.setOnClickListener {
            setState(NOTHING)
        }

        KeyboardHeightProvider(activity!! as AppCompatActivity).addKeyboardListener(object :
            KeyboardHeightProvider.KeyboardListener {
            override fun onHeightChanged(height: Int) {
                Log.d("SDS", "onHeightChanged, height: $height")
                if (height > 0) {
                    onShowKeyboardAnimation(height)
                    state = KEYBOARD
                } else {
                    if (state == ATTACHES)
                        onShowKeyboardAnimation(keyboardHeight)
                    else
                        onShowKeyboardAnimation(0)
                }
            }
        })

        if (savedInstanceState == null) {
            editText.focusAndShowKeyboard()
            val anim = ObjectAnimator.ofFloat(view,  View.ALPHA, 0f, 1f)
            anim.duration = ANIMATION_DURATION
            anim.start()
        }
    }

    override fun onPause() {
        currentAnimation?.cancel()
        super.onPause()
    }

    private fun toggleState() {
        if (state == KEYBOARD)
            setState(ATTACHES)
        else
            editText.focusAndShowKeyboard()
    }

    private fun setState(state: Int) {
        when (state) {
            KEYBOARD -> {

            }
            ATTACHES -> {
                hideKeyboardFrom(dialog!!.context, editText)
            }
            NOTHING -> {
                if (KeyboardInfo.keyboardState == KeyboardInfo.STATE_CLOSED)
                    onShowKeyboardAnimation(0)
                else hideKeyboardFrom(dialog!!.context, editText)
            }
        }
        this.state = state
    }

    private fun onShowKeyboardAnimation(to: Int) {
        currentAnimation?.cancel()
        attachWindow ?: return
        currentAnimation = ValueAnimator.ofInt(attachWindow.height, to).apply {
            duration = ANIMATION_DURATION
            addUpdateListener {
                val value = it.animatedValue as Int
                attachWindow.layoutParams = LayoutParams(MATCH_PARENT, value)
            }
            start()
        }
    }

    companion object {

        private const val KEYBOARD = 0
        private const val ATTACHES = 1
        private const val NOTHING = 2
        private const val ANIMATION_DURATION = 200L

        fun newInstance(): BottomDialog = BottomDialog()

        fun hideKeyboardFrom(context: Context, view: View) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun showSoftKeyboard(view: View) {
            if (view.requestFocus()) {
                val imm =
                    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}