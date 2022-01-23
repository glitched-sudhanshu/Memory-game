package com.example.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.utlis.DEFAULT_ICONS
import com.example.memorygame.utlis.MemoryGame
import javax.security.auth.login.LoginException

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var rvBoard: RecyclerView
    private lateinit var txtMovesNum: TextView
    private lateinit var txtPairsNum: TextView
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame

    private var boardSize: BoardSize = BoardSize.HARD


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        txtMovesNum = findViewById(R.id.txtMovesNum)
        txtPairsNum = findViewById(R.id.txtPairsNum)

        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object : MemoryBoardAdapter.CardClickListener{
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
            memoryGame
    }


}