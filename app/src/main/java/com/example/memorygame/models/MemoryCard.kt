package com.example.memorygame.models

data class MemoryCard(
    val identifier : Int,
    var isFacedUp: Boolean = false,
    var isMatched: Boolean = false
)