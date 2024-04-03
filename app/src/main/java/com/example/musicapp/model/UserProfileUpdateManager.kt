package com.example.musicapp.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileUpdateManager {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun updateUserProfile(userModel: UserModel): Task<Void> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("users").document(uid).set(userModel)
        } else {
            Tasks.forException(Exception("User is not authenticated"))
        }
    }
}
