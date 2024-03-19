package com.example.valezhnik

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider


class SelectLocationActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myPoint: CameraPosition


    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_select_location)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val openButton = findViewById<ImageButton>(R.id.buttonDrawerToggleMap2)
        val newKitButton = findViewById<MaterialButton>(R.id.buttonNewKit2)
        val locator = findViewById<ImageView>(R.id.locationImage)

        var bool : String = intent.getStringExtra("showOnly").toString()

        Log.d("bool", bool)

        if(bool == "true"){
            val layoutImage = findViewById<LinearLayout>(R.id.layoutImage)
            val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)

            layoutImage.removeView(locator)
            relativeLayout.removeView(newKitButton)

            Log.d("coordinates", this.intent.getStringExtra("from_coords").toString())
            Log.d("coordinates", this.intent.getStringExtra("to_coords").toString())

            parseCoordinates(this.intent.getStringExtra("from_coords").toString())
            parseCoordinates(this.intent.getStringExtra("to_coords").toString())

        }


        mapView = findViewById(R.id.mapView2)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        if(bool != "true") {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Handle the retrieved location
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Do something with latitude and longitude

                        mapView.mapWindow.map.move(
                            CameraPosition(Point(latitude, longitude), 16.0f, 0.0f, 0.0f)
                        )

                        mapView.mapWindow.map.addCameraListener(cameraListener)
                    }
                }
        }else{
            if(this.intent.getStringExtra("source") == "1"){
                val p1 : Point = parseCoordinates(this.intent.getStringExtra("from_coords").toString())
                mapView.mapWindow.map.move(
                    CameraPosition(p1, 16.0f, 0.0f, 0.0f)
                )
                drawMyLocationMark(p1, mapView)
            }
            if(this.intent.getStringExtra("source") == "2"){
                val p2 : Point = parseCoordinates(this.intent.getStringExtra("to_coords").toString())
                mapView.mapWindow.map.move(
                    CameraPosition(p2, 16.0f, 0.0f, 0.0f)
                )
                drawMyLocationMark(p2, mapView)
            }

        }



        newKitButton.setOnClickListener {
            val intent = Intent(this, NewKit::class.java)
            intent.putExtra("date", this.intent.getStringExtra("date").toString())
            intent.putExtra("volume", this.intent.getStringExtra("volume").toString())
            intent.putExtra("byte", this.intent.getStringExtra("byte").toString())
            intent.putExtra("photo", this.intent.getStringExtra("photo").toString())
            intent.putExtra("name", this.intent.getStringExtra("name").toString())


            Log.d("uri", this.intent.getStringExtra("byte").toString())
            if (this.intent.getStringExtra("source").toString().equals("1")){
                intent.putExtra("from_coords", "${myPoint.target.latitude},\n${myPoint.target.longitude}")
                intent.putExtra("to_coords", this.intent.getStringExtra("to_coords"))

            }

            if (this.intent.getStringExtra("source").toString().equals("2")){
                Log.d("Equals", "2")
                intent.putExtra("to_coords", "${myPoint.target.latitude},\n${myPoint.target.longitude}")
                intent.putExtra("from_coords", this.intent.getStringExtra("from_coords"))
            }

            startActivity(intent)
            this.finish()
        }

        openButton.setOnClickListener{
            val intent = Intent(this, NewKit::class.java)

            intent.putExtra("date", this.intent.getStringExtra("date").toString())
            intent.putExtra("volume", this.intent.getStringExtra("volume").toString())
            intent.putExtra("byte", this.intent.getStringExtra("byte").toString())
            intent.putExtra("photo", this.intent.getStringExtra("photo"))
            intent.putExtra("name", this.intent.getStringExtra("name"))
            intent.putExtra("from_coords", this.intent.getStringExtra("from_coords"))
            intent.putExtra("to_coords", this.intent.getStringExtra("to_coords"))

            if(bool == "true"){
                intent.putExtra("showData", "true")
            }

            intent.putExtra("checkAPI", "false")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this.finish()
        }

        mapView = findViewById(R.id.mapView2)

    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        mapView.onStop()
    }


    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun parseCoordinates(str : String) : Point{
        val data = str.split(',')

        return Point(data[0].toDouble(), data[1].toDouble())
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun drawMyLocationMark(point: Point, mapview: MapView): PlacemarkMapObject {
        val view = View(this).apply {
            background = getDrawable(R.drawable.map_locate)
            layoutParams = ViewGroup.LayoutParams(width/10, height/10)
        }


        return mapview.map.mapObjects.addPlacemark(point, ViewProvider(view))
    }

    // Camera listener to listen for camera position changes
    private val cameraListener =
        CameraListener { map, cameraPosition, cameraUpdateReason, finished -> // Get the current camera position
            myPoint = cameraPosition
            val target = cameraPosition.target
            val latitude = target.latitude
            val longitude = target.longitude

        }

}
