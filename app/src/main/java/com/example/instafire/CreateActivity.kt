package com.example.instafire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instafire.databinding.ActivityCreateBinding
import com.example.instafire.models.Post
import com.example.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234

class CreateActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    private lateinit var firestoreDb: FirebaseFirestore
    private var signedInUser: User? = null
    private var photoUri: Uri? = null
    private lateinit var binding: ActivityCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create)

        storageRef = FirebaseStorage.getInstance().reference

        firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "failure fetching signed in user:$exception")
            }

        binding.btnPickImage.setOnClickListener {
            Log.i(TAG, "open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
            binding.btnSubmit.setOnClickListener {
                handleSubmitButtonClick()
            }
        }

    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null) {
            Toast.makeText(this, "no photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etDescription.text.isBlank()) {
            Toast.makeText(this, "description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedInUser == null) {
            Toast.makeText(this, "no signed in user,please wait", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnSubmit.isEnabled = false
        val photoReference = storageRef.child("images/${System.currentTimeMillis()}-photo.jpg")
        //upload photo to firebase storage
        photoReference.putFile(photoUri!!)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploaded bytes:${photoUploadTask.result?.bytesTransferred}")
                //retrieve image url of the uploaded image
                photoReference.downloadUrl

            }.continueWithTask { downloadUrlTask ->
                //create post object with image url and add that to the posts collection
                val post = Post(
                    binding.etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser
                )
                firestoreDb.collection("Posts").add(post)

            }.addOnCompleteListener { postCreationTask ->
                binding.btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "exception during firebase operations", postCreationTask.exception)
                    Toast.makeText(this, "failed to save post", Toast.LENGTH_SHORT).show()
                }
                binding.etDescription.text.clear()
                binding.imageView.setImageResource(0)
                Toast.makeText(this, "success!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                Log.i(TAG, "photoUri $photoUri")
                binding.imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "image pick acion canceled", Toast.LENGTH_SHORT).show()

            }
        }
    }
}