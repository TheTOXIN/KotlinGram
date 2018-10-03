package com.toxin.kotlingram.recyclerview.item

import android.content.Context
import android.graphics.Color
import android.view.View
import com.google.firebase.auth.FirebaseAuth
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
        private val context: Context
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
        viewHolder.textView_bio.text = person.bio

        val countNotRead = person.counter.getOrDefault(FirebaseAuth.getInstance().currentUser?.uid!!, 0)
        viewHolder.textView_read.text = countNotRead.toString()

        if (countNotRead == 0) {
            viewHolder.textView_read_img.setColorFilter(Color.argb(255, 96, 96, 96))
            viewHolder.textView_read.visibility = View.INVISIBLE
        }

        if (person.profilePicturePath != null)
            GlideApp.with(context)
                    .load(StorageUtil.pathToReference(person.profilePicturePath))
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(viewHolder.imageView_profile_picture)
    }

    override fun getLayout() = R.layout.item_person
}