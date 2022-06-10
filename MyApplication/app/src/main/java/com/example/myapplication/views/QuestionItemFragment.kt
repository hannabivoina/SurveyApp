package com.example.myapplication.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemQuestionCreateBinding
import com.example.myapplication.models.data.Option
import com.example.myapplication.models.data.Question
import com.example.myapplication.models.data.QuestionsDataId
import com.example.myapplication.views.adapters.CreateAnswersAdapter

const val ANSWER_OPEN = "Open"
const val ANSWER_SINGLE = "Single"

class QuestionItemFragment(private val questionTag: String, val questionNumber: Int): Fragment() {
    private var _binding: ItemQuestionCreateBinding? = null
    private val binding get() = _binding!!

    private val answersListAdapter = CreateAnswersAdapter()

    private val questionDataId = QuestionsDataId()
    private var questionType = "Single"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemQuestionCreateBinding.inflate(inflater, container, false)

        setupAnswersAdapter()
        setQuesId()

        binding.itemQuestionCreateTypeRadioGroup.check(R.id.itemQuestionCreateTypeSingle)
        updateQuestionType(ANSWER_SINGLE)
        answersListAdapter.addAnswer(addAnswerIdToQuestion())

        binding.itemQuestionCreateAddAnswer.setOnClickListener {
            val answerTag = addAnswerIdToQuestion()
            answersListAdapter.addAnswer(answerTag)
        }

        binding.itemQuestionCreateTypeRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            val questionType = radio.text.toString()
            updateQuestionType(questionType)
        })

        return binding.root
    }

    private fun updateQuestionType(type: String){
        questionType = type

        questionDataId.addQuestionType(type)

        if (type == ANSWER_OPEN){
            binding.itemQuestionCreateOpenAnswer.visibility = View.VISIBLE
            binding.itemQuestionCreateAnswersRecyclerView.visibility = View.GONE
        } else {
            binding.itemQuestionCreateOpenAnswer.visibility = View.GONE
            binding.itemQuestionCreateAnswersRecyclerView.visibility = View.VISIBLE
        }
        answersListAdapter.updateQuestionType(type)
    }

    private fun setQuesId(){
        questionDataId.quesId = questionTag
        binding.itemQuestionCreateName.tag = questionTag
    }

    private fun addAnswerIdToQuestion(): String {
        val answersCount = binding.itemQuestionCreateAnswersRecyclerView.childCount
        val tag = questionTag + "A${answersCount}"
        questionDataId.addAnswerId(tag)
        return tag
    }

    open fun saveQuestion(): Question {
        val optionsList = arrayListOf<Option>()
        for (p in questionDataId.quesAnswerIds.indices) {
            val answerData = questionDataId.quesAnswerIds[p]
            val answer = view?.findViewWithTag<EditText>(answerData)?.text.toString()
            val option = Option(p, questionNumber, answer)
            optionsList.add(option)
        }

        return Question(
            questionNumber,
            optionsList,
            binding.itemQuestionCreateName.text.toString(),
            questionType,
            emptyList()
        )
    }

    private fun setupAnswersAdapter(){
        binding.itemQuestionCreateAnswersRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.itemQuestionCreateAnswersRecyclerView.adapter = answersListAdapter
    }
}