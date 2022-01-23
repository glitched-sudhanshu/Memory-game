package com.example.memorygame.utlis

import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard

class MemoryGame(private val boardSize: BoardSize) {


    val cards: List<MemoryCard>
    var numPairsFound = 0
    private var indexOfSingleSelectedCard: Int? = null
    private var numCardFlips = 0

    init {
        val imagesChosen = DEFAULT_ICONS.shuffled().take(boardSize.getPairs())
        val randomizedImages = (imagesChosen + imagesChosen).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        var foundMatch: Boolean = false
        val card = cards[position]
        //Three cases:
        //0 cards previously flipped over ==> restore cards + flip the selected card
        //1 card previously flipped over ==> flip over the selected card + check if cards matched
        //2 card previously flipped over ==> restore cards + flip the selected card
        if(indexOfSingleSelectedCard == null){
            //0 or 2 cards previously flipped
            restoreCards()
            indexOfSingleSelectedCard = position
        }
        else{
            //exactly 1 card flipped over previously
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFacedUp = !card.isFacedUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier!=cards[position2].identifier)
            return false
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards() {
        for (card in cards){
            if(!card.isMatched)
                card.isFacedUp = false
        }
    }

    fun haveWonGame(): Boolean {
        return (numPairsFound == boardSize.getPairs())

    }

    fun isCardFacedUp(position: Int): Boolean {
        return (cards[position].isFacedUp)

    }

    fun numMoves(): Int {
        return numCardFlips/2
    }
}