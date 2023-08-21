package com.restart.banzenty.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import com.restart.banzenty.R
import com.restart.banzenty.databinding.ActivityIntroBinding
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroActivity : BaseActivity() {
    var sceneCount = 0
    lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.flNext.setOnClickListener {
            when (sceneCount) {
                0 -> {
                    binding.navHostFragment.findNavController()
                        .navigate(R.id.action_introGetServicesFragment_to_introMarketFragment2)
                    sceneCount++
                }
                1 -> {
                    binding.navHostFragment.findNavController()
                        .navigate(R.id.action_introMarketFragment_to_introCashBackFragment)
                    sceneCount++
                }
                2 -> {
                    sessionManager.setFirstTime(false)
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
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

    override fun onBackPressed() {
    }
}