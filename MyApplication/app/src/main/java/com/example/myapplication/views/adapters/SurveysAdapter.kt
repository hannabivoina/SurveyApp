package com.example.myapplication.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemSurveyBinding
import com.example.myapplication.models.data.Survey

class SurveysAdapter: RecyclerView.Adapter<SurveysAdapter.ViewHolder>() {

    var surveysList = listOf<Survey>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = surveysList[position]
        holder.bind(room)
    }

    override fun getItemCount() = surveysList.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemSurveyBinding.bind(view)
        fun bind(survey: Survey){
            binding.itemSurveyName.text = survey.name
            binding.itemSurveyUserName.text = survey.userName
            binding.itemSurvey.setOnClickListener {

            }
        }
    }

    fun updateRoomsList(list: List<Survey>){
        surveysList = list
        notifyDataSetChanged()
    }
}