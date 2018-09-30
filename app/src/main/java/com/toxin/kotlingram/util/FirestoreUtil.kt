package com.toxin.kotlingram.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.toxin.kotlingram.model.User

object FirestoreUtil {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("user/${FirebaseAuth.getInstance().uid
                ?: throw NullPointerException("UUID is NULL")}")

    fun intiCurrentIfFirstTime(onComplete: () -> Unit) {
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

}