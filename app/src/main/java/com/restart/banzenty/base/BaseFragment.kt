package com.restart.banzenty.base

import androidx.fragment.app.Fragment
import com.bumptech.glide.RequestManager
import com.restart.banzenty.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment : Fragment(){
    private val TAG = "BaseFragment"

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var requestManager: RequestManager


    abstract fun showProgressBar(show: Boolean)

    abstract fun showErrorUI(
        show: Boolean,
        image: Int? = 0,
        title: String? = "",
        desc: String? = null,
        showButton: Boolean? = false
    )

}