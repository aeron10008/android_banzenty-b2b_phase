package com.restart.banzenty.utils

interface UICommunicationListener {
    fun onUIMessageReceived(uiMessage: UIMessage)
    fun onUIListOptionResReceived(uiListSingleResSelection: UIListSingleResSelection)
    fun onUIListOptionStringReceived(uIListSingleStringSelection: UIListSingleStringSelection)
    fun onUIListOptionObjectReceived(uIListSingleObjectSelection: UIListSingleObjectSelection)
    fun onUIListOptionsReceived(uiListMultiSelection: UIListMultiSelection)
    fun onUIDateTimeReceived(uiDateTimePicker: UIDateTimePicker)
}