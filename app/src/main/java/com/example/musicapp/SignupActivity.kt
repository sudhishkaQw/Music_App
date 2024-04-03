package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.createButton.setOnClickListener {
            val email=binding.emailAddress.text.toString()
            val pass =binding.password.text.toString()
            val conPass = binding.confirmPassword.text.toString()

            if(!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(),email))
            {
                binding.emailAddress.setError("Invalid email")
                return@setOnClickListener
            }
            if(pass.length<6)
            {
                binding.password.setError("Password should be atleast 6 char")
                return@setOnClickListener
            }
            if(!pass.equals(conPass))
            {
                binding.confirmPassword.setError("Password doesn't matches")
                return@setOnClickListener
            }
            createAccountWithFirebase(email,pass)

        }
        binding.already.setOnClickListener {
            startActivity(Intent(this@SignupActivity,LoginActivity::class.java))
        }
    }

    private fun createAccountWithFirebase(email: String, pass: String) {
        setProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                setProgress(false)
                Toast.makeText(applicationContext, "User created successfully", Toast.LENGTH_SHORT).show()

                // Add the user to Firestore
                val userId = authResult.user?.uid
                if (userId != null) {
                    val user = hashMapOf(
                        "email" to email,
                        // Add any other user information you want to store
                    )
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "User added to Firestore successfully", Toast.LENGTH_SHORT).show()
                            // Proceed to the next activity or perform any other action
                            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(applicationContext, "Failed to add user to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                setProgress(false)
                Toast.makeText(applicationContext, "Create account failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            binding.createButton.visibility=View.GONE
            binding.progressBar.visibility=View.VISIBLE
        }
        else
        {
            binding.createButton.visibility=View.VISIBLE
            binding.progressBar.visibility=View.GONE
        }
    }
}