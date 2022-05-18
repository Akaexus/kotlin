package com.example.satfinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.InputStream
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textLocation: TextView
    private lateinit var textAzimuth:TextView
    private lateinit var textElevation:TextView
    private lateinit var textTilt:TextView
    private lateinit var satPicker: NumberPicker
    private val permissionRequest = 100

    private var location:Location? = null
    private var selectedSatellite:Satellite? = null
    private lateinit var satellites: List<Satellite>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textLocation = findViewById(R.id.textLocation)
        textAzimuth = findViewById(R.id.textAzimuth)
        textElevation = findViewById(R.id.textElevation)
        textTilt = findViewById(R.id.textTilt)
        satPicker = findViewById(R.id.satPicker)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        showSatellistes()
    }

    fun showSatellistes() {
        val ins: InputStream = resources.openRawResource(resources.getIdentifier(
            "sats",
            "raw", packageName
        ))

        val inputString = ins.bufferedReader().use {
            it.readText()
        }
        satellites = Json.decodeFromString<List<Satellite>>(inputString)
        satPicker.minValue = 0
        satPicker.maxValue = satellites.count() - 1
        var nameArray = Array(satellites.count()) { "" }
        for (i in 0 until satellites.count()) {
            nameArray[i] = satellites[i].name
        }
        satPicker.displayedValues = nameArray
        selectedSatellite = satellites[0]
        calculateParameters()
        satPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedSatellite = satellites[newVal]
            calculateParameters()
            true
        }
    }

    fun azimuth(location: Location, satellitePosition: Double) : Double {
        val longDiffR = toRadians(location.longitude - satellitePosition)
        val latR = toRadians(location.longitude - satellitePosition)
        var esazimuth = 180 + toDegrees(atan(tan(longDiffR)/sin(latR)))
        if (location.latitude < 0) {
            esazimuth -= 180
        }
        if (esazimuth < 0) {
            esazimuth += 360
        }
        return esazimuth
    }

    fun elevation(location: Location, satellitePosition: Double) : Double {
        val longDiffR = toRadians(location.longitude - satellitePosition)
        val latR = toRadians(location.latitude)
        val r1 = 1.0 + 35786.0 / 6378.16
        val v1 = r1 * cos(latR) * cos(longDiffR) - 1
        val v2 = r1 * sqrt(1 - cos(latR) * cos(latR) * cos(longDiffR) * cos(longDiffR))
        var eselevation = toDegrees(atan(v1 / v2))
        if (eselevation < 30) {
            eselevation = (eselevation + sqrt(eselevation * eselevation + 4.132) / 2)
        }
        return eselevation
    }

    fun polarisation(location: Location, satellitePosition: Double) : Double {
        val longDiffR = toRadians(location.longitude - satellitePosition)
        val latR = toRadians(location.latitude)
        val tilt = toDegrees(atan(sin(longDiffR) / tan(latR)))
        return tilt
    }

    private fun calculateParameters() {
        val loc = location
        if (loc != null && selectedSatellite != null) {
            textLocation.text = "Location: ${loc.toShortString()}"
            val satPosition = selectedSatellite!!.pos
            val az = String.format("%.2f", azimuth(loc!!, satPosition))
            textAzimuth.text = "Azimuth: $az°"
            val el = String.format("%.2f", elevation(loc!!, satPosition))
            textElevation.text = "Elevation: $el°"
            val ti = String.format("%.2f", polarisation(loc!!, satPosition))
            textTilt.text = "Tilt: $ti°"
        } else {
            textLocation.text = "Update coordinates"
        }
    }

    fun updateLocation(v: View) {
        getLastKnownLocation()
    }

    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                permissionRequest
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                location = lastLocation
                calculateParameters()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun Location.toShortString() : String {
        val sb = StringBuilder()
        sb.append(String.format("%.4f", longitude))
        sb.append(", ")
        sb.append(String.format("%.4f"), latitude)
        return sb.toString()
    }

    @Serializable
    data class Satellite(val name: String, val pos: Double)
}