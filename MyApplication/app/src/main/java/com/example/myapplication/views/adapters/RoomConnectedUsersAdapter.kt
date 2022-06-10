package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemConnectedUserBinding

class ConnectedUsersDiffCallback(
    private val oldList: List<String>,
    private val newList: List<String>
): DiffUtil.Callback(){
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSearch = oldList[oldItemPosition]
        val newSearch = newList[newItemPosition]

        return oldSearch == newSearch
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSearch = oldList[oldItemPosition]
        val newSearch = newList[newItemPosition]

        return oldSearch == newSearch
    }
}

class RoomConnectedUsersAdapter(): RecyclerView.Adapter<RoomConnectedUsersAdapter.ViewHolder>() {

    private var connectedUsersList = listOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connected_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = connectedUsersList[position]
        holder.bind(user)
    }

    override fun getItemCount() = connectedUsersList.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = ItemConnectedUserBinding.bind(view)

        fun bind(user: String){
            binding.connectedUserName.text = user
        }
    }

    fun updateConnectedUsers(newUsersList: List<String>){
//        val diffCallback = ConnectedUsersDiffCallback(connectedUsersList, newUsersList)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        connectedUsersList.clear()
//        connectedUsersList.addAll(newUsersList)
//
//        diffResult.dispatchUpdatesTo(this)

        connectedUsersList = newUsersList
        notifyDataSetChanged()
    }
}