package com.toxin.kotlingram.recyclerview.item

import android.content.Context
import android.graphics.Color
import com.toxin.kotlingram.R
import com.toxin.kotlingram.glide.GlideApp
import com.toxin.kotlingram.model.User
import com.toxin.kotlingram.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*


class PersonItem(
        val person: User,
        val userId: String,
        val notRead: Int,
        private val context: Context
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
        viewHolder.textView_bio.text = person.bio
        viewHolder.textView_read.text = notRead.toString()

        if (notRead == 0)
            viewHolder.textView_read_img.setColorFilter(Color.argb(255, 96, 96, 96))

        if (person.profilePicturePath != null)
            GlideApp.with(context)
                    .load(StorageUtil.pathToReference(person.profilePicturePath))
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(viewHolder.imageView_profile_picture)
    }

    override fun getLayout() = R.layout.item_person
}