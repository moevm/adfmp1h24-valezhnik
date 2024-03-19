package com.example.valezhnik

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

data class Place(
    val district: String,
    val quarter: String,
    val selected: String,
    val area: String,
    val length: String,
    val way: String)

class ViewRegionActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_region)

        val searchView = findViewById<SearchView>(R.id.search)
        val searchContainer = findViewById<CardView>(R.id.searchContainer)

        val title = findViewById<TextView>(R.id.namePage)
        title.text = this.intent.getStringExtra("name")

        val region : String = this.intent.getStringExtra("path").toString()
        val regionList: MutableList<Place> = jsonStringToList(readJsonFile( applicationContext.filesDir.path + "/" + region))
        val regionListCopy = regionList.toMutableList()

        searchContainer.setOnClickListener{
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val filterList : List<Place> = regionList.filter { kit ->
                    kit.district.contains(query.toString(), ignoreCase = true) ||
                    kit.quarter.contains(query.toString(), ignoreCase = true) ||
                    kit.selected.contains(query.toString(), ignoreCase = true) ||
                    kit.area.contains(query.toString(), ignoreCase = true) ||
                    kit.length.contains(query.toString(), ignoreCase = true) ||
                    kit.way.contains(query.toString(), ignoreCase = true)
                }

                editView(filterList)

                Snackbar.make(
                    findViewById(R.id.rowContainer),
                    "Список отсортирован",
                    Snackbar.LENGTH_SHORT
                ).setAction("Отменять") {
                    editView(regionListCopy)
                }.show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filterList : List<Place> = regionList.filter { kit ->
                            kit.district.contains(newText.toString(), ignoreCase = true) ||
                            kit.quarter.contains(newText.toString(), ignoreCase = true) ||
                            kit.selected.contains(newText.toString(), ignoreCase = true) ||
                            kit.area.contains(newText.toString(), ignoreCase = true) ||
                            kit.length.contains(newText.toString(), ignoreCase = true) ||
                            kit.way.contains(newText.toString(), ignoreCase = true)
                }

                editView(filterList)
                return false
            }
        })

        val btnBack = findViewById<ImageButton>(R.id.buttonBack)

        setView(regionList)

        btnBack.setOnClickListener{
            val intent = Intent(this, RegionsActivity::class.java)
            intent.putExtra("checkAPI", "false")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
            this.finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    private fun jsonStringToList(jsonString: String): MutableList<Place> {
        val placeList = mutableListOf<Place>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val district = jsonObject.getString("Column2").toString()
                val quarter = jsonObject.getString("Column3").toString()
                val selected = jsonObject.getString("Column4").toString()
                val area = jsonObject.getString("Column5").toString()
                val length = jsonObject.getString("Column6").toString()
                val way = jsonObject.getString("Column7").toString()

                val place = Place(district, quarter, selected, area, length, way)
                placeList.add(place)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return placeList
    }

    private fun setView(list: List<Place>){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = PlaceAdapter(list.drop(1))

        // recyclerView instance and add default Item Divider
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                layoutManager.orientation
            )
        )
    }

    private fun editView(list: List<Place>){
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = PlaceAdapter(list.drop(1))
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
}

class PlaceAdapter(private val places: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_content_places, parent, false)
        Log.d("view", view.toString())

        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place)

        // Updating the background color according
        // to the odd/even positions in list.
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.Gray
                )
            )
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

    }

    override fun getItemCount(): Int {
        return places.size
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val districtTextView: TextView = itemView.findViewById(R.id.district)
        private val quarterTextView: TextView = itemView.findViewById(R.id.quarter)
        private val selectedTextView: TextView = itemView.findViewById(R.id.selected)
        private val areaTextView: TextView = itemView.findViewById(R.id.area)
        private val lengthTextView: TextView = itemView.findViewById(R.id.length)
        private val wayTextView: TextView = itemView.findViewById(R.id.way)

        fun bind(place: Place) {
            districtTextView.text = place.district
            quarterTextView.text = place.quarter
            selectedTextView.text = place.selected
            areaTextView.text = place.area
            lengthTextView.text = place.length
            wayTextView.text = place.way
            // Bind other properties if needed
        }
    }

}