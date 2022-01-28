package com.example.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instafire.databinding.ItemPostsBinding
import com.example.instafire.models.Post
import java.math.BigInteger
import java.security.MessageDigest

class PostAdapter (val context: Context,val posts:List<Post>):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPostsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount()=posts.size
    
    inner class ViewHolder(val Binding:ItemPostsBinding) :RecyclerView.ViewHolder(Binding.root) {
        fun bind(post: Post) {
            val username=post.user?.username as String
            Binding.tvUsername.text=username
            Binding.tvDescription.text=post.description
            Glide.with(context).load(post.imageUrl).into(Binding.ivPost)
            Glide.with(context).load(getProfileImageUrl(username)).into(Binding.ivPofileImage)
            Binding.tvRelativeTime.text=DateUtils.getRelativeTimeSpanString(post.creationTimeMs)

        }
        private fun getProfileImageUrl(username:String):String{
            val  digest = MessageDigest.getInstance("MD5")
            val  hash = digest.digest(username.toByteArray())
           val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16)
        return "https://www.gravatar.com/avatar/$hex ?d=identicon"
        }
    }
}

