package com.example.myapplication.views.surveyRooms

import com.example.myapplication.models.SurveyRoom
import com.example.myapplication.models.SurveyRoomsRepo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SurveyRoomsVM(): ViewModel() {

    private val surveyRoomsRepo = SurveyRoomsRepo()
    var surveyRoomsLiveData = MutableLiveData<List<SurveyRoom>>()

    fun startListeningDirectoryMessages(){
//        viewModelScope.launch {
//            surveyRoomsRepo.readIncomingDirectoryMessages()
//        }

//        surveyRoomsRepo.listenThreadDir()
//        surveyRoomsLiveData.postValue(
//            listOf(SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
////                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
////                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
////                SurveyRoom("Alice", "222.222.222.222", "422", "3"),
//                SurveyRoom("Alice", "222.222.222.222", "422", "3"))
//        )
    }

    fun getAllOnlineRooms(name: String){
        viewModelScope.launch {
            surveyRoomsRepo.queryForPears(name)
        }
    }

    fun subscribe(){
        surveyRoomsRepo.roomsLiveData.observeForever{
            surveyRoomsLiveData.postValue(it)
        }
    }
}