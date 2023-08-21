package com.restart.banzenty.utils

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.restart.banzenty.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavController(val context: Context, @IdRes val containerId: Int,
                          @IdRes val appStartDestinationId: Int,
                          val graphChangeListener: OnNavigationGraphChanged?,
                          val navigationProvider: NavGraphProvider) {

    private val TAG = "BottomNavController"
    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
    private lateinit var navItemChangeListener:OnNavigationItemChanged
    private val navigationBackStack: BackStack = BackStack.of(appStartDestinationId)

    init {
        if (context is Activity) {
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    private class BackStack: ArrayList<Int>() {
        companion object {
            fun of(vararg elements: Int): BackStack {
                val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size - 1)

        fun moveLast(item: Int) {
            remove(item) //if present, remove
            add(item) //add to the end of the list
        }
    }

    //For setting the checked icon in the bottom nav
    interface OnNavigationItemChanged {
        fun onItemChanged(itemId: Int)
    }

    //Get id of each graph, ex: R.navigation.nav_blog
    interface NavGraphProvider {
        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    //Executed when nav graph changed, ex: select a new item on the bottom nav: Home to account.
    interface OnNavigationGraphChanged {
        fun onGraphChanged()
    }

    interface OnNavigationReselectedListener {
        fun onReselectNavItem(navController: NavController, fragment: Fragment)
    }

    fun setOnItemNavigationChanged(listener: (itemId: Int) -> Unit) {
        this.navItemChangeListener = object : OnNavigationItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId)
            }
        }
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()) : Boolean {
        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: NavHostFragment.create(navigationProvider.getNavGraphId(itemId))

        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        //Add to backstack
        navigationBackStack.moveLast(itemId)

        //Update checked icon
        navItemChangeListener.onItemChanged(itemId)

        //communicate with activity
        graphChangeListener?.onGraphChanged()

        return true
    }

    fun onBackPressed() {
        val childFragmentManager = fragmentManager.findFragmentById(containerId)!!.childFragmentManager

        when {
            childFragmentManager.popBackStackImmediate() -> {}

            //Fragment backstack empty so try to back on the navigation stack
            navigationBackStack.size > 1 -> {

            //Remove last item from backstack
            navigationBackStack.removeLast()

            //update the container with the new fragment
            onNavigationItemSelected()
            }

            navigationBackStack.last() != appStartDestinationId -> {
                navigationBackStack.removeLast()
                navigationBackStack.add(0, appStartDestinationId)
                onNavigationItemSelected()
            }

            else -> activity.finish()
        }
    }
}

// Convenience extension to set up the navigation
fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onReselectedListener: BottomNavController.OnNavigationReselectedListener) {

    setOnNavigationItemSelectedListener { bottomNavController.onNavigationItemSelected(it.itemId) }
    setOnNavigationItemReselectedListener {
        bottomNavController.fragmentManager.findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager.fragments[0]?.let { fragment ->
            onReselectedListener.onReselectNavItem(
                bottomNavController.activity.findNavController(bottomNavController.containerId), fragment)
        }
    }

    bottomNavController.setOnItemNavigationChanged { itemId ->
menu.findItem(itemId).isChecked = true
    }
}