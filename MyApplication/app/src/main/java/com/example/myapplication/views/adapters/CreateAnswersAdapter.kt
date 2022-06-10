package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemAnswerBinding
import com.example.myapplication.views.ANSWER_SINGLE
import java.util.ArrayList

class AnswersDiffCallback(
    private val oldList: List<Boolean>,
    private val newList: List<Boolean>
): DiffUtil.Callback(){
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSearch = oldList[oldItemPosition]
        val newSearch = newList[newItemPosition]

        return oldSearch == newSearch
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSearch = oldList[oldItemPosition]
        val newSearch = newList[newItemPosition]

        return oldSearch == newSearch
    }
}

class CreateAnswersAdapter(): RecyclerView.Adapter<CreateAnswersAdapter.ViewHolder>() {

    val answersList = ArrayList<Boolean>()
    var questionType: String = ""
    var answerTags = ArrayList<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_answer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = answersList[position]
        holder.bind(answer)
    }

    override fun getItemCount() = answersList.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        private val binding = ItemAnswerBinding.bind(view)
        private val answerId = answersList.size -1

        fun bind(answer: Boolean){

            binding.radioAnswerButton.isChecked = answer
            binding.editTextAnswer.tag = answerTags.last()

            binding.deleteAnswerButton.setOnClickListener {
                answersList.removeAt(answerId)
                deleteAnswer()
            }

            if (questionType == ANSWER_SINGLE){
                binding.radioAnswerButton.visibility = View.VISIBLE
                binding.checkBoxAnswerButton.visibility = View.GONE
            } else {
                binding.radioAnswerButton.visibility = View.GONE
                binding.checkBoxAnswerButton.visibility = View.VISIBLE
            }
            binding.radioAnswerButton.setOnClickListener {
                updateCorrectButton(answerId)
            }

        }
    }

    fun updateQuestionType(type: String){
        questionType = type
        notifyDataSetChanged()
    }

    fun addAnswer(answersTag: String){
        val newList = arrayListOf<Boolean>()
        newList.addAll(answersList)
        newList.add(false)

        val diffCallback = AnswersDiffCallback(answersList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        answersList.clear()
        answersList.addAll(newList)

        answerTags.add(answersTag)

        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteAnswer(){
        notifyDataSetChanged()
    }

    fun updateCorrectButton(id: Int){
        for (i in answersList.indices){
            answersList[i] = i == id
        }
        notifyDataSetChanged()
    }
}