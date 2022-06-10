package com.example.myapplication.common

import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.models.data.Survey
import com.example.myapplication.views.QuestionItemFragment
import com.google.firebase.database.DatabaseReference

fun Fragment.contract() : AppContract? = requireActivity() as? AppContract

interface AppContract {
    fun goToSurveyRooms()
    fun goToCreateSurvey()
    fun goToAllSurveys()
    fun getDataBaseInstance(): DatabaseReference
    fun goToCreateRoomActivity(userName: String)
    fun getLocalIpAddress(): String
    fun goToStartFragment()
    fun setUserName(name: String)
    fun getUserName(): String
    fun goOnline(name: String)
    fun goOffline()
    fun goToJoinRoomActivity(hostName: String, hostIP: String, hostPort: String)
    fun addQuestion(questionId: Int): QuestionItemFragment
    fun getSurveyButton(): ImageButton
}