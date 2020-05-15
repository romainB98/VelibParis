package com.example.velibparis

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    val helper = MySQLiteHelper(this)

    fun getBaseOnMap(){
        runOnUiThread {

            val result = helper.getAllStations()
            if (result != null) {
                result.moveToFirst()
                while (result.moveToNext()) {
                    //val stationId = result.getString(result.getColumnIndex("id"))
                    val lieu = result.getString(result.getColumnIndex("name"))
                    //val nbVelib = result.getString(result.getColumnIndex("nbr_bike")).toInt()
                    val gps = result.getString(result.getColumnIndex("coordinates"))

                    // convert string to HashMap
                    val long = gps.split("{lon=")[1].split(",")[0]
                    val lat = gps.split("lat=")[1].split("}")[0]

                    val gpsHashMap = HashMap<String, Double>()
                    gpsHashMap["lat"] = lat.toDouble()
                    gpsHashMap["lon"] = long.toDouble()

                    val myPlace = LatLng(lat.toDouble(), long.toDouble())  // this is New York
                    print(myPlace)
                    map.addMarker(MarkerOptions().position(myPlace).title(lieu))


                }


                result.close()
            }
        }


    }

   companion object{
       private const val LOCATION_PERMISSION_REQUEST_CODE = 1
   }
    private lateinit var lastLocation: Location
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

          return
        }
        // 1
        map.isMyLocationEnabled = true

// 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }

        }

    }
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)


        getBaseOnMap()
        setUpMap()

    }

    override fun onMarkerClick(p0: Marker?): Boolean = false


}
