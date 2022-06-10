package com.example.myapplication.models.data

data class Question(
    var id: Int? = null,
    var options: List<Option>? = null,
    var questionText: String? = null,
    var type: String? = null,
    var answers: List<String>? = null
)
