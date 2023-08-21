package com.restart.banzenty.utils

import com.restart.banzenty.network.DataState

interface DataStateChangeListener {
    fun onDataStateChange(dataState: DataState<*>?)
    fun hideSoftKeyboard()
    fun showSoftKeyboard()
    fun onErrorStateChange(stateError: StateError)
}