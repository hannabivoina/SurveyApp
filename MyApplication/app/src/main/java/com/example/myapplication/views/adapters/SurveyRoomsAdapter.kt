package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemRoomBinding
import com.example.myapplication.models.SurveyRoom
import com.example.myapplication.views.surveyRooms.RoomConnectInterface

class SurveyRoomsAdapter(val roomInterface: RoomConnectInterface): RecyclerView.Adapter<SurveyRoomsAdapter.ViewHolder>() {

    var roomsList = listOf<SurveyRoom>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SurveyRoomsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyRoomsAdapter.ViewHolder, position: Int) {
        val room = roomsList[position]
        holder.bind(room)
    }

    override fun getItemCount() = roomsList.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemRoomBinding.bind(view)
        fun bind(room: SurveyRoom){
            binding.itemRoomHostName.text = room.hostName
            binding.itemRoomNumber.text = room.surveyName
            binding.itemRoom.setOnClickListener {
                roomInterface.connectToSurveyRoom(room.hostName, room.surveyPort, room.hostApi)
            }
        }
    }

    fun updateRoomsList(list: List<SurveyRoom>){
        roomsList = list
        notifyDataSetChanged()
    }
}