package com.restart.banzenty.utils

import androidx.recyclerview.widget.RecyclerView
import java.util.*

data class UIMessage(
    val message: String,
    val uiMessageType: UIMessageType
)

data class UIListMultiSelection(
    val title: String,
    val list: List<CharSequence>,
    val indices: IntArray,
    val uiMessageType: UIMessageType
)

data class UIListSingleResSelection(
    val title: String,
    val list: Int,
    val pos: Int = 0,
    val uiMessageType: UIMessageType
)

data class UIListSingleStringSelection(
    val title: String,
    val list: List<CharSequence>,
    val pos: Int = 0,
    val uiMessageType: UIMessageType
)

data class UIListSingleObjectSelection(
    val title: String,
    val adapter: RecyclerView.Adapter<*>,
    val callback: AreYouSureCallback
)

data class UIDateTimePicker(
    val needTime: Boolean,
    val currentDateTime: Calendar? = null,
    val uiMessageType: UIMessageType
)


sealed class UIMessageType {

    class Toast : UIMessageType()
    class Dialog : UIMessageType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ) : UIMessageType()

    class SingleChoiceDialog(
        val callback: SingleChoiceCallback
    ) : UIMessageType()

    class MultiChoiceDialog(
        val callback: MultiChoiceCallback
    ) : UIMessageType()

    class DateTimePickerDialog(
        val callback: DateCallback
    ) : UIMessageType()

    class None : UIMessageType()
}