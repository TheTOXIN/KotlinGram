package com.toxin.kotlingram

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import com.google.firebase.firestore.ListenerRegistration
import com.toxin.kotlingram.recyclerview.item.TextMessageItem
import com.toxin.kotlingram.util.FirestoreUtil
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity() {

    private lateinit var messsagesListenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreatChatChannel(otherUserId) { channelId ->
            messsagesListenerRegistration =
                    FirestoreUtil.addChatMessageListener(channelId, this, this::onMessagesChanged)
        }
    }

    private fun onMessagesChanged(messages: List<TextMessageItem>) {
        toast("MESSAGE CHANGED RUN!!!")
    }

}
