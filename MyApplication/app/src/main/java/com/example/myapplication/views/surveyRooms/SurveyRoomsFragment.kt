package com.example.myapplication.views.surveyRooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.common.contract
import com.example.myapplication.databinding.FragmentSurveyRoomsBinding
import com.example.myapplication.models.SurveyRoom
import com.example.myapplication.views.adapters.SurveyRoomsAdapter

interface RoomConnectInterface{
    fun connectToSurveyRoom(hostName: String, hostPort: String, hostApi: String)
}

class SurveyRoomsFragment: Fragment() {
    private var _binding: FragmentSurveyRoomsBinding? = null
    private val binding get() = _binding!!

    private val viewModel = SurveyRoomsVM()
    private val roomsAdapter = SurveyRoomsAdapter(object : RoomConnectInterface{
        override fun connectToSurveyRoom(hostName: String, hostPort: String, hostApi: String) {
            val userName = contract()?.getUserName()
            if (userName != null)
                contract()?.goToJoinRoomActivity(hostName, hostApi, hostPort)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSurveyRoomsBinding.inflate(inflater, container, false)

        val userName = contract()?.getUserName()

        viewModel.startListeningDirectoryMessages()
        viewModel.subscribe()
        if (!userName.isNullOrEmpty()){
            viewModel.getAllOnlineRooms(userName)
        }

        binding.surveyRoomsRecycler.adapter = roomsAdapter
        binding.surveyRoomsRecycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.surveyRoomsLiveData.observe(viewLifecycleOwner){
            roomsAdapter.updateRoomsList(it)
        }

//        roomsAdapter.updateRoomsList(listOf<SurveyRoom>(
//            SurveyRoom("Mark", "18", "", "German Language Level"),
//            SurveyRoom("Pasha", "18", "", "History Survey"),
//            SurveyRoom("Hanna", "18", "", "Present Simple, part 1"),
//            SurveyRoom("Nino", "18", "", "Favorite things"),
//            SurveyRoom("User18", "18", "", "Future"),
//            SurveyRoom("Karl", "18", "", "Who is Karl?")
//        ))

        binding.surveyRoomAdd.setOnClickListener {
            contract()?.goToCreateRoomActivity(userName!!)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}