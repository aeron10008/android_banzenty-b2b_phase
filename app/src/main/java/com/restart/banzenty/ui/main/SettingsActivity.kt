package com.restart.banzenty.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbarSetup()
        initViews()
    }


    private fun toolbarSetup() {
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.settings)
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        binding.tvChangeLanguage.setOnClickListener(this)
        binding.tvChangePassword.setOnClickListener(this)
        binding.tvChangePassword.visibility = if (sessionManager.getSocialId().isNullOrEmpty())
            View.VISIBLE else View.GONE
    }

    override fun onClick(v: View) {
        when (v) {
            binding.tvChangePassword -> {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(intent)
            }
            binding.tvChangeLanguage -> {
                val intent = Intent(this, ChangeLanguageActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun showProgressBar(show: Boolean) {
//        Not used here
    }

    override fun showErrorUI(
        show: Boolean,
        image: Int?,
        title: String?,
        desc: String?,
        showButton: Boolean?
    ) {
//        Not used here
    }


}