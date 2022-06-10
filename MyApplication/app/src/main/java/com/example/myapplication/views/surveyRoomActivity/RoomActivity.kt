package com.example.myapplication.views.surveyRoomActivity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityRoomBinding
import com.example.myapplication.models.data.Question
import com.example.myapplication.models.data.Survey
import com.example.myapplication.views.adapters.AnswersSurveyAdapter
import com.example.myapplication.views.adapters.RoomConnectedUsersAdapter
import com.example.myapplication.views.mainActivity.*
import com.google.firebase.database.*

class RoomActivity: AppCompatActivity() {

    var _binding: ActivityRoomBinding? = null
    val binding get() = _binding!!

    val answersAdapter = AnswersSurveyAdapter()
    val connectedUsersAdapter = RoomConnectedUsersAdapter()
    private lateinit var databaseReference: DatabaseReference
    private val viewModel = RoomVM()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setIpAddressValue(intent.getStringExtra(IP_KEY))
        viewModel.setUserNameValue(intent.getStringExtra(USERNAME_KEY))
        viewModel.observeRoomMessages()

        binding.createRoomName.text = viewModel.userName

        setRoomMode()

        databaseReference = FirebaseDatabase.getInstance()
            .getReference(SURVEY_KEY)

        binding.roomAnswerButton.setOnClickListener {
            if (viewModel.requiredSurvey != null){
                val currentQuestion = viewModel.getCurrentQuestion()
                if (currentQuestion != null) {
                    answersAdapter.updateQuestion(currentQuestion)
                    println("*********")
                    println(currentQuestion.toString())
                    updateQuestionNumberText(viewModel.currentQuestionId, viewModel.requiredSurvey!!.questions!!.size)
                    answerMessage()
                    if (viewModel.currentQuestionId == viewModel.requiredSurvey!!.questions!!.size){
                        binding.roomAnswerButton.visibility = View.GONE
                        binding.roomAnswerFinishButton.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.roomAnswerFinishButton.setOnClickListener {
            viewModel.sendMessage("finishSurvey")
            binding.createRoomCardLay.visibility = View.GONE
            binding.roomQuestionsDataLay.visibility = View.GONE
            binding.roomJoinWaitingText.text = "Thank you for participating in the Survey"
            binding.roomJoinWaitingText.visibility = View.VISIBLE
        }

        binding.roomCloseButton.setOnClickListener {
            println("kkkkkkkk")
            disconnectMessage()
        }

        viewModel.observeRoomConnectedUsers()
        viewModel.connectedUsers.observe(this){ usersList->
            connectedUsersAdapter.updateConnectedUsers(usersList.map { it[0] })
            println("iiiiiiiiiiiiiiiii")
            for (i in usersList){
                println(i)
            }
        }

        binding.createRoomButton.setOnClickListener {
            createSurveyRoom()
        }

        binding.startSurveyButton.setOnClickListener {
            shareSurveyMessage()
        }

        binding.roomJoinWaitingText.setOnClickListener {
            viewModel.sendMessage("hello")
        }

        viewModel.startSurveyLiveData.observe(this){
            binding.createRoomCardLay.visibility = View.GONE
            binding.roomQuestionsDataLay.visibility = View.VISIBLE
            binding.roomJoinWaitingText.visibility = View.GONE
            getSurveyFromDB(it)
        }

        viewModel.isHostMutableLiveData.observe(this){ isHost ->
//            Toast.makeText(this, "jsjjsj", Toast.LENGTH_SHORT).show()
            if (isHost) binding.roomCloseForAllButton.visibility = View.VISIBLE
            else binding.roomCloseForAllButton.visibility = View.GONE
        }

        binding.roomCloseForAllButton.setOnClickListener{
//            disconnectMessage()
//            Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show()
            viewModel.sendMessage("hhhhhhhhhhhhhhhhhh")
        }

        viewModel.clientAnswersLiveData.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRoomMode(){
        val roomMode = intent.getStringExtra(ROOM_MODE_KEY)
        viewModel.roomMode = roomMode
        if (roomMode != null && roomMode == "create"){
            binding.createRoomCardLay.visibility = View.VISIBLE
            binding.roomQuestionsDataLay.visibility = View.GONE
            binding.roomJoinWaitingText.visibility = View.GONE
            setupOnlineUsersRecyclerView()
        } else {
            val hostName = intent.getStringExtra(ROOM_HOST_NAME)
            val hostIp = intent.getStringExtra(ROOM_HOST_IP)
            val hostPort = intent.getStringExtra(ROOM_HOST_PORT)

            binding.createRoomCardLay.visibility = View.GONE
            binding.roomQuestionsDataLay.visibility = View.GONE
            binding.roomJoinWaitingText.visibility = View.VISIBLE
            viewModel.connectToSurveyRoom(hostName, hostPort, hostIp)
        }
    }

    private fun createSurveyRoom(){
        binding.createRoomConnectedUsersLay.visibility = View.VISIBLE
        val surveyName = binding.createRoomSurveyIdText.text.toString()
        viewModel.createRoom(surveyName)
        binding.createRoomButton.visibility = View.GONE
        binding.startSurveyButton.visibility = View.VISIBLE
    }

    private fun getSurveyFromDB(name: String){
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in dataSnapshot.children){
                    val survey = i.getValue(Survey::class.java) as Survey
                    if (survey.name == name){
                        viewModel.requiredSurvey = survey
                        setupQuestionsRecyclerView()
                        if (viewModel.requiredSurvey != null){
                            val requiredSurvey = viewModel.requiredSurvey!!
                            val currentQuestion = viewModel.getCurrentQuestion()
                            if (currentQuestion != null) {
                                answersAdapter.updateQuestion(currentQuestion)
                                updateQuestionNumberText(1, requiredSurvey.questions!!.size)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        }
        databaseReference?.addValueEventListener(valueEventListener)
    }

    private fun shareSurveyMessage(){
        val surveyName = binding.createRoomSurveyIdText.text
        if (!surveyName.isNullOrEmpty()){
            val message = "survey:$surveyName"
            viewModel.sendMessage(message)
        }
    }

    private fun disconnectMessage(){
        if(viewModel.isHost){
            viewModel.hostDisconnect()
        } else {
            viewModel.sendMessage("disconnect")
            viewModel.clientDisconnect()
        }
    }

    private fun answerMessage(){
        val message = "answer:${viewModel.currentQuestionId-1}"
        viewModel.sendMessage(message)
    }

    private fun setupQuestionsRecyclerView(){
        binding.roomQuestionsRecyclerView.apply {
            adapter = answersAdapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
        }
    }

    private fun setupOnlineUsersRecyclerView(){
        binding.createRoomConnectedUsersRecyclerView.apply {
            adapter = connectedUsersAdapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
        }
    }

    private fun updateQuestionNumberText(questionNumber: Int, questionListSize: Int){
        val questionText = "Question $questionNumber from $questionListSize"
        binding.roomQuestionsNumberText.text = questionText
    }

    override fun onDestroy() {
        disconnectMessage()
        super.onDestroy()

        _binding = null
    }
}