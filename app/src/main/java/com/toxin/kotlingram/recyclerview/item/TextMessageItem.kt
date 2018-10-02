package com.toxin.kotlingram.recyclerview.item

import android.content.Context
import com.toxin.kotlingram.R
import com.toxin.kotlingram.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class TextMessageItem(
        val message: TextMessage,
        val context: Context
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout() = R.layout.item_text_message
}