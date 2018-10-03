package com.toxin.kotlingram.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.toxin.kotlingram.model.*
import com.toxin.kotlingram.recyclerview.item.ImageMessageItem
import com.toxin.kotlingram.recyclerview.item.MessageItem
import com.toxin.kotlingram.recyclerview.item.PersonItem
import com.toxin.kotlingram.recyclerview.item.TextMessageItem

object FirestoreUtil {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UUID is NULL")}")

    private val chatChannelCollectionRef = firestoreInstance.collection("chatChannels")

    fun initCurrentIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                        FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "",
                        null,
                        mutableListOf()
                )
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null) {
        val userFiledMap = mutableMapOf<String, Any>()

        if (name.isNotBlank()) userFiledMap["name"] = name
        if (bio.isNotBlank()) userFiledMap["bio"] = bio
        if (profilePicturePath != null) userFiledMap["profilePicturePath"] = profilePicturePath

        currentUserDocRef.update(userFiledMap)
    }

    fun getCurrentUser(onComplete: (User?) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java))
                }
    }

    fun addUsersListener(context: Context, onListen: (List<PersonItem>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<PersonItem>()
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            items.add(PersonItem(
                                    it.toObject(User::class.java)!!,
                                    it.id,
                                    context
                            ))
                        }
                        onListen(items)
                    }
                }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(
            otherUserId: String,
            onComplete: (channelId: String) -> Unit
    ) {
        currentUserDocRef.collection("engagedChatChannels")
                .document(otherUserId).get().addOnSuccessListener {
                    if (it.exists()) {
                        onComplete(it["channelId"] as String)
                        return@addOnSuccessListener
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                    val newChannel = chatChannelCollectionRef.document()
                    newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                    currentUserDocRef
                            .collection("engagedChatChannels")
                            .document(otherUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    firestoreInstance.collection("users").document(otherUserId)
                            .collection("engagedChatChannels")
                            .document(currentUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
    }

    fun addChatMessagesListener(
            channelId: String,
            context: Context,
            onListen: (List<MessageItem>) -> Unit
    ): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<MessageItem>()
                    querySnapshot!!.documents.forEach {
                        if (it["senderId"] != FirebaseAuth.getInstance().currentUser?.uid)
                            readMessage(it.id, channelId)

                        if (it["type"] == MessageType.TEXT)
                            items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                        else
                            items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))

                        return@forEach
                    }
                    onListen(items)
                }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelCollectionRef.document(channelId)
                .collection("messages")
                .add(message)
        updateCounterRead(message.senderId, channelId, false)
    }

    fun readMessage(messageId: String, channelId: String) {
        chatChannelCollectionRef.document(channelId)
                .collection("messages")
                .document(messageId)
                .update("read", true)

        chatChannelCollectionRef.document(channelId)
                .collection("messages")
                .document(messageId)
                .get()
                .addOnSuccessListener {
                    updateCounterRead(it["senderId"].toString(), channelId, true)
                }
    }

    private fun updateCounterRead(
            senderId: String,
            channelId: String,
            clear: Boolean
    ) {
        chatChannelCollectionRef
                .document(channelId).get().addOnSuccessListener {
                    val channel = it.toObject(ChatChannel::class.java)!!
                    channel.userIds.forEach {
                        val reciverId = it
                        if (reciverId != senderId) {
                            firestoreInstance.document("users/${senderId}").get().addOnSuccessListener {
                                val user = it.toObject(User::class.java)!!
                                val counter = user.counter
                                val count = counter.getOrDefault(reciverId, 0)
                                if (clear) counter.put(reciverId, 0)
                                else counter.put(reciverId, count + 1)
                                firestoreInstance.document("users/${senderId}").update("counter", counter)
                            }
                        }
                    }
                }
    }

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
    }
    //endregion FCM

}