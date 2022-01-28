package com.example.memorygame

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.utlis.MemoryGame
import com.google.android.material.snackbar.Snackbar


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

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh -> {
                if (memoryGame.numMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog(
                        "Do you really want to quit? You will loose your progress",
                        null
                    ) {
                        setupBoard()
                    }
                } else
                    setupBoard()
                return true
            }

            R.id.mi_choose_size -> {
                showNewSizeDialog()
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.rbGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            //set new value for board size
            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK"){
                _,_->positiveClickListener.onClick(null)
            }.show()

    }

    private fun setupBoard(){

        when(boardSize){
            BoardSize.EASY -> {
                txtMovesNum.text = "Easy: 4 x 2"
                txtPairsNum.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM ->  {
                txtMovesNum.text = "Medium: 6 x 3"
                txtPairsNum.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD ->  {
                txtMovesNum.text = "Hard: 6 x 4"
                txtPairsNum.text = "Pairs: 0 / 12"
            }
        }

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
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }


    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (memoryGame.haveWonGame()) {
            //alert the user of an invalid move
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFacedUp(position)) {
            //alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
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