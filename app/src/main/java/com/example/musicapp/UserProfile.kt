
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.UserFetcher
import com.example.musicapp.databinding.ActivityUserProfileBinding


class UserProfile : AppCompatActivity() {

    private val userFetcher = UserFetcher()
    private lateinit var binding:ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
    }}
//        fetchUserDetails()
//

//        binding.update.setOnClickListener {
//            // Get updated username and credit score from EditText fields
//            val updatedUsername = binding.profileUsername.text.toString()
//            val updatedCreditScore = binding.profileCreditScore.text.toString().toIntOrNull()
//
//            // Ensure both fields are not empty and credit score is valid
//            if (updatedUsername.isNotEmpty() && updatedCreditScore != null) {
//                // Construct a map with updated user data
//                val updatedUserData = mapOf(
//                    "username" to updatedUsername,
//                    "creditScore" to updatedCreditScore
//                )}

//                // Call the function to update user details in Firestore
//                userFetcher.addNewUser(
//                    updatedUserData,
//                    onSuccess = {
//                        // Handle success, such as showing a success message
//                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
//                    },
//                    onFailure = { exception ->
//                        // Handle failure, such as displaying an error message
//                        // For simplicity, we'll just log the error here
//                        Log.e(TAG, "Error updating user profile", exception)
//                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
//                    }
//                )
//            } else {
//                // Display an error message if any field is empty or credit score is invalid
//                Toast.makeText(this, "Please enter valid username and credit score", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun fetchUserDetails() {
//        // Call the function to fetch user details from Firestore
//        userFetcher.getUserDetails(
//            onSuccess = { userData ->
//                // Update UI with user data
//                updateUI(userData)
//            },
//            onFailure = { exception ->
//                // Handle failure, such as displaying an error message
//                // For simplicity, we'll just log the error here
//                Log.e(TAG, "Error fetching user details", exception)
//            }
//        )
//    }
//
//    private fun updateUI(userData: Map<String, Any>) {
//        // Update UI elements with user data
//        val username = userData["username"] as String
//        val creditScore = userData["creditScore"] as Int
//
//        binding.profileUsername.setText(username)
//        binding.profileCreditScore.setText(creditScore.toString())
//    }
//
//    companion object {
//        private const val TAG = "UserProfile"
//    }
//}
