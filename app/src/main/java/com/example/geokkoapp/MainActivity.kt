package com.example.geokkoapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.app.AlertDialog
import android.widget.EditText
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.util.Log

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var myWebView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var isTrackingLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.myWebView)

        myWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                val input = EditText(this@MainActivity)
                input.setText(defaultValue ?: "")

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Please enter the KEYWORD")
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        val text = input.text.toString()
                        result?.confirm(text)
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        result?.cancel()
                    }
                    .setOnCancelListener {
                        result?.cancel()
                    }
                    .show()

                return true
            }
        }

        myWebView.addJavascriptInterface(WebAppInterface(this), "NativeBridge")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        myWebView.loadUrl("https://geokko-proto--pr55-feat-native-bridge-t0le3jcr.web.app?gamedebug")

        checkLocationPermissions()
        startLocationUpdates()
        startNativeSensor()
    }

    fun getNativeLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    myWebView.post {
                        myWebView.evaluateJavascript("javascript:if(window.onReceiveLocation) { window.onReceiveLocation($lat, $lng); }", null)
                    }
                }
            }
        }
    }
    fun startNativeSensor() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotationMatrix = FloatArray(9)

        SensorManager.getRotationMatrixFromVector(
            rotationMatrix,
            event.values
        )

        val orientation = FloatArray(3)

        SensorManager.getOrientation(
            rotationMatrix,
            orientation
        )

        val azimuth = (
                Math.toDegrees(orientation[0].toDouble()) + 360.0
                ) % 360.0

        myWebView.post {
            myWebView.evaluateJavascript("javascript:if(window.onReceiveSensorData) { window.onReceiveSensorData($azimuth); }", null)
        }
}

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(
            "SensorDebug",
            "sensor=${sensor?.name}, accuracy=$accuracy"
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
            setMinUpdateDistanceMeters(5f)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lng = location.longitude

                    myWebView.post {
                        myWebView.evaluateJavascript("javascript:if(window.onReceiveLocation) { window.onReceiveLocation($lat, $lng); }", null)
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            isTrackingLocation = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isTrackingLocation) {
            startLocationUpdates()
        }
        startNativeSensor()
    }
}
