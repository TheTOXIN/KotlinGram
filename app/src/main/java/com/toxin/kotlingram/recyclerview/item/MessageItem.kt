package com.toxin.kotlingram.recyclerview.item

import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.toxin.kotlingram.R
import com.toxin.kotlingram.model.Message
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat

abstract class MessageItem(
        private val message: Message
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        setTimeText(viewHolder)
        setMessageRootGravity(viewHolder)
        setStatusMessage(viewHolder)
    }

    private fun setTimeText(viewHolder: ViewHolder) {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewHolder: ViewHolder) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_primary_color
                val lParam = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.END)
                this.layoutParams = lParam
            }
        } else {
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_white
                val lParam = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.START)
                this.layoutParams = lParam
            }
            viewHolder.textView_message_time.apply {
                textColor = R.color.colorGray
            }
        }
    }

    private fun setStatusMessage(viewHolder: ViewHolder) {
        if (message.read) {
            viewHolder.textView_message_status.apply {
                imageResource = R.drawable.ic_check_box_black_24dp
            }
            viewHolder.textView_message_status.setColorFilter(
                    Color.rgb(17, 169, 53)
            )
        } else {
            viewHolder.textView_message_status.apply {
                imageResource = R.drawable.ic_remove_red_eye_black_24dp
            }
            viewHolder.textView_message_status.setColorFilter(
                    Color.rgb(228, 52, 52)
            )
        }
    }


}