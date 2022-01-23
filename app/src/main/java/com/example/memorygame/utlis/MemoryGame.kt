package com.example.memorygame.utlis

import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard

class MemoryGame(private val boardSize: BoardSize) {

    val cards: List<MemoryCard>
    val numPairsFound = 0

    init {
        val imagesChosen = DEFAULT_ICONS.shuffled().take(boardSize.getPairs())
        val randomizedImages = (imagesChosen + imagesChosen).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }
}