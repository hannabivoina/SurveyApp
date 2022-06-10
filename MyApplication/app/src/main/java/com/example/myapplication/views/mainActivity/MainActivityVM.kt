package com.example.myapplication.views.mainActivity

import com.example.myapplication.models.SurveyRoomsRepo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainActivityVM: ViewModel() {
    private var userName = ""
    private var surveyRoomsRepo = SurveyRoomsRepo()

    fun setUserName(name: String){
        userName = name
    }

    fun getUserName() = userName

    fun goOnline(name: String){
        userName = name
        viewModelScope.launch {
            surveyRoomsRepo.goOnline(userName)
        }
    }

    fun goOffline(){
        viewModelScope.launch {
            surveyRoomsRepo.goOffline(userName)
        }
    }
}