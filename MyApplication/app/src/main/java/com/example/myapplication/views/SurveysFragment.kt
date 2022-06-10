package com.example.myapplication.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.common.contract
import com.example.myapplication.databinding.FragmentSurveysBinding
import com.example.myapplication.models.data.Survey
import com.example.myapplication.views.adapters.SurveysAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class SurveysFragment: Fragment() {
    private var _binding: FragmentSurveysBinding? = null
    private val binding get() = _binding!!
    val surveysAdapter = SurveysAdapter()
    var databaseReference: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSurveysBinding.inflate(inflater, container, false)

        setupRecyclerView()
        databaseReference = contract()?.getDataBaseInstance()
        getSurveysFromDB()

        return binding.root
    }

    private fun setupRecyclerView(){
        binding.surveysRecycler.apply {
            adapter = surveysAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getSurveysFromDB(){
        val valueEventListener = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val surveysList = ArrayList<Survey>()
//                println(surveysList.toString())
                for (i in dataSnapshot.children){
                    val survey = i.getValue(Survey::class.java) as Survey
                    surveysList.add( survey)
                    println(survey.toString())
//                    println(i.getValue(Survey::class.java).toString())
                }
                surveysAdapter.updateRoomsList(surveysList.reversed())
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        databaseReference?.addValueEventListener(valueEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}