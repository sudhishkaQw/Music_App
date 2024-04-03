package com.example.musicapp

import android.util.Log
import com.example.musicapp.model.SongsModel
import com.example.musicapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserFetcher {


    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    suspend fun addNewUser(userData: Map<String, Any>) {
        // Get the current user's ID from Firebase Authentication
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        // Access the "users" collection in Firestore and add a new document with the current user's ID
        db.collection("users").document(userId).set(userData).await()
    }



    fun deductCreditScoreFromUser(
        requiredCreditScore: Long,
        onSuccess: (Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get the current user's document reference
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: return)

        // Update the user's credit score by deducting the required credit score
        userDocRef.get()
            .addOnSuccessListener { document ->
                val currentCreditScore = document.get("creditScore") as Long
                val newCreditScore = currentCreditScore - requiredCreditScore

                // Update the user's document with the new credit score
                userDocRef.update("creditScore", newCreditScore)
                    .addOnSuccessListener {
                        // Credit score updated successfully
                        onSuccess(newCreditScore)
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to update credit score
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                // Handle failure to fetch user document
                onFailure(e)
            }
    }

    fun updateCreditScore(
        newCreditScore: Long,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get the current user's document reference
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: return)

        // Update the user's document with the new credit score
        userDocRef.update("creditScore", newCreditScore)
            .addOnSuccessListener {
                // Credit score updated successfully
                onSuccess()
            }
            .addOnFailureListener { e ->
                // Handle failure to update credit score
                onFailure(e)
            }
    }

    fun fetchFavoriteSongs(
        userId: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Reference to the user document in Firestore
        val userRef = db.collection("users").document(userId)

        // Fetch the user document
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve the list of favorite songs from the user document
                    val favoriteSongs = document.get("favoriteSongs") as? List<String> ?: emptyList()
                    onSuccess(favoriteSongs)
                } else {
                    onFailure(Exception("User document does not exist"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private suspend fun fetchSongDetails(songId: String): SongsModel? {
        return try {
            if (songId.isNotBlank()) {
                val documentSnapshot = db.collection("songs").document(songId).get().await()
                documentSnapshot.toObject(SongsModel::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching song details", e)
            null
        }
    }


    suspend fun fetchPurchasedSongsForCurrentUser(
        onSuccess: (List<Pair<SongsModel, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userDocRef = db.collection("users").document(userId).get().await()
                if (userDocRef.exists()) {
                    val user = userDocRef.toObject(UserModel::class.java)
                    user?.let { userModel ->
                        val purchasedSongsIds = userModel.purchasedSongs.filter { it.isNotBlank() }
                        val purchasedSongs = mutableListOf<SongsModel>()

                        // Log the retrieved purchased songs
                        Log.d(TAG, "Purchased Songs IDs: $purchasedSongsIds")

                        // Fetch details of each purchased song
                        purchasedSongsIds.forEach { songId ->
                            val song = fetchSongDetails(songId)
                            song?.let { purchasedSongs.add(it) }
                        }

                        // Map the purchased songs to pairs with their cover URLs
                        val purchasedSongCovers: List<Pair<SongsModel, String>> = purchasedSongs.map { it to (it.coverUrl ?: "") }

                        onSuccess(purchasedSongCovers)
                    } ?: onFailure(Exception("Failed to parse user data"))
                } else {
                    onFailure(Exception("User document does not exist"))
                }
            } else {
                onFailure(Exception("User ID not found"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }


    suspend fun getUserDetails(): UserModel? {
        // Get the current user's ID from Firebase Authentication
        val userId = auth.currentUser?.uid ?: return null

        // Retrieve user details from Firestore
        val documentSnapshot = db.collection("users").document(userId).get().await()
        return documentSnapshot.toObject(UserModel::class.java)
    }

    companion object {
        private const val TAG = "UserFetcher"
    }



    //    fun getUserDetails(onSuccess: (Map<String, Any>) -> Unit, onFailure: (Exception) -> Unit) {
//        // Get the current user's ID from Firebase Authentication
//        val userId = auth.currentUser?.uid ?: return
//
//        // Retrieve user details from Firestore
//        db.collection("users").document(userId)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val userData = documentSnapshot.data
//                    onSuccess(userData ?: mapOf()) // Pass user data to success callback
//                } else {
//                    onFailure(Exception("User document does not exist"))
//                }
//            }
//            .addOnFailureListener { e ->
//                onFailure(e)
//            }
//    }
    suspend fun updateUserDetails(
        updatedUserData: Map<String, Any>,
        creditScoreDeduction: Long = 0L
    ) {
        // Get the current user's ID from Firebase Authentication
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        // Update the user's document in Firestore with the provided data
        db.collection("users").document(userId).update(updatedUserData).await()

        // Deduct credit score if creditScoreDeduction is specified
        if (creditScoreDeduction > 0L) {
            val userDocRef = db.collection("users").document(userId)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    val currentCreditScore = document.get("creditScore") as Long
                    val newCreditScore = currentCreditScore - creditScoreDeduction

                    // Update the user's document with the new credit score
                    userDocRef.update("creditScore", newCreditScore)
                        .addOnFailureListener { e ->
                            // Handle failure to update credit score
                            Log.e(TAG, "Error deducting credit score", e)
                        }
                }
                .addOnFailureListener { e ->
                    // Handle failure to fetch user document
                    Log.e(TAG, "Error fetching user document", e)
                }
        }
    }

    fun purchaseSong(
        song: SongsModel,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get the current user's document reference
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: return)

        // Deduct credits from the user's credit score for purchasing the song
        val requiredCreditScore = 10L // Assuming each song costs 10 credits
        userDocRef.get()
            .addOnSuccessListener { document ->
                val currentCreditScore = document.getLong("creditScore") ?: 0
                if (currentCreditScore >= requiredCreditScore) {
                    // Sufficient credits, proceed with the purchase
                    val newCreditScore = currentCreditScore - requiredCreditScore

                    // Update the user's document with the new credit score and add the purchased song
                    val purchasedSongs = document.get("purchasedSongs") as? List<String> ?: emptyList()
                    val newPurchasedSongs = purchasedSongs + song.id

                    userDocRef.update(
                        mapOf(
                            "creditScore" to newCreditScore,
                            "purchasedSongs" to newPurchasedSongs
                        )
                    )
                        .addOnSuccessListener {
                            // Credit score updated and song purchased successfully
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to update user document
                            onFailure(e)
                        }
                } else {
                    // Insufficient credits, notify the caller
                    onFailure(IllegalStateException("Insufficient credits to purchase the song"))
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to fetch user document
                onFailure(e)
            }
    }


    suspend fun fetchRemainingCredit(): Long {
        return try {
            val userId = auth.currentUser?.uid ?: return 0L
            val userDocRef = db.collection("users").document(userId).get().await()
            userDocRef.getLong("creditScore") ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching remaining credit", e)
            0L
        }
    }
    suspend fun getUserCreditScore(): Long {
        try {
            // Get the current user's ID from Firebase Authentication
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

            // Retrieve user details from Firestore
            val documentSnapshot = db.collection("users").document(userId).get().await()
            return documentSnapshot.getLong("creditScore") ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user credit score", e)
            return 0L
        }
    }

    // In UserFetcher class
//    suspend fun fetchFavoriteSongs(
//        onSuccess: (List<String>) -> Unit,
//        onFailure: (Exception) -> Unit
//    ) {
//        // Get the current user's ID
//        val userId = auth.currentUser?.uid
//        if (userId != null) {
//            // Fetch the user document from Firestore
//            db.collection("users").document(userId).get()
//                .addOnSuccessListener { document ->
//                    if (document.exists()) {
//                        // Retrieve the list of favorite songs from the user document
//                        val favoriteSongs = document.get("favoriteSongs") as? List<String> ?: emptyList()
//                        onSuccess(favoriteSongs)
//                    } else {
//                        onFailure(Exception("User document does not exist"))
//                    }
//                }
//                .addOnFailureListener { e ->
//                    onFailure(e)
//                }
//        } else {
//            onFailure(IllegalStateException("User ID not found"))
//        }
//    }

    // Function to add a song to favorites
    fun addSongToFavorites(
        userId: String,
        songId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userDocRef = db.collection("users").document(userId)

        // Update the favoriteSongs field of the user document
        userDocRef.update("favoriteSongs", FieldValue.arrayUnion(songId))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun isSongPurchased(songId: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val purchasedSongs = documentSnapshot.get("purchasedSongs") as? List<String> ?: emptyList()
                        val isPurchased = purchasedSongs.contains(songId)
                        onSuccess(isPurchased)
                    } else {
                        onFailure(IllegalStateException("User document does not exist"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(IllegalStateException("User not authenticated"))
        }
    }


    suspend fun fetchFavoriteSongsForCurrentUser(): List<SongsModel> {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val userDocRef = db.collection("users").document(userId)
        val documentSnapshot = userDocRef.get().await()

        if (documentSnapshot.exists()) {
            val favoriteSongIds = documentSnapshot.get("favoriteSongs") as? List<String> ?: emptyList()
            val favoriteSongs = mutableListOf<SongsModel>()

            for (songId in favoriteSongIds) {
                val song = fetchSongDetails(songId)
                song?.let { favoriteSongs.add(it) }
            }

            return favoriteSongs
        } else {
            throw IllegalStateException("User document does not exist")
        }
    }

    // Function to remove a song from favorites
    fun removeSongFromFavorites(
        songId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Update the user document to remove the song from favorites
            db.collection("users").document(userId)
                .update("favoriteSongs", FieldValue.arrayRemove(songId))
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(IllegalStateException("User not authenticated"))
        }
    }
    suspend fun fetchPurchasedSongIdsForCurrentUser(): List<String> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            if (userDocRef.exists()) {
                val user = userDocRef.toObject(UserModel::class.java)
                return user?.purchasedSongs ?: emptyList()
            }
        }
        return emptyList()
    }
}

    // Other existing methods...







    // Function to fetch the user's credit score

    // Function to fetch purchased songs for the current user














