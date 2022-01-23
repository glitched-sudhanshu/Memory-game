package com.example.memorygame

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.utlis.DEFAULT_ICONS
import com.example.memorygame.utlis.MemoryGame
import com.google.android.material.snackbar.Snackbar
import javax.security.auth.login.LoginException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var rvBoard: RecyclerView
    private lateinit var clRoot: ConstraintLayout
    private lateinit var txtMovesNum: TextView
    private lateinit var txtPairsNum: TextView
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame

    private var boardSize: BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        txtMovesNum = findViewById(R.id.txtMovesNum)
        txtPairsNum = findViewById(R.id.txtPairsNum)
        clRoot = findViewById(R.id.clRoot)

        txtPairsNum.setTextColor(ContextCompat.getColor(this, R.color.no_progress))

        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    Log.i(TAG, "onCardClicked: Card clicked $position")
                    updateGameWithFlip(position)
                }
            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth());
    }

    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (memoryGame.haveWonGame()) {
            //alert the user of an invalid move
            Snackbar.make(clRoot, "You already won!!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFacedUp(position)) {
            //alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid won!!", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getPairs(),
            ContextCompat.getColor(this, R.color.no_progress),
            ContextCompat.getColor(this, R.color.full_progress)
            ) as Int
            txtPairsNum.setTextColor(color)
            Log.i(TAG, "updateGameWithFlip: No. of pairs found " + memoryGame.numPairsFound)
            txtPairsNum.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getPairs()}"
            if (memoryGame.haveWonGame()){
                Snackbar.make(clRoot, "Congratulations, You won!!", Snackbar.LENGTH_LONG).show()
            }
        }
        txtMovesNum.text = "Moves: ${memoryGame.numMoves()}"
        adapter.notifyDataSetChanged()
    }


}