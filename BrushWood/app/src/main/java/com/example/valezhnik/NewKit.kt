package com.example.valezhnik

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.valezhnik.ViewKitsActivity.Kit
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStreamReader


@Suppress("DEPRECATION")
class NewKit : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE  = 2
    @SuppressLint("WrongViewCast")

    private  var imageUri : ByteArray? = null
    private  var imageName: String? = null

    @SuppressLint("WrongViewCast")
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_kit)
        val btnOpenDialog = findViewById<ImageButton>(R.id.buttonAdd2)
        val tvSelectedDate = findViewById<ImageButton>(R.id.buttonAdd3)
        val btnInsertSize = findViewById<ImageView>(R.id.buttonAdd4)
        val btnLocation1 = findViewById<ImageButton>(R.id.buttonAdd5)
        val btnLocation2 = findViewById<ImageButton>(R.id.buttonAdd6)
        val btnBackArrow = findViewById<ImageButton>(R.id.buttonBackArrow)
        val textSizeView = findViewById<TextView>(R.id.textSize)
        val textCalender = findViewById<TextView>(R.id.textDate)
        val textImage = findViewById<TextView>(R.id.photoName)
        val textFrom = findViewById<TextView>(R.id.fromCoords)
        val textTo = findViewById<TextView>(R.id.toCoords)

        val photoText = findViewById<ImageView>(R.id.textViewPhoto)
        val nameText = findViewById<TextView>(R.id.textSize0)

        val btnSubmit = findViewById<MaterialButton>(R.id.buttonSubmit)
        val btnShare = findViewById<ImageButton>(R.id.buttonShare)
        val btnPhoto = findViewById<ImageButton>(R.id.buttonAdd22)
        val btnName = findViewById<ImageButton>(R.id.buttonAdd0)


        var date: String = intent.getStringExtra("date").toString()
        var bytes : String = intent.getStringExtra("byte").toString()
        var name :  String = intent.getStringExtra("name").toString()
        var photo :  String = intent.getStringExtra("photo").toString()
        var volume :  String = intent.getStringExtra("volume").toString()
        var from :  String = intent.getStringExtra("from_coords").toString()
        var to   :  String = intent.getStringExtra("to_coords").toString()
        var bool   : String = intent.getStringExtra("showData").toString()


        if(date != "null"){
            textCalender.text = date
        }

        if(volume != "null"){
            textSizeView.text = Html.fromHtml("$volume м<sup>3</sup>")
        }

        nameText.text = intent.getStringExtra("name")


        Log.d("Image name", photo)

        if (!from.equals("null")){
            textFrom.text = from
        }

        if(!to.equals("null")){
            textTo.text = to
        }

        Log.d("name", name)

        if(photo != "null"){
            imageName = photo
            textImage.text = imageName
        }

        if(name != "null"){
            nameText.text = name
        }

        if(bool.equals("true")){
            imageUri = base64ToByteArray(this.intent.getStringExtra("byte").toString())
            photoText.setImageBitmap(BitmapFactory.decodeByteArray(imageUri,
                0,
                imageUri!!.size))

            btnShare.setOnClickListener{
                val sharingIntent = Intent(Intent.ACTION_SEND)

                val shareBody = "Привет друг, хотел поделиться с тобой мою новую сборку валежника. Он был собран по гэолокализации ${from}"
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Сбор валежника")
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                sharingIntent.setType("image/")
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sharingIntent, "Сбор валежника"));
            }


        }else{

            findViewById<Toolbar>(R.id.toolBarMain).removeView(btnShare)

            if( bytes  != "null"){
                findViewById<LinearLayout>(R.id.bigContainer).removeView(btnShare)
                imageUri = base64ToByteArray(this.intent.getStringExtra("byte"))
                photoText.setImageBitmap(BitmapFactory.decodeByteArray(imageUri,
                    0,
                    imageUri!!.size))

            }
        }


        btnPhoto.setOnClickListener{
            val sharingIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(sharingIntent, REQUEST_IMAGE_CAPTURE )
        }

        btnName.setOnClickListener{
            showTextInputDialog(this) { text ->
                name = text
                nameText.text = text
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newKit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnOpenDialog.setOnClickListener { openPhotoDialog() }
        tvSelectedDate.setOnClickListener { showDatePickerDialog(textCalender)}


        if(!bool.equals("true")) {
            btnInsertSize.setOnClickListener {
                showNumberInputDialog(this) { number ->
                    volume = number.toString()
                    textSizeView.text = Html.fromHtml("$volume м<sup>3</sup>")

                }
            }
        }

        btnBackArrow.setOnClickListener{
            val intent : Intent
            if(!bool.equals("true")){
                intent = Intent(this, MapsActivity::class.java)
            }else{
                intent = Intent(this, ViewKitsActivity::class.java)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
            this.finish()
        }


        btnLocation1.setOnClickListener{
            val intent = Intent(this, SelectLocationActivity::class.java)
            intent.putExtra("date", textCalender.text)
            intent.putExtra("volume", volume)
            intent.putExtra("photo", imageName)

            var to  : String? = textTo.text.toString()

            if(to != null){
                intent.putExtra("to_coords", to.toString())
            }

            if (imageUri != null){
                intent.putExtra("byte", byteArrayToBase64(imageUri))
            }

            intent.putExtra("name", nameText.text.toString())
            intent.putExtra("source", "1")

//            Log.d("bytes in new set", this.intent.getStringExtra("byte").toString())

            if(bool.equals("true")){
                intent.putExtra("showOnly", "true")
                intent.putExtra("from_coords", this.intent.getStringExtra("from_coords"))
            }

            startActivity(intent);
            this.finish()
        }

        btnLocation2.setOnClickListener{
            val intent = Intent(this, SelectLocationActivity::class.java)
            intent.putExtra("date", textCalender.text)
            intent.putExtra("volume", volume)
            intent.putExtra("photo", imageName)

            var from   : String? = textFrom.text.toString()

            if(from != null){
                intent.putExtra("from_coords", from.toString())
            }

            if (imageUri != null){
                intent.putExtra("byte", byteArrayToBase64(imageUri))
            }

            intent.putExtra("name", nameText.text.toString())
            intent.putExtra("source", "2")

            if(bool.equals("true")){
                intent.putExtra("showOnly", "true")
                intent.putExtra("to_coords", this.intent.getStringExtra("to_coords"))
            }

            startActivity(intent);
            this.finish()
        }

        if(bool.equals("true")){
            val buttonSubmit = this.findViewById<MaterialButton>(R.id.buttonSubmit)
            val layout = findViewById<LinearLayout>(R.id.bigContainer)

            val t0 = findViewById<Toolbar>(R.id.toolBar0)
            val t1 = findViewById<Toolbar>(R.id.toolBar1)
            val t2 = findViewById<Toolbar>(R.id.toolBar2)
            val t3 = findViewById<Toolbar>(R.id.toolBar3)

            layout.removeView(buttonSubmit)
            t0.removeView(btnName)
            t1.removeView(btnOpenDialog)
            t1.removeView(btnPhoto)
            t2.removeView(tvSelectedDate)
            t3.removeView(btnInsertSize)
        }

        btnSubmit.setOnClickListener{
            val filePath = applicationContext.filesDir.path + "/collections.json"
            val fileContent = jsonStringToList(readJsonFile(filePath))

            if (checkForm(nameText.text.toString(),
                textCalender.text.toString(),
                volume,
                from,
                to)){
                addDataToList(Kit((fileContent.size + 1).toString(),
                    nameText.text.toString(),
                    imageName.toString(),
                    textCalender.text.toString(),
                    volume,
                    byteArrayToBase64(imageUri),
                    from,
                    to), fileContent)

                writeJsonDataToFile(this,"collections.json", listToJsonString(fileContent))
                val intent = Intent(this, ViewKitsActivity::class.java)
                intent.putExtra("checkAPI", "false")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
                this.finish()
            }else{

                Snackbar.make(
                    findViewById(R.id.bigContainer),
                    "Заполняйте все поля прежде чем сохранять сбор",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


    // Method to open the photo dialog
    private fun openPhotoDialog() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    // Method to handle the result of the photo dialog
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            var tmp =  data.data!!.path.toString().split('/').last().replace(':', '_')

            imageName = tmp

            val bs =  ByteArrayOutputStream()
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bs);
            imageUri = bs.toByteArray()

            val photoText = findViewById<ImageView>(R.id.textViewPhoto)
            val photoName = findViewById<TextView>(R.id.photoName)

            photoName.text = imageName

            photoText.setImageBitmap(BitmapFactory.decodeByteArray(bs.toByteArray(), 0, bs.toByteArray().size))
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {

            var tmp =  "image"
            imageName = tmp

            val bitmap = data?.extras?.get("data") as Bitmap
            val bs =  ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
            imageUri = bs.toByteArray()

            val photoText = findViewById<ImageView>(R.id.textViewPhoto)
            val photoName = findViewById<TextView>(R.id.photoName)

            photoName.text = imageName

            photoText.setImageBitmap(bitmap)

        }
    }

    private fun showDatePickerDialog(textCalender: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val paddingDay: String = if (dayOfMonth   < 10)   "0" else ""
                val paddingMonth : String = if (monthOfYear < 10) "0" else ""
                val selectedDate = "$paddingDay$dayOfMonth - $paddingMonth${monthOfYear + 1} - $year"

                textCalender.text = selectedDate
            }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    private fun showNumberInputDialog(context: Context, callback: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_number_input, null)
        val editTextNumber = dialogView.findViewById<EditText>(R.id.editTextNumber)

        AlertDialog.Builder(context)
            .setTitle("Введите объём (в м^3)")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val inputText = editTextNumber.text.toString()
                val number = if (inputText.isNotEmpty()) inputText.toInt() else 0
                callback(number)
                dialog.dismiss()
            }
            .setNegativeButton("Назад") { dialog, _ ->

                dialog.dismiss()

            }
            .create()
            .show()

            editTextNumber.requestFocus();
            WindowCompat.getInsetsController(window, editTextNumber).show(WindowInsetsCompat.Type.ime())
    }

    @SuppressLint("WrongViewCast")
    private fun showTextInputDialog(context: Context, callback: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_input, null)
        val editTextNumber = dialogView.findViewById<EditText>(R.id.editTextName)

        AlertDialog.Builder(context)
            .setTitle("Введите название: ")
            .setView(dialogView)

            .setPositiveButton("Сохранить") { dialog, _ ->
                val inputText = editTextNumber.text.toString()
                val number = inputText.ifEmpty { "" }
                callback(number)
                dialog.dismiss()
            }
            .setNegativeButton("Назад") { dialog, _ ->

                dialog.dismiss()

            }
            .create()
            .show()

        editTextNumber.requestFocus();
        WindowCompat.getInsetsController(window, editTextNumber).show(WindowInsetsCompat.Type.ime())

    }

    private fun readJsonFile(filePath: String): String {
        val fileInputStream = FileInputStream(filePath)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var text: String?

        // Read each line from the file and append it to the StringBuilder
        while (bufferedReader.readLine().also { text = it } != null) {
            stringBuilder.append(text).append('\n')
        }

        // Close the streams
        fileInputStream.close()
        inputStreamReader.close()
        bufferedReader.close()

        // Return the content of the file as a string
        return stringBuilder.toString()
    }

    // Function to add new data to the list
    private fun addDataToList(data: Kit, list: MutableList<Kit>) {
        list.add(data)
    }

    private fun writeJsonDataToFile(context: Context, fileName: String, jsonData: String) {
        try {
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(jsonData.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun jsonStringToList(jsonString: String): MutableList<Kit> {
        val kitList = mutableListOf<Kit>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val id = jsonObject.getString("id")
                val name = jsonObject.getString("name")
                val photo = jsonObject.getString("photo")
                val date = jsonObject.getString("date")
                val volume = jsonObject.getString("volume")
                val byteValue = jsonObject.getString("byte")
                val coordsFrom = jsonObject.getString("coordsFrom")
                val coordsTo = jsonObject.getString("coordsTo")

                val kit = Kit(id, name, photo, date, volume, byteValue, coordsFrom, coordsTo)
                kitList.add(kit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return kitList
    }

    private fun listToJsonString(kitList: List<Kit>): String {
        val jsonArray = JSONArray()

        for (kit in kitList) {
            val jsonObject = JSONObject()
            jsonObject.put("id", kit.id)
            jsonObject.put("date", kit.date)
            jsonObject.put("name", kit.name)
            jsonObject.put("photo", kit.photo)
            jsonObject.put("volume", kit.volume)
            jsonObject.put("byte", kit.byte)
            jsonObject.put("coordsFrom", kit.cFrom)
            jsonObject.put("coordsTo", kit.cTo)

            jsonArray.put(jsonObject)
        }

        return jsonArray.toString()
    }

    private fun checkForm(nameText: String, textCalender: String, volume: String, from: String, to: String): Boolean{

        Log.d("from", from)
        Log.d("to", to)


        return nameText != "" &&
            imageName.toString() != "null" &&
            textCalender != "" &&
            volume != "null" &&
            imageUri.toString() != "null" &&
            from != "null" &&
            to != "null"
    }

    // Encode byte array to Base64 string
    private fun byteArrayToBase64(byteArray: ByteArray?): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Decode Base64 string to byte array
    private fun base64ToByteArray(base64String: String?): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }
}