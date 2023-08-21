package com.restart.banzenty.ui.main

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.navigation.NavigationView
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.data.models.UserModel
import com.restart.banzenty.databinding.ActivityMainBinding
import com.restart.banzenty.databinding.CustomLogoutDialogViewBinding
import com.restart.banzenty.databinding.NavHeaderBinding
import com.restart.banzenty.ui.auth.LoginActivity
import com.restart.banzenty.ui.intro.IntroActivity
import com.restart.banzenty.utils.BottomNavController
import com.restart.banzenty.utils.Constants
import com.restart.banzenty.utils.NewNotificationBroadCastReceiver
import com.restart.banzenty.utils.setUpNavigation
import com.restart.banzenty.utils.toArabicNumbers
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener, BottomNavController.OnNavigationGraphChanged,
    BottomNavController.NavGraphProvider, BottomNavController.OnNavigationReselectedListener,
    NewNotificationBroadCastReceiver.ReceivingNotificationInterface {
    private var tvCount: TextView? = null
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController

    lateinit var toggle: ActionBarDrawerToggle

    val activeSubscription = MutableLiveData<UserModel.ActiveSubscription>()
    private var receiver: NewNotificationBroadCastReceiver? = null
    private var intentFilter: IntentFilter? = null

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this, R.id.fragmentsContainer, R.id.home_menu_item,
            this, this
        )
    }
    private val openSubscriptionIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    //open subscription fragment
                    navigateToMySubscription()
                }
            }
        }

    private val openProfileIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    //open subscription fragment
                    populateData()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        sessionManager.fetchToken()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isUserLoggedIn(savedInstanceState)
        registerNotificationBroadCastReceiver()
        Log.d(TAG, "onCreate: ${sessionManager.getToken()}")
    }

    private fun registerNotificationBroadCastReceiver() {
        receiver = NewNotificationBroadCastReceiver(this)
        intentFilter = IntentFilter(Constants.NEW_NOTIFICATION_ACTION)
    }

    private fun isUserLoggedIn(savedInstanceState: Bundle?) {
        //content is ready, start drawing
        if (sessionManager.isFirstTime()) {
            //First time user
            startActivity(Intent(this@MainActivity, IntroActivity::class.java))
            finish()
        } else if (sessionManager.getToken()
                .isBlank() && !intent.hasExtra("isGuest")
        ) {
            //Either First time nor logged in
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            setUpBottomNav(savedInstanceState)
            setUpToggle()
            binding.navView.setNavigationItemSelectedListener(this@MainActivity)
            binding.buttonLogout.setOnClickListener(this@MainActivity)
            binding.homeContent.toolbar.root.inflateMenu(R.menu.toolbar_menu)
            binding.homeContent.toolbar.root.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_notification -> {
                        openNotificationsActivity()
                    }
                }
                true
            }
            val notificationItem = binding.homeContent.toolbar.root.menu.getItem(0).actionView
            notificationItem?.setOnClickListener {
//                displayToast("Open Notification")
                openNotificationsActivity()
            }
            tvCount = notificationItem?.findViewById<View>(R.id.tv_notification_count) as TextView
            populateData()
        }
    }

    private fun openNotificationsActivity() {
        if (sessionManager.getToken().isEmpty())
            showLoginDialog()
        else {
            val intent = Intent(
                this@MainActivity,
                NotificationsActivity::class.java
            )
            startActivity(intent)
        }
        setNotificationCount(0)
    }

    fun setNotificationCount(notificationCount: Int) {
        tvCount?.visibility = if (notificationCount != 0) View.VISIBLE else View.INVISIBLE
        tvCount?.text = "$notificationCount"
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
//        TODO("Not yet implemented")
    }

/*
    private fun exitSplashScreen(savedInstanceState: Bundle?) {
        //setup an onPreDrawListener to the root view
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    //check if the initial data is ready
                    val openNextActivity = true
                    return if (openNextActivity) {
                        //content is ready, start drawing
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        if (sessionManager.isFirstTime()) {
                            //First time user
                            startActivity(Intent(this@MainActivity, IntroActivity::class.java))
                            finish()
                        } else if (sessionManager.getToken()
                                .isBlank() && !intent.hasExtra("isGuest")
                        ) {
                            //Either First time nor logged in
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            setUpBottomNav(savedInstanceState)
                            setUpToggle()
                            binding.navView.setNavigationItemSelectedListener(this@MainActivity)
                            binding.buttonLogout.setOnClickListener(this@MainActivity)

                            binding.homeContent.toolbar.root.inflateMenu(R.menu.toolbar_menu)
                            binding.homeContent.toolbar.root.setOnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.action_notification -> {
                                        if (sessionManager.getToken().isEmpty())
                                            showLoginDialog()
                                        else {
                                            val intent = Intent(
                                                this@MainActivity,
                                                NotificationsActivity::class.java
                                            )
                                            startActivity(intent)
                                        }
                                    }
                                }
                                true
                            }
                            populateData()
                        }

                        //else user is logged in
                        true
                    } else {
                        //the content is not ready, suspend
                        false
                    }
                }
            }
        )
    }
*/

    fun populateData() {
        val viewHeader = binding.navView.getHeaderView(0)
        val navViewHeaderBinding: NavHeaderBinding = NavHeaderBinding.bind(viewHeader)
        if (intent.hasExtra("isGuest")) {
            navViewHeaderBinding.textViewUserName.text = getString(R.string.guest)
            Log.d(TAG, "populateData: ${sessionManager.getUserImage()}")
        } else {
            // navView is NavigationView
            navViewHeaderBinding.textViewUserName.text = sessionManager.getUserName()
        }
        requestManager.load(sessionManager.getUserImage())
            .transform(CenterCrop(), CircleCrop())
            .into(navViewHeaderBinding.imageviewProfileImage)
        if (sessionManager.getCarPlateCharacters().isNotEmpty()) {
            // put the plate numbers
            navViewHeaderBinding.textViewPlateNum.text = sessionManager.getCarPlateDigits().toArabicNumbers()
            val carPlateChars = sessionManager.getCarPlateCharacters()
            for (i in carPlateChars.indices) {
                when (i) {
                    0 -> navViewHeaderBinding.textViewPlate1stLetter.text =
                        carPlateChars[i].toString()
                    1 -> navViewHeaderBinding.textViewPlate2ndLetter.text =
                        carPlateChars[i].toString()
                    2 -> navViewHeaderBinding.textViewPlate3rdLetter.text =
                        carPlateChars[i].toString()
                }
            }
            if (navViewHeaderBinding.textViewPlate3rdLetter.text.toString().trim().isEmpty())
                navViewHeaderBinding.textViewPlate3rdLetter.visibility = View.GONE
        } else {
            navViewHeaderBinding.textViewPlateNum.visibility = View.GONE
            navViewHeaderBinding.textViewPlate1stLetter.visibility = View.GONE
            navViewHeaderBinding.textViewPlate2ndLetter.visibility = View.GONE
            navViewHeaderBinding.textViewPlate3rdLetter.visibility = View.GONE
        }

    }


    private fun setUpBottomNav(savedInstanceState: Bundle?) {
        binding.homeContent.bottomNavigationMenu
            .setUpNavigation(bottomNavController, this)

        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }
    }

    private fun setNavController() {
        navController = Navigation.findNavController(this, R.id.fragmentsContainer)
    }

    fun showLoginDialog() {
        //show logout dialog
        val builder = AlertDialog.Builder(this)
        val dialogViewBinding = CustomLogoutDialogViewBinding.inflate(layoutInflater)
        dialogViewBinding.textViewLogoutDialogTitle.text = getString(R.string.need_access)
        dialogViewBinding.textViewLogoutDialogContent.text =
            getString(R.string.you_need_to_login)
        dialogViewBinding.buttonDialogLogout.text = getString(R.string.sign_in)
        builder.setView(dialogViewBinding.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogViewBinding.buttonCancelLogout.setOnClickListener {
            alertDialog.hide()
        }
        dialogViewBinding.buttonDialogLogout.setOnClickListener {
            alertDialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun setUpToggle() {
        toggle = object : ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.homeContent.toolbar.root, R.string.open_nav, R.string.close_nav
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
            }
        }

        toggle.isDrawerIndicatorEnabled = false

        val drawable = ResourcesCompat.getDrawable(
            resources, R.drawable.hamburger_menu_ic,
            this.theme
        )

        toggle.setHomeAsUpIndicator(drawable)

        binding.drawerLayout.addDrawerListener(toggle)

        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        toggle.syncState()
    }

    override fun getNavGraphId(itemId: Int) =
        when (itemId) {
            R.id.home_menu_item -> {
                R.navigation.nav_home
            }

            R.id.requests_menu_item -> {
                if (sessionManager.getToken().isEmpty()) {
                    R.navigation.nav_home
                } else R.navigation.nav_requests
            }

            R.id.subscription_menu_item -> {
                if (sessionManager.getToken().isEmpty()) {
                    R.navigation.nav_home
                } else R.navigation.nav_subscription

            }

            R.id.wallet_menu_item -> {
                if (sessionManager.getToken().isEmpty()) {
                    R.navigation.nav_home
                } else R.navigation.nav_wallet

            }

            else -> {
                R.navigation.nav_home
            }
        }

    override fun onGraphChanged() {
        when (binding.homeContent.bottomNavigationMenu.selectedItemId) {
            R.id.home_menu_item -> {
                binding.homeContent.toolbar.textViewToolbarTitle.text = getString(R.string.home)
            }

            R.id.requests_menu_item -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else
                    binding.homeContent.toolbar.textViewToolbarTitle.text =
                        getString(R.string.requests)
            }

            R.id.subscription_menu_item -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else binding.homeContent.toolbar.textViewToolbarTitle.text =
                    getString(R.string.subscription)
            }

            else -> {
                binding.homeContent.toolbar.textViewToolbarTitle.text = getString(R.string.home)
            }
        }
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) {
        when (fragment) {
            is HomeFragment -> {
                fragment.onRefresh()
            }
            is FuelRequestsFragment -> {
                fragment.refreshFragment()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent

        when (item.itemId) {
            R.id.home_nav -> {
                binding.homeContent.bottomNavigationMenu.selectedItemId = R.id.home_menu_item
            }
            R.id.profile_nav -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else navigateToProfile()
            }
            R.id.myRequests_nav -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else binding.homeContent.bottomNavigationMenu.selectedItemId =
                    R.id.requests_menu_item
            }
            R.id.subscriptions_nav -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else {
                    val subscriptionsIntent = Intent(
                        this@MainActivity,
                        SubscriptionsActivity::class.java
                    )
                    subscriptionsIntent.putExtra(
                        "active_subscription_id",
                        activeSubscription.value?.plan?.id ?: -1
                    )
                    openSubscriptionIntent.launch(subscriptionsIntent)
                }
            }
            R.id.settings_nav -> {
                if (sessionManager.getToken().isEmpty())
                    showLoginDialog()
                else {
                    intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.contactUs_nav -> {
                intent = Intent(this, ContactUsActivity::class.java)
                startActivity(intent)
            }
/*            R.id.language_nav -> {
            }*/
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        toggle.syncState()

        return false
    }

    override fun onClick(v: View?) {
        if (v == binding.buttonLogout) {

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            toggle.syncState()

            //show logout dialog
            val builder = AlertDialog.Builder(this)

            val viewGroup = findViewById<ViewGroup>(android.R.id.content)

            val dialogView = LayoutInflater.from(this).inflate(
                R.layout.custom_logout_dialog_view,
                viewGroup, false
            )

            builder.setView(dialogView)

            val alertDialog = builder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialog.show()

            val cancelButton = dialogView.findViewById<Button>(R.id.buttonCancelLogout)
            cancelButton.setOnClickListener {
                alertDialog.hide()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                toggle.syncState()
            }

            val logoutButton = dialogView.findViewById<Button>(R.id.buttonDialogLogout)
            logoutButton.setOnClickListener {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                toggle.syncState()

                alertDialog.dismiss()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                sessionManager.logout()
                startActivity(intent)
                finish()
            }
        }
    }


    override fun onBackPressed() {
        bottomNavController.onBackPressed()
    }

    fun navigateToProfile() {
        if (sessionManager.getToken().isNotEmpty())
            openProfileIntent.launch(Intent(this, ProfileActivity::class.java))
        else {
            showLoginDialog()
            /*           displayInfoWithCancelOptionsDialog(
                           getString(R.string.you_need_to_login),
                           object : AreYouSureCallback {
                               override fun proceed() {
                                   startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                               }

                               override fun cancel() {
           //                TODO("Not yet implemented")
                               }

                           })*/
        }

    }

    fun navigateToMySubscription() {
        binding.homeContent.bottomNavigationMenu.selectedItemId = R.id.subscription_menu_item
    }

    fun navigateToHome() {
        binding.homeContent.bottomNavigationMenu.selectedItemId = R.id.home_menu_item
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationReceived() {
        setNotificationCount(tvCount?.text.toString().trim().toInt() + 1)
        navigateToHome()
    }
}