package dev.georgiys.personawidget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import dev.georgiys.personawidget.ui.theme.PersonaWidgetTheme

class MainActivity : ComponentActivity() {
    private var currentLocation: Location? = null
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var locationManager: LocationManager
    private lateinit var latitude: MutableState<String>
    private lateinit var longitude: MutableState<String>

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonaWidgetTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val preference = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    latitude = remember{mutableStateOf(preference.getString("latitude", "-200.0") ?: "")}
                    longitude = remember{mutableStateOf(preference.getString("longitude", "-200.0")?: "")}
                    val configuration = LocalConfiguration.current
                    val screenWidth = (configuration.screenWidthDp * 0.45).dp
                    Column(
                        modifier = Modifier.fillMaxSize().padding(5.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ){
                            TextField(
                                modifier = Modifier.defaultMinSize(screenWidth),
                                value = latitude.value,
                                onValueChange = {newText:String -> latitude.value = newText},
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            TextField(
                                modifier = Modifier.defaultMinSize(screenWidth),
                                value = longitude.value,
                                onValueChange = {newText:String -> longitude.value = newText},
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }

                        TextButton(
                            modifier = Modifier.fillMaxWidth(5f),
                            onClick = {
                                getGeoPoints()
                            }) {
                            Text(text = "Get your geodata")
                        }

                        TextButton(
                            modifier = Modifier.fillMaxWidth(5f),
                            onClick = { saveGeoPoint(latitude.value, longitude.value, this@MainActivity) }) {
                            Text(text = "Keep your geodata")
                        }
                    }
                }
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
            false
        } else {
            true
        }
    }
    private fun saveGeoPoint(latitude: String, longitude: String, context: Context){
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putString("latitude", latitude).apply()
        preference.edit().putString("longitude", longitude).apply()
    }

    private fun getGeoPoints(){
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) { return }
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (hasGps) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    gpsLocationListener
                )
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    networkLocationListener
                )
            }
            val lastKnownLocationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocationByGps?.let {
                locationByGps = lastKnownLocationByGps
            }
            //------------------------------------------------------//
            val lastKnownLocationByNetwork =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastKnownLocationByNetwork?.let {
                locationByNetwork = lastKnownLocationByNetwork
            }
//------------------------------------------------------//
            if (locationByGps != null && locationByNetwork != null) {
                if (locationByGps!!.accuracy > locationByNetwork!!.accuracy) {
                    currentLocation = locationByGps
                    latitude.value = currentLocation!!.latitude.toString()
                    longitude.value = currentLocation!!.longitude.toString()
                    // use latitude and longitude as per your need
                } else {
                    currentLocation = locationByNetwork
                    latitude.value = currentLocation!!.latitude.toString()
                    longitude.value = currentLocation!!.longitude.toString()
                    // use latitude and longitude as per your need
                }
            }
        }
    }




    private val gpsLocationListener: LocationListener =
        LocationListener { location -> locationByGps = location }
    private val networkLocationListener: LocationListener =
        LocationListener { location -> locationByNetwork= location }


}