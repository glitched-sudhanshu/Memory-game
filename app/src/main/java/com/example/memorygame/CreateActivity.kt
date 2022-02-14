package com.example.memorygame

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.utlis.BitmapScaler
import com.example.memorygame.utlis.EXTRA_BOARD_SIZE
import com.example.memorygame.utlis.isPermissionGranted
import com.example.memorygame.utlis.requestPermission
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {


    companion object{
        private const val PICK_PHOTOS = 200
        private const val TAG = "CreateActivity"
        private const val READ_EXTERNAL_PHOTOS_CODE = 311
        private const val MIN_GAME_LENGTH = 3
        private const val MAX_GAME_LENGTH = 3
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize : BoardSize
    private lateinit var rvImagePicker : RecyclerView
    private lateinit var etGameName : EditText
    private lateinit var btnSave : Button
    private var chosenImageUris = mutableListOf<Uri>()
    private val storage = Firebase.storage
    private var numImagesRequired = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        //to add back button to action bar
        //and override method onOptionsItemSelected
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()

        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize

        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize,
            object : ImagePickerAdapter.ImageClickListener {
                override fun onPlaceHolderClicked() {
                    if(isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION )){
                        launchIntentForPhotos()
                    }
                    else{
                        requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE)
                    }

                }

            })

        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())


        numImagesRequired = boardSize.getPairs()
        supportActionBar?.title = "Choose pics (0/$numImagesRequired)"

        btnSave.setOnClickListener {
            saveDataToFirebase()
        }

        //edit text have filters and we are setting it to a array of filters. We are using only one inbuilt input filter that is length filter.
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))
        //every time we change the game name the shouldEnableSaveButton() function will be called to check whether to enable btnSave or not.
        etGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }
        })
    }

    //to initialize views
    private fun initViews() {
        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)
    }

    //override this function to give functionalities is app bar menus
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //home is the id of the back button added by the android system itself
        if(item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //override this method regardless the user choose yes/no to callback the necessary action to perform
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }
            else{
                Toast.makeText(this, "In order to create a custom game, you need to provide access to your photos", Toast.LENGTH_LONG)
                    .show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun launchIntentForPhotos() {
        //here we are creating an implicit intent because we dont care that from where the user will choose the photo (google photos, gallery etc) we just want photos.
        val intent = Intent(Intent.ACTION_PICK)
        //we only care about images not pdfs etc
        intent.type = "image/*"
        //we can choose multiple photos at once
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTOS)
    }


    //override this method to get the changes, that is to receive the photos we choose in the gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode != PICK_PHOTOS || resultCode != Activity.RESULT_OK || data == null){
            Log.w(TAG, "Did not get data back from the launched activity, user likely canceled the flow")
            return
        }
        //if app supports only 1 selection at a time it will come as data
        val selectedUri = data.data
        //if app supports multiple selections at a time it will come as clip data
        val clipData = data.clipData
        if (clipData != null){
            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
            for (i in 0 until clipData.itemCount){
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }
            }
        }else if (selectedUri != null){
            Log.i(TAG, "data: $selectedUri")
            chosenImageUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${chosenImageUris.size}/$numImagesRequired)"
        btnSave.isEnabled = shouldEnableSaveButton()

    }

    //function to return whether save button should be enabled or not
    private fun shouldEnableSaveButton(): Boolean {
        if(chosenImageUris.size != numImagesRequired)
            return false
        if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_LENGTH)
            return false
        return true
    }

    //if data can be enabled save it to firebase
    private fun saveDataToFirebase() {
        Log.i(TAG, "saveDataToFirebase")
        for ((index, photoUri) in chosenImageUris.withIndex()){
            //image byte array is what we will be actually uploading to the firebase
            val imageByteArray = getImageByteArray(photoUri)
        }
    }

    //reducing size of selected photos
    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        }
        else{
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "Original width: ${originalBitmap.width} and height: ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width: ${originalBitmap.width} and height: ${originalBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

}