package com.example.memorygame

import android.app.ActionBar
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import kotlin.math.min



class ImagePickerAdapter(private val context: Context,
                         private val imageUris: List<Uri>,
                         private val boardSize: BoardSize,
                         private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE = 10
    }

    //interface to invoke an intent to go to gallery to choose picture
    interface ImageClickListener{
        fun onPlaceHolderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width/boardSize.getWidth() - (2* MARGIN_SIZE)
        val cardHeight = parent.height/boardSize.getHeight() - (2* MARGIN_SIZE)
        val cardSizeLength = min(cardHeight, cardWidth)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = cardSizeLength
        layoutParams.width = cardSizeLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        Log.i(ContentValues.TAG, "Image height: $cardSizeLength")
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageUris.size){
            holder.bind(imageUris[position])
        }else{
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return boardSize.getPairs()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI((uri))
            ivCustomImage.setOnClickListener(null)
        }
        fun bind (){
            //invoking the listener when a button is clicked to choose photo
            ivCustomImage.setOnClickListener{
                imageClickListener.onPlaceHolderClicked()
            }

        }
    }

}
