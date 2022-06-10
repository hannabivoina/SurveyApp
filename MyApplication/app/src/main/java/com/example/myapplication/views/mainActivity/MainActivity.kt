package com.example.myapplication.views.mainActivity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.common.AppContract
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.views.CreateSurveyFragment
import com.example.myapplication.views.QuestionItemFragment
import com.example.myapplication.views.StartFragment
import com.example.myapplication.views.SurveysFragment
import com.example.myapplication.views.surveyRoomActivity.RoomActivity
import com.example.myapplication.views.surveyRooms.SurveyRoomsFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

const val USERNAME_KEY = "USERNAME"

const val SURVEY_KEY = "Survey"
const val IP_KEY = "IP"
const val ROOM_MODE_KEY = "ROOM_MODE"
const val ROOM_HOST_NAME = "ROOM_HOST_NAME"
const val ROOM_HOST_PORT = "ROOM_HOST_PORT"
const val ROOM_HOST_IP = "ROOM_HOST_IP"

class MainActivity : AppCompatActivity(), AppContract {

    var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference

    val viewModel = MainActivityVM()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance()
            .getReference(SURVEY_KEY)

        binding.mainAppBar.topBarSaveButton.setOnClickListener{

        }

        setupMenu()
        goToStartFragment()
    }

    private fun setupMenu() {
        binding.mainBottomBar.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.surveyRoomsFragment -> goToSurveyRooms()
                    R.id.addSurveyFragment -> goToCreateSurvey()
                    R.id.allSurveysFragment -> goToAllSurveys()
                }
                true
            }
        }
    }

    override fun goToSurveyRooms() {
        binding.mainBottomBar.visibility = View.VISIBLE
        binding.mainAppBar.topBarLay.visibility = View.VISIBLE
        binding.mainAppBar.topBarSaveButton.visibility = View.GONE

        setTopBarTitle("Online Survey Rooms")

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainContainerLay, SurveyRoomsFragment())
            .commit()
    }

    override fun getDataBaseInstance(): DatabaseReference = databaseReference

    override fun goToAllSurveys() {
        setTopBarTitle("All Surveys")
        binding.mainAppBar.topBarSaveButton.visibility = View.GONE

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainContainerLay, SurveysFragment())
            .commit()
    }

    override fun goToCreateSurvey() {
        setTopBarTitle("Create Survey")
        binding.mainAppBar.topBarSaveButton.visibility = View.VISIBLE

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainContainerLay, CreateSurveyFragment())
            .commit()
    }

    override fun goToCreateRoomActivity(userName: String) {
        val intent = Intent(this, RoomActivity::class.java)
        intent.putExtra(USERNAME_KEY, viewModel.getUserName())
        intent.putExtra(IP_KEY, getLocalIpAddress())
        intent.putExtra(ROOM_MODE_KEY, "create")
        startActivity(intent)
    }

    override fun goToJoinRoomActivity(hostName: String, hostIP: String, hostPort: String) {
        val intent = Intent(this, RoomActivity::class.java)
        intent.putExtra(USERNAME_KEY, viewModel.getUserName())
        intent.putExtra(IP_KEY, getLocalIpAddress())
        intent.putExtra(ROOM_MODE_KEY, "join")
        intent.putExtra(ROOM_HOST_IP, hostIP)
        intent.putExtra(ROOM_HOST_PORT, hostPort)
        intent.putExtra(ROOM_HOST_NAME, hostName)
        startActivity(intent)
    }

    override fun goToStartFragment() {
        binding.mainBottomBar.visibility = View.GONE
        binding.mainAppBar.topBarLay.visibility = View.GONE

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainContainerLay, StartFragment())
            .commit()
    }

    override fun addQuestion(questionId: Int): QuestionItemFragment {
        val tag = "Q$questionId"
        val fragment = QuestionItemFragment(tag, questionId)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.createSurveyQuestionsLay, fragment)
            .commit()

        return fragment
    }

    override fun setUserName(name: String) {
        viewModel.setUserName(name)
    }

    override fun getUserName() = viewModel.getUserName()

    override fun getLocalIpAddress(): String {
        val wm = applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    override fun getSurveyButton() = binding.mainAppBar.topBarSaveButton

    override fun goOnline(name: String) {
        viewModel.goOnline(name)
    }

    override fun goOffline() {
        viewModel.goOffline()
    }

    private fun setTopBarTitle(title: String) {
        binding.mainAppBar.topBarTitle.text = title
    }

    override fun onDestroy() {
        super.onDestroy()

        goOffline()
        _binding = null
    }
}