package com.example.valezhnik

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.map.CameraListener


class MapsActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var myPoint: CameraPosition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_maps)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        requestLocationPermissions()
        startLocationUpdates()

        val openButton = findViewById<ImageButton>(R.id.buttonDrawerToggleMap)
        val newKitButton = findViewById<ImageButton>(R.id.buttonNewKit)

        val drawerLayout = findViewById<DrawerLayout>(R.id.map2)
        // Assuming you have a reference to your NavigationView

        openButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("checkAPI", "false")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
            this.finish()
        }

        newKitButton.setOnClickListener{
            startActivity(Intent(this, NewKit::class.java))
            onStop()
            this.finish()
        }

        mapView = findViewById(R.id.mapView)


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

        locationManager.subscribeForLocationUpdates(50.0, 1000L, 10.0, true, FilteringMode.OFF, object : LocationListener {
            override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                // Handle location updates
                val latitude = location.position.latitude
                val longitude = location.position.longitude

                mapView.map.move(
                    CameraPosition(Point(59.935493, 30.327392), 14.0f, 0.0f, 0.0f)
                )
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                // Handle location status updates if needed

            }
        })
    }

    private fun startLocationUpdates() {
        locationManager.subscribeForLocationUpdates(50.0, 1000L, 10.0, true, FilteringMode.OFF, object : LocationListener {
            override fun onLocationUpdated(location: com.yandex.mapkit.location.Location) {
                // Handle location updates
                val latitude = location.position.latitude
                val longitude = location.position.longitude

                mapView.map.move(
                    CameraPosition(Point(latitude, longitude), 14.0f, 0.0f, 0.0f)
                )
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                // Handle location status updates if needed

            }
        })
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

    private val cameraListener =
        CameraListener { _, cameraPosition, _, _ -> // Get the current camera position
            myPoint = cameraPosition
        }
}