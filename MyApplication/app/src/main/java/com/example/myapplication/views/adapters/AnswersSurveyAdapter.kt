package com.example.myapplication.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemQuestionBinding
import com.example.myapplication.models.data.Question
import com.example.myapplication.models.data.Survey
import java.util.ArrayList

class AnswersSurveyAdapter(): RecyclerView.Adapter<AnswersSurveyAdapter.ViewHolder>() {

    private var questions = emptyList<Question>()
    private val questionViews = ArrayList<ViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
//        questionViews.add(ViewHolder(view))
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(question)
    }

    override fun getItemCount() = questions.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = ItemQuestionBinding.bind(view)

        fun bind(question: Question) = when(question.type){
            "single" -> bindSingle(question)
            "open" -> bindOpen(question)
            else -> bindMultiple(question)
        }

        private fun bindSingle(question: Question) = with(binding){
            itemQuestionMultipleCheckLay.isGone = true
            itemQuestionOpenTextField.isGone = true
            itemQuestionSingleRadioGroup.isGone = false

            itemQuestionName.text = question.questionText
            itemQuestionSingleRadioGroup.removeAllViews()
            for(i in question.options!!){
                val radioOption = RadioButton(itemView.context)
                radioOption.text = i.value
                itemQuestionSingleRadioGroup.addView(radioOption)
            }
        }

        private fun bindMultiple(question: Question) = with(binding){
            itemQuestionMultipleCheckLay.isGone = false
            itemQuestionOpenTextField.isGone = true
            itemQuestionSingleRadioGroup.isGone = true

            itemQuestionName.text = question.questionText
            itemQuestionMultipleCheckLay.removeAllViews()
            for (i in question.options!!){
                val checkOption = CheckBox(itemView.context)
                checkOption.text = i.value
                itemQuestionMultipleCheckLay.addView(checkOption)
            }
        }

        private fun bindOpen(question: Question) = with(binding){
            itemQuestionMultipleCheckLay.isGone = true
            itemQuestionOpenTextField.isGone = false
            itemQuestionSingleRadioGroup.isGone = true

            itemQuestionName.text = question.questionText
        }

        fun saveAnswer(question: Question): List<String>{
            return when(question.type){
                "single" -> saveSingle(question)
                "open" -> saveOpen()
                else -> saveMultiple(question)
            }
        }

        private fun saveSingle(question: Question): List<String> {
            val answerId = binding.itemQuestionSingleRadioGroup.checkedRadioButtonId

            return listOf(question.options!!.get(answerId).value!!)
        }

        private fun saveMultiple(question: Question): List<String>{
            val answersList = ArrayList<String>()
            for(i in question.options!!.indices){
                val checkBox = binding.itemQuestionMultipleCheckLay[i] as CheckBox
                if(checkBox.isChecked){
                    answersList.add(question.options!![i].value!!)
                }
            }

            return answersList
        }

        private fun saveOpen(): List<String>{
            return listOf(binding.itemQuestionOpenTextField.text.toString())
        }
    }

    fun setSurvey(survey: Survey){
        questions = survey.questions!!
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateQuestion(question: Question){
        questions = listOf(question)
        notifyDataSetChanged()
    }

    fun saveSurveyAnswers(): List<List<String>>{
        val answersList = ArrayList<List<String>>()

        for(i in questions.indices){
            answersList.add(questionViews[i].saveAnswer(questions[i]))
        }

        return answersList.toList()
    }
}