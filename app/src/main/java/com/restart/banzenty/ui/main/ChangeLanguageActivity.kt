package com.restart.banzenty.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityChangeLanguageBinding
import com.restart.banzenty.utils.MainUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeLanguageActivity : BaseActivity() {

    lateinit var binding: ActivityChangeLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarSetup()

        setDetails()
    }

    override fun showProgressBar(show: Boolean) {
//        TODO("Not yet implemented")
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
        TODO("Not yet implemented")
    }

    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.change_Language)

        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun setDetails() {
        if (sessionManager.getLanguage() == "en") {
            binding.englishView.isSelected = true
            binding.arabicView.isSelected = false
        }
        else {
            binding.arabicView.isSelected = true
            binding.englishView.isSelected = false
        }

        binding.arabicView.setOnClickListener {
            if (!binding.arabicView.isSelected) {
                binding.arabicView.isSelected = true
                binding.englishView.isSelected = false

                sessionManager.setLanguage("ar")
                restartApp()
//                languageChanged(binding.arabicView)
            }
        }

        binding.englishView.setOnClickListener {
            if (!binding.englishView.isSelected) {
                binding.englishView.isSelected = true
                binding.arabicView.isSelected = false

                sessionManager.setLanguage("en")
                restartApp()
//                languageChanged(binding.englishView)
            }
        }


    }

    private fun restartApp() {
        val intent = Intent(this@ChangeLanguageActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        finish()
    }


/*    private fun languageChanged(view: View) {
        //show password changed dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        val viewGroup = this.findViewById<ViewGroup>(android.R.id.content)

        val dialogView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.custom_dialog_view, viewGroup, false)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        dialogTitle.text = getString(R.string.language_changed)

        val dialogContent = dialogView.findViewById<TextView>(R.id.textViewDialogContent)
        dialogContent.text = getString(R.string.language_changed_msg)

        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val doneButton = dialogView.findViewById<Button>(R.id.buttonDone)

        doneButton.setOnClickListener {
            alertDialog.hide()

            finish()
        }
    }*/
}