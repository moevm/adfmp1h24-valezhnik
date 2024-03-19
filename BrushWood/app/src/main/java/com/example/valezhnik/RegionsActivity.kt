package com.example.valezhnik

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader


class RegionsActivity : AppCompatActivity() {

    data class Region(val id: Int, val name: String, val path: String)


    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_kits)

        val regionList: List<Region> = jsonStringToList(readJsonFile( applicationContext.filesDir.path + "/regions.json"))
        val regionListCopy = regionList.toMutableList()
        val searchView = findViewById<SearchView>(R.id.search)
        val searchContainer = findViewById<CardView>(R.id.searchContainer)

        searchContainer.setOnClickListener{
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val filterList : List<Region> = regionList.filter { kit ->
                    kit.name.contains(query.toString(), ignoreCase = true)
                }

                findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                setView(filterList)

                Snackbar.make(
                    findViewById(R.id.rowContainer),
                    "Результат отфильтрован",
                    Snackbar.LENGTH_SHORT
                ).setAction("Отменять") {
                    findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                    setView(regionListCopy)

                }.show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filterLits : List<Region> = regionList.filter { kit ->
                    kit.name.contains(newText.toString(), ignoreCase = true)
                }

                findViewById<LinearLayout>(R.id.rowContainer2).removeAllViews()
                setView(filterLits)

                return false
            }
        })


        val layout = findViewById<LinearLayout>(R.id.rowContainer2)
        val btnBack = findViewById<ImageButton>(R.id.buttonBack)

        for (line in regionList){
            val row = layoutInflater.inflate(R.layout.row_content_regions, null)
            val textView = row.findViewById<TextView>(R.id.date)

            textView.text = line.name

            row.setOnClickListener{
                val intent = Intent(this, ViewRegionActivity::class.java)
                intent.putExtra("name", line.name)
                intent.putExtra("path", line.path)
                intent.putExtra("checkAPI", "false")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
                this.finish()
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("checkAPI", "false")
            startActivity(intent)
            this.finish()
        }
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


    private fun jsonStringToList(jsonString: String): MutableList<Region> {
        val kitList = mutableListOf<Region>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val id = jsonObject.getString("id").toInt()
                val name = jsonObject.getString("name")
                val path = jsonObject.getString("path")

                val kit = Region(id, name, path)
                kitList.add(kit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return kitList
    }



    private fun setView(list: List<Region>){
        val layout = findViewById<LinearLayout>(R.id.rowContainer2)

        for (line in list){
            val row = layoutInflater.inflate(R.layout.row_content_regions, null)
            val textView = row.findViewById<TextView>(R.id.date)

            textView.text = line.name

            row.setOnClickListener{
                val intent = Intent(this, ViewRegionActivity::class.java)
                intent.putExtra("name", line.name)
                intent.putExtra("path", line.path)
                intent.putExtra("checkAPI", "false")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
                this.finish()
            }

            layout.addView(row)

        }
    }
}