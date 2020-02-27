package com.example.bottomsheet

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

    override fun getTheme(): Int = R.style.BottomDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
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
                    view.attachWindow.layoutParams = LayoutParams(MATCH_PARENT, height)
                    state = KEYBOARD
                } else {
                    if (state == ATTACHES)
                        view.attachWindow.layoutParams = LayoutParams(MATCH_PARENT, keyboardHeight)
                    else
                        view.attachWindow.layoutParams = LayoutParams(MATCH_PARENT, 0)
                }
            }
        })
    }

    private fun toggleState() {
        if (state == KEYBOARD)
            setState(ATTACHES)
        else
            setState(KEYBOARD)
    }

    private fun setState(state: Int) {
        when (state) {
            KEYBOARD -> {
                showSoftKeyboard(editText)
            }
            ATTACHES -> {
                hideKeyboardFrom(dialog!!.context, editText)
            }
            NOTHING -> {
                if (KeyboardInfo.keyboardState == KeyboardInfo.STATE_CLOSED)
                    attachWindow.layoutParams = LayoutParams(MATCH_PARENT, 0)
                else hideKeyboardFrom(dialog!!.context, editText)
            }
        }
        this.state = state
    }

    companion object {

        private const val KEYBOARD = 0
        private const val ATTACHES = 1
        private const val NOTHING = 2

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