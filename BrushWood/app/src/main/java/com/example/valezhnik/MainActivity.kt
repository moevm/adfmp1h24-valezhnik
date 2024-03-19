package com.example.valezhnik

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.yandex.mapkit.MapKitFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setAPI()
        setEnvironment()
        setContentView(R.layout.activity_main)

        Log.d("exist", fileExist("collections.json").toString())
        Log.d("exist", fileExist("contacts.json").toString())


        val openButton = findViewById<ImageButton>(R.id.buttonDrawerToggle)
        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        // Assuming you have a reference to your NavigationView
        val navigationView: NavigationView = findViewById(R.id.navigationView)

// Set an OnNavigationItemSelectedListener to handle item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navMap -> {

                    // Handle click on navMap item
                    // For example, you can start a new activity when navMap is clicked
                    val intent = Intent(this, MapsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true // Return true to indicate the click has been handled
                }
                R.id.navCart -> {
                    val intent = Intent(this, ViewKitsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true // Return true to indicate the click has been handled
                }
                R.id.navFavourite -> {
                    // Handle click on navFavourite item
                    val intent = Intent(this, MemoryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true // Return true to indicate the click has been handled
                }

                R.id.navRegions -> {
                    val intent = Intent(this, RegionsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true
                }

                R.id.navContacts -> {
                    val intent = Intent(this, ViewContactsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true
                }

                R.id.navFeedback -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent)
                    true
                }
                // Handle other menu items similarly
                else -> false
            }
        }

        openButton.setOnClickListener{
            drawerLayout.open()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fileExist(name: String): Boolean {
        return File(applicationContext.filesDir.path, name).exists()
    }

    private fun setAPI(){
        if(!this.intent.getStringExtra("checkAPI").equals("false")){
            MapKitFactory.setApiKey("bfec5c2f-3288-47f6-84b6-5a45d1b50642")
        }
    }
    private fun setEnvironment(){
        val files : List<String> = listOf("contacts.json", "regions.json", "region1.json", "region2.json")

        for (file in files){
            if(!fileExist(file)){
                copyJsonFileToInternalStorage(this, file)
            }
        }

        if (!fileExist("collections.json")){
            val file =  this.openFileOutput("collections.json", Context.MODE_PRIVATE)
            file.close();
        }
    }

    private fun copyJsonFileToInternalStorage(context: Context, fileName: String) {
        val inputStream = context.assets.open(fileName)
        val outputStream: OutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
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