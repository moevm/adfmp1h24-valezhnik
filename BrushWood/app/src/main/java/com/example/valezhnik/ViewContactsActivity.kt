package com.example.valezhnik

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
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

data class Contact(
    val organization: String,
    val name: String,
    val role: String,
    val contact: String)

class ViewContactsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_contacts)

        val searchView = findViewById<SearchView>(R.id.search)
        val searchContainer = findViewById<CardView>(R.id.searchContainer)

        val contactList: MutableList<Contact> = jsonStringToList(readJsonFile( applicationContext.filesDir.path + "/contacts.json"))
        val contactListCopy = contactList.toMutableList()

        for(element in contactList){
            Log.d("show", element.organization)
            Log.d("show", element.name)
            Log.d("show", element.contact)
        }

        searchContainer.setOnClickListener{
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val filterList : List<Contact> = contactList.filter { kit ->
                    kit.organization.contains(query.toString(), ignoreCase = true) ||
                            kit.name.contains(query.toString(), ignoreCase = true) ||
                            kit.role.contains(query.toString(), ignoreCase = true)
                }

                searchView.clearFocus()
                editView(filterList)

                Snackbar.make(
                    findViewById(R.id.rowContainer),
                    "Список отфильтрован",
                    Snackbar.LENGTH_SHORT
                ).setAction("Отменять") {
                    editView(contactListCopy)

                }.show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filterList : List<Contact> = contactList.filter { kit ->
                    kit.organization.contains(newText.toString(), ignoreCase = true) ||
                            kit.name.contains(newText.toString(), ignoreCase = true) ||
                            kit.role.contains(newText.toString(), ignoreCase = true)
                }

                editView(filterList)
                return false
            }
        })

        val btnBack = findViewById<ImageButton>(R.id.buttonBack)

        setView(contactList)

        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
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



    private fun jsonStringToList(jsonString: String): MutableList<Contact> {
        val placeList = mutableListOf<Contact>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val organization = jsonObject.getString("organization").toString()
                val name = jsonObject.getString("name").toString()
                val role = jsonObject.getString("role").toString()
                val phone = jsonObject.getString("contact").toString()

                val place = Contact(organization, name, role, phone)
                placeList.add(place)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return placeList
    }

    private fun setView(list: List<Contact>){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = ContactAdapter(list)

        // recyclerView instance and add default Item Divider
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                layoutManager.orientation
            )
        )
    }

    private fun editView(list: List<Contact>){
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = ContactAdapter(list)
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

class ContactAdapter(private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_content_contacts, parent, false)

        return ContactViewHolder(view)
    }

    @SuppressLint("CutPasteId", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)

        val number = holder.itemView.findViewById<LinearLayout>(R.id.contactLayout)
        val contactText =  holder.itemView.findViewById<TextView>(R.id.contact)

        number.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Action to perform when the touch is initially pressed
                    Log.d("Typeface", contactText.typeface.toString())
                    contactText.setTextColor(Color.BLUE)
                    val phoneNumber = contact.contact
                    val dialIntent = Intent(Intent.ACTION_DIAL)
                    dialIntent.data = Uri.parse("tel:$phoneNumber")
                    holder.itemView.context.startActivity(dialIntent)
                    true // Return true to indicate that the touch event is consumed
                }
                MotionEvent.ACTION_UP -> {
                    // Action to perform when the touch is released
                    contactText.setTextColor(Color.BLACK)

                    // Perform additional action here if needed
                    true // Return true to indicate that the touch event is consumed
                }
                else -> {
                    contactText.setTextColor(Color.BLACK)
                    false
                } // Return false for other touch events
            }
        }


        // Updating the background color according
        // to the odd/even positions in list.
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.Cyan
                )
            )
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val organizationTextView: TextView = itemView.findViewById(R.id.organization)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val roleTextView: TextView = itemView.findViewById(R.id.role)
        private val phoneTextView: TextView = itemView.findViewById(R.id.contact)

        fun bind(contact: Contact) {
            organizationTextView.text = contact.organization
            nameTextView.text = contact.name
            roleTextView.text = contact.role
            phoneTextView.text = contact.contact

            // Bind other properties if needed
        }
    }

}