package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instafire.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        //firebase authentication check
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            goPostActivity()
        }
        binding.buttonLogin.setOnClickListener {
            binding.buttonLogin.isEnabled = false
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "email/password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                binding.buttonLogin.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "!success", Toast.LENGTH_SHORT).show()
                    goPostActivity()
                } else {
                    Log.e(TAG, "sign in with email failed", task.exception)
                    Toast.makeText(this, "authentication failed", Toast.LENGTH_SHORT).show()

                }
            }

        }

    }

    private fun goPostActivity() {
        Log.i(TAG, "goPostActivity")
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}