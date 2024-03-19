package com.example.valezhnik

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class ViewKitsActivity : AppCompatActivity() {

    data class Kit(val id: String,
                   val name: String,
                   val photo: String,
                   val date: String,
                   val volume: String,
                   val byte: String,
                   val cFrom: String,
                   val cTo : String)

    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_kits)

        val kitList: List<Kit> = jsonStringToList(readJsonFile( applicationContext.filesDir.path + "/collections.json"))
        val searchView = findViewById<SearchView>(R.id.search)
        val searchContainer = findViewById<CardView>(R.id.searchContainer)

        searchContainer.setOnClickListener{
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val filterLits : List<Kit> = kitList.filter { kit ->
                    kit.id.contains(query.toString(), ignoreCase = true) ||
                            kit.name.contains(query.toString(), ignoreCase = true) ||
                            kit.date.contains(query.toString(), ignoreCase = true)
                }

                findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                setView(filterLits)

                Snackbar.make(
                    findViewById(R.id.rowContainer),
                    "Список отсортирован",
                    Snackbar.LENGTH_SHORT
                ).setAction("Отменять") {
                    findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                    setView(jsonStringToList(readJsonFile( applicationContext.filesDir.path + "/collections.json")))

                }.show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filterLits : List<Kit> = kitList.filter { kit ->
                    kit.id.contains(newText.toString(), ignoreCase = true) ||
                            kit.name.contains(newText.toString(), ignoreCase = true) ||
                            kit.date.contains(newText.toString(), ignoreCase = true)
                }

                findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                setView(filterLits)

                return false
            }
        })


        val layout = findViewById<LinearLayout>(R.id.rowContainer2)
        val btnBack = findViewById<ImageButton>(R.id.buttonBack)

        for (line in kitList){
            val row = layoutInflater.inflate(R.layout.row_content, null)
            val textView = row.findViewById<TextView>(R.id.infos)
            val iconDelete = row.findViewById<ImageButton>(R.id.iconButton)

             "${line.name}  (${line.date})".also { textView.text = it }

            Log.d("data", line.name)
            Log.d("data", line.photo)
            Log.d("data", line.date)
            Log.d("data", line.volume)

            row.setOnClickListener{
                val intent = Intent(this, NewKit::class.java)
                intent.putExtra("name", line.name)
                intent.putExtra("photo", line.photo)
                intent.putExtra("date", line.date)
                intent.putExtra("volume", line.volume)
                intent.putExtra("byte", line.byte)
                intent.putExtra("from_coords", line.cFrom)
                intent.putExtra("to_coords", line.cTo)
                intent.putExtra("showData", "true")

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
                this.finish()
            }

            val popupDeleteKit = Snackbar.make(
                layout,
                "Сбор удален",
                Snackbar.LENGTH_SHORT
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("Вы уверены что хотите удалить сбор ?")
                .setTitle("Удаление сбора")
                .setPositiveButton("Да") { dialog, which ->
                    val filePath = applicationContext.filesDir.path + "/collections.json"
                    val fileContent = jsonStringToList(readJsonFile(filePath))
                    deleteDataToList(line, fileContent)
                    writeJsonDataToFile(this,"collections.json", listToJsonString(fileContent))
                    finish()
                    startActivity(Intent(this, ViewKitsActivity::class.java))
                    //popupDeleteKit.show()
                }
                .setNegativeButton("Нет") { dialog, which ->
                    // Do something else.
                }

            iconDelete.setOnClickListener{
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            layout.addView(row)

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("checkAPI", "false")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("checkAPI", "false")
            startActivity(intent)
            this.finish()
        }
    }

    fun readJsonFile(filePath: String): String {
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

    private fun deleteDataToList(kit: Kit, list: MutableList<Kit>) {
        list.removeAt(list.indexOf(kit))
    }

    fun jsonStringToList(jsonString: String): MutableList<Kit> {
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

    private fun writeJsonDataToFile(context: Context, fileName: String, jsonData: String) {
        try {
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(jsonData.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listToJsonString(kitList: List<Kit>): String {
        val jsonArray = JSONArray()

        for (kit in kitList) {
            val jsonObject = JSONObject()
            jsonObject.put("id", kit.id)
            jsonObject.put("name", kit.name)
            jsonObject.put("photo", kit.photo)
            jsonObject.put("date", kit.date)
            jsonObject.put("volume", kit.volume)
            jsonObject.put("byte", kit.byte)
            jsonObject.put("coordsFrom", kit.cFrom)
            jsonObject.put("coordsTo", kit.cTo)

            jsonArray.put(jsonObject)
        }

        return jsonArray.toString()
    }

    private fun setView(list: List<Kit>){
        val layout = findViewById<LinearLayout>(R.id.rowContainer2)

        for (line in list){
            val row = layoutInflater.inflate(R.layout.row_content, null)
            val textView = row.findViewById<TextView>(R.id.infos)
            val iconDelete = row.findViewById<ImageButton>(R.id.iconButton)

            "${line.name}  (${line.date})".also { textView.text = it }

            row.setOnClickListener{
                val intent = Intent(this, NewKit::class.java)
                intent.putExtra("date", line.date)
                intent.putExtra("name", line.name)
                intent.putExtra("photo", line.photo)
                intent.putExtra("volume", line.volume)
                intent.putExtra("byte", line.byte)
                intent.putExtra("from_coords", line.cFrom)
                intent.putExtra("to_coords", line.cTo)
                intent.putExtra("showData", "true")
                intent.putExtra("checkAPI", "false")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
                this.finish()
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("Вы уверены что хотите удалить сбор ?")
                .setTitle("Удаление сбора")
                .setPositiveButton("Да") { dialog, which ->
                    val filePath = applicationContext.filesDir.path + "/collections.json"
                    val fileContent = jsonStringToList(readJsonFile(filePath))
                    deleteDataToList(line, fileContent)
                    writeJsonDataToFile(this,"collections.json", listToJsonString(fileContent))
                    finish()
                    startActivity(Intent(this, ViewKitsActivity::class.java))
                    //popupDeleteKit.show()
                }
                .setNegativeButton("Нет") { dialog, which ->
                    // Do something else.
                }

            iconDelete.setOnClickListener{
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            layout.addView(row)
        }
    }
}