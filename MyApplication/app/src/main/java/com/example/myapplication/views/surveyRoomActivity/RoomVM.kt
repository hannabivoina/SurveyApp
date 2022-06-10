package com.example.myapplication.views.surveyRoomActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.SurveyRoomsRepo
import com.example.myapplication.models.data.Question
import com.example.myapplication.models.data.Survey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class RoomVM : ViewModel() {
    val surveyRoomsRepo = SurveyRoomsRepo()
    val connectedUsers = MutableLiveData<List<List<String>>>()

    var userName: String = ""
    var ipAddress: String = ""

    var roomMode: String? = null

    var isSurveyStart = false

    var requiredSurvey: Survey? = null
    var currentQuestionId = 0
    var isHost = false
    val isHostMutableLiveData = MutableLiveData<Boolean>()
    var roomPort = 0

    var startSurveyLiveData = MutableLiveData<String>()
    val clientAnswersLiveData = MutableLiveData<String>()

    var currentJob: Job? = null

    fun setIpAddressValue(address: String?) {
        ipAddress = address ?: ""
    }

    fun setUserNameValue(name: String?) {
        userName = name ?: ""
    }

    fun getCurrentQuestion(): Question? {
        val currentQuestion = requiredSurvey?.questions?.get(currentQuestionId)
        currentQuestionId += 1
        return currentQuestion
    }

    fun createRoom(surveyName: String) {
        if (ipAddress.isNotEmpty() && userName.isNotEmpty()) {
            roomPort = Random.nextInt(1000, 9999)
            viewModelScope.launch {
                surveyRoomsRepo.createRoom(ipAddress, userName, roomPort, surveyName)
            }
            isHost = true
            isHostMutableLiveData.postValue(true)
        }
    }

    fun connectToSurveyRoom(hostName: String?, hostPort: String?, hostIp: String?) {
        if (hostName != null && hostPort != null && hostIp != null) {
            currentJob = viewModelScope.launch {
                surveyRoomsRepo.joinRoom(userName, hostName, hostPort, hostIp, ipAddress)
            }
            isHost = false
            isHostMutableLiveData.postValue(false)
        }
    }

    fun observeRoomConnectedUsers() {
        surveyRoomsRepo.connectedUsersLiveData.observeForever { it ->
            for (i in it){
                println("-------------------++++++++++++++++++++++")
                println(i.toString())
            }
            connectedUsers.postValue(it)
        }
    }

    fun sendMessage(message: String) {
        val newMessage = "${userName}:$message"
        viewModelScope.launch {
            surveyRoomsRepo.sendMessageToAllClients(newMessage)
        }
    }

    fun observeRoomMessages() {
        surveyRoomsRepo.roomMessageLiveData.observeForever {
            parseMessage(it)
        }
    }

    private fun parseMessage(messageData: String) {
        val messageArray = messageData.split(":").toTypedArray()
        val messageName = messageArray[0]
        val messageMode = messageArray[1]
        when (messageMode) {
            "survey" -> {
                startSurveyLiveData.postValue(messageArray[2])
                isSurveyStart = true
            }
            "answer" -> showClientAnswerMessage(messageArray)
            "hostDisconnect" -> changeHost(messageArray)
            "finishSurvey" -> showClientFinishAnswerMessage(messageArray)
        }
    }

    private fun changeHost(messageData: Array<String>) {
        val newHostName = messageData[3]
        val newHostIp = messageData[4]
        val roomPort = messageData[5]
        viewModelScope.launch {
            isHost = newHostName==userName
            isHostMutableLiveData.postValue(isHost)
            surveyRoomsRepo.changeHost(newHostName==userName, newHostIp, roomPort.toInt())
        }
        println("-------------------------------------------------")
        isHost = newHostName==userName
        isHostMutableLiveData.postValue(isHost)
    }

    private fun showClientAnswerMessage(messageArray: Array<String>){
         if (messageArray[0] != userName){
             val clientAnswer = "${messageArray[0]} answered ${messageArray[2]} questions"
             clientAnswersLiveData.postValue(clientAnswer)
         }
    }

    private fun showClientFinishAnswerMessage(messageArray: Array<String>){
        if (messageArray[0] != userName){
            val clientAnswer = "${messageArray[0]} finish survey"
            clientAnswersLiveData.postValue(clientAnswer)
        }
    }

fun goOffline() {
    viewModelScope.launch {
        surveyRoomsRepo.goOffline(userName)
    }
}

fun clientDisconnect() {
    viewModelScope.launch {
        surveyRoomsRepo.clientDisconnect()
    }
//    currentJob = null
//        goOffline()
}

fun hostDisconnect() {
    if (!connectedUsers.value.isNullOrEmpty()){
        val newHostName = connectedUsers.value?.get(0)?.get(0)
        val newHostIp = connectedUsers.value?.get(0)?.get(1)
        sendMessage("hostDisconnect:newHost:${newHostName}:${newHostIp}:${roomPort}")
        clientDisconnect()
    }

//        viewModelScope.launch {
//
//        }
}
}