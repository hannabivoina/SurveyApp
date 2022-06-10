package com.example.myapplication.models.data

class QuestionsDataId() {
    var quesId: String = ""
    var quesType: String = ""
    val quesAnswerIds: ArrayList<String> = arrayListOf()

    fun addQuestionId(questionId: String){
        quesId = questionId
    }

    fun addQuestionType(questionType: String){
        quesType = questionType
    }

    fun addAnswerId(answerId: String){
        quesAnswerIds.add(answerId)
    }
}