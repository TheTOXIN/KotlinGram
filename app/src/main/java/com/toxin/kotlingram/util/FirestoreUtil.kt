package com.toxin.kotlingram.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.toxin.kotlingram.model.User
import com.toxin.kotlingram.recyclerview.item.PersonItem

object FirestoreUtil {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UUID is NULL")}")

    fun initCurrentIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                        FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "",
                        null)
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
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                            items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                    }
                    onListen(items)
                }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

}