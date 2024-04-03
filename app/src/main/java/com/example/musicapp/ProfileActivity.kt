package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.databinding.ActivityProfileBinding
import com.example.musicapp.model.UserModel
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val userFetcher = UserFetcher()
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUserDetails()

        binding.update.setOnClickListener {
            val updatedUsername = binding.profileUsername.text.toString()

            if (updatedUsername.isNotEmpty()) {
                val updatedUserData = mapOf(
                    "username" to updatedUsername
                )

                updateUserDetails(updatedUserData)
            } else {
                Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backPro.setOnClickListener {
            startActivity(Intent(this@ProfileActivity,MainActivity::class.java))
        }

    }

    private fun fetchUserDetails() {
        lifecycleScope.launch {
            try {
                val userData = userFetcher.getUserDetails()
                if (userData != null) {
                    updateUI(userData)
                } else {
                    Log.e(TAG, "Error: User details not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user details", e)
            }
        }
    }

    private fun updateUI(userData: UserModel?) {
        if (userData != null) {
            if (userData.username.isNotEmpty() && userData.creditScore != null) {
                binding.profileUsername.setText(userData.username)
                binding.profileCreditScore.setText("Remaining Credit: ${userData.creditScore}")
            } else {
                Log.e(TAG, "Error: Missing keys in userData")
            }
        } else {
            Log.e(TAG, "Error: userData is null")
        }
    }

    private fun updateUserDetails(updatedUserData: Map<String, Any>) {
        lifecycleScope.launch {
            try {
                userFetcher.updateUserDetails(updatedUserData)
                Toast.makeText(this@ProfileActivity, "Username updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating username", e)
                Toast.makeText(this@ProfileActivity, "Failed to update username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "UserProfile"
    }
}
