package com.example.myapplication.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.common.contract
import com.example.myapplication.databinding.FragmentCreateSurveyBinding
import com.example.myapplication.models.data.Question
import com.example.myapplication.models.data.Survey

class CreateSurveyFragment: Fragment() {

    private var _binding: FragmentCreateSurveyBinding? = null
    private val binding get() = _binding!!

    private var questionNumber = 0
    private val questionsFragmentsList = arrayListOf<QuestionItemFragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateSurveyBinding.inflate(inflater, container, false)

        binding.createSurveyAddQuestion.setOnClickListener {
            addQuestion()
        }

        contract()?.getSurveyButton()?.setOnClickListener{
            saveSurvey()
        }

        return binding.root
    }

    private fun saveSurvey(){
        val database = contract()?.getDataBaseInstance()

        val id = database?.key
        val surveyName = binding.createSurveyName.text.toString()
        val questionsList = arrayListOf<Question>()
        for (i in questionsFragmentsList){
            questionsList.add(i.saveQuestion())
        }
        val userName = contract()?.getUserName()
        if (userName != null){
            val survey = Survey(
                id ?: "0000",
                surveyName,
                questionsList,
                userName)

            database?.push()?.setValue(survey)
        }
    }

    private fun addQuestion(){
        val questionFragment = contract()?.addQuestion(questionNumber)
        if(questionFragment != null ) {
            questionsFragmentsList.add(questionFragment)
            questionNumber++
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}