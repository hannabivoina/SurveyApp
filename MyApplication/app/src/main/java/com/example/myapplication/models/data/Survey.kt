package com.example.myapplication.models.data

data class Survey(
    var id: String? = null,
    var name: String? = null,
    var questions: List<Question>? = null,
    var userName: String? = null
)
