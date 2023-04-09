package sg.edu.smu.cs461.scoutscoot

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
//import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityRideInfoBinding
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList



class RideInfoActivity : AppCompatActivity() , SensorEventListener {

    private lateinit var binding: ActivityRideInfoBinding
    private val database = Firebase.database(DATABASE_URL).reference
    private var auth = FirebaseAuth.getInstance()
    private lateinit var rentalID: String
    private lateinit var ride: Ride
    private lateinit var scooter: Scooter
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var dialogBuilder: Dialog
    private lateinit var preferences: SharedPreferences
    private var price = 0.0

    private lateinit var sensorMan: SensorManager
    private lateinit var accelerometer: Sensor
    private var color = false

    private var totalTime = 0
    private var latitudeFrom = 0.0
    private var longitudeFrom = 0.0

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest


    private var latestLatitude: Double = 0.0
    private var latestLongitude: Double = 0.0



    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideInfoBinding.inflate(layoutInflater)
        val view = binding.root
        preferences = getPreferences(Context.MODE_PRIVATE)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        latitudeFrom = intent.getDoubleExtra("latitude", 0.0)
        longitudeFrom = intent.getDoubleExtra("longitude", 0.0)

        // accelormeter sensor code
        sensorMan = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        binding.endrental.setOnClickListener {

            //check if user have location permission in order to finish and update the scooter location
            endrental()
        //            val broadcast =Intent(this, Vibration::class.java)
//            sendBroadcast(broadcast)
//            if (!checkPermission()) {
//                endrental()
//            }
//            else{
//                requestUserPermissions()
//            }

        }

        binding.floatingActionButton.setOnClickListener(){
            val intent = Intent(this, MapRide::class.java)
            startActivity(intent)
        }

        database.child("Users").orderByKey().equalTo(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            if (postSnapshot.getValue<User>()!!.inRental == "false") {
                                setResult(Activity.RESULT_OK)
                                finish() //return to back activity
                            } else {
                                rentalID = postSnapshot.getValue<User>()?.inRental!!
                                getRentalInfo(rentalID)

                            }

                        }
                    } else {
                        setResult(Activity.RESULT_OK)
                        finish() //return to back activity
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        setContentView(view)

        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 50
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    // Use the latest location here
                    latestLatitude = location.latitude
                    latestLongitude = location.longitude

                    println(latestLatitude)
                    println(latestLongitude)
                    println("location is currently tracking")
                }
            }
        }

    }

    private fun getRentalInfo(rentalID: String) {
        database.child("Users").child(auth.currentUser!!.uid).child("Rides").orderByKey()
            .equalTo(rentalID).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children) {
                        ride = postSnapshot.getValue<Ride>()!!
                    }
                    val elapsedRealtimeOffset =
                        System.currentTimeMillis() - SystemClock.elapsedRealtime()
                    binding.timer.base = (ride.start_Time!! - elapsedRealtimeOffset)
                    binding.timer.onChronometerTickListener = OnChronometerTickListener {
                        val elapsedMillis = SystemClock.elapsedRealtime() - binding.timer.base
                        val minutes = (elapsedMillis / 1000 / 30)
                        price = (minutes * 0.5).toDouble()
                        binding.price.text = price.toString().plus(" SGD")
                        totalTime = (elapsedMillis / 1000 / 60).toInt()
                    }



                    binding.timer.start()
                    binding.lock.setOnClickListener {
                        database.child("Users")
                            .child((auth.currentUser?.uid!!))
                            .child("Rides")
                            .child(rentalID)
                            .child("locked")
                            .setValue(!ride.locked!!)
                    }
                    if (ride.locked == true) {
                        binding.lock.text = "Unlock"
                    } else {
                        binding.lock.text = "Lock"
                    }
                    database.child("Scooter").orderByKey().equalTo(ride.scooter_ID.toString())
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children) {
                                    scooter = postSnapshot.getValue<Scooter>()!!
                                    binding.scootername.text = scooter.name
                                    binding.range.text = scooter.getRange().toString().plus(" km")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun endrental() {
        binding.endrental.isEnabled=false
        val contextView = findViewById<View>(R.id.rideinfo_layout)
        val snackbarWaiting= Snackbar.make(contextView, "Getting Location ...", Snackbar.LENGTH_INDEFINITE)
        snackbarWaiting.show()
        var position: String
        val tokenSource = CancellationTokenSource();
        val token = tokenSource.token

        fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY,token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude =  latestLatitude
                    val longitude = latestLongitude
                    var postalTo = ""
                    var postalFrom = ""


                    CoroutineScope(Dispatchers.IO).launch {

                        // Perform other Geocoder-related tasks
                        postalTo = ""
                        val geocoder = Geocoder(this@RideInfoActivity, Locale.getDefault())
                        val addressTo= geocoder.getFromLocation(latitude,longitude,1)
                        if (addressTo!!.isNotEmpty()){
                            postalTo = addressTo[0].postalCode
                        }

                        postalFrom = ""
                        val addressFrom = geocoder.getFromLocation(latitudeFrom,longitudeFrom,1)
                        if (addressFrom!!.isNotEmpty()){
                            postalFrom = addressFrom[0].postalCode
                        }
                        println(postalTo)
                        println(postalFrom)

                        // Move the following code back to the main thread
                        launch(Dispatchers.Main) {
                            position = latitude.toString().plus(", ").plus(longitude.toString())

                            database.child("Users")
                                .child((auth.currentUser?.uid!!))
                                .child("Rides")
                                .child(rentalID)
                                .child("end_time")
                                .setValue(System.currentTimeMillis())
                            database.child("Users")
                                .child((auth.currentUser?.uid!!))
                                .child("Rides")
                                .child(rentalID)
                                .child("final_latlong")
                                .setValue(position)
                            database.child("Users")
                                .child(auth.currentUser?.uid!!)
                                .child("Rides")
                                .child(rentalID)
                                .child("price")
                                .setValue(price)

                            database.child("Users")
                                .child((auth.currentUser?.uid!!))
                                .child("inRental")
                                .setValue("false")

                            database.child("Scooter")
                                .child(ride.scooter_ID.toString())
                                .child("rented")
                                .setValue(false)

                            database.child("Scooter")
                                .child(ride.scooter_ID.toString())
                                .child("where")
                                .setValue(position)

                            val intent = Intent(this@RideInfoActivity, PaymentActivity::class.java)

                            println("location to be pushed to intent")
                            println(postalTo)
                            println(postalFrom)


                            price *= 100
                            intent.putExtra("priceKey",price.toInt().toString())
                            intent.putExtra("timeKey",totalTime)
                            intent.putExtra("toKey",postalTo)
                            intent.putExtra("fromKey",postalFrom)
                            startActivity(intent)

                        }
                    }


                } else {
                    binding.endrental.isEnabled=true
                    snackbarWaiting.dismiss()
                    errorlocation()

                }

            }
            .addOnFailureListener() {
                binding.endrental.isEnabled=true
                snackbarWaiting.dismiss()
                errorlocation()
            }

    }


    //Beginning Permission Methods

    //check for location permission
    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED


    //Request User Permissions
    fun requestUserPermissions() {
        // An array with location-aware permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )

    }

    /**
     * Create an array with the permissions to show to the user.
     *
     * @param permissions An array with the permissions needed by this applications.
     *
     * @return An array with the permissions needed to ask to the user.
     */
    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission)
        return result
    }

    //Handle Permissions Result
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    endrental()
                } else {
                    permissiondialog()
                }
                return
            }
        }
    }

    //End of Permission Methods



    private fun errorlocation(){
        dialogBuilder =MaterialAlertDialogBuilder(this)
            .setMessage("We need your location to finish the ride. Please check app permissions or if location services are enabled on the device")
            .setPositiveButton("OK"){ dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun permissiondialog(){
        dialogBuilder =MaterialAlertDialogBuilder(this)
            .setMessage("The app need Location Permission to be able to end the ride. We are Sorry but we promise to respect your privacy ;)")
            .setPositiveButton("OK"){ dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    override fun onResume() {
        super.onResume()
        sensorMan.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        sensorMan.unregisterListener(this)
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // on sensor changed
    override fun onSensorChanged(event: SensorEvent?) {
        val x = event!!.values[0]
        val y = event!!.values[1]
        val z = event!!.values[2]

        val accel = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

//        findViewById<TextView>(R.id.accelX).text = "Acceleration X: $x"
//        findViewById<TextView>(R.id.accelY).text = "Acceleration Y: $y"
//        findViewById<TextView>(R.id.accelZ).text = "Acceleration Z: $z"
        findViewById<TextView>(R.id.accel).text = "Acceleration: $accel"

        if (accel > 20) {
            Toast.makeText(this, "TOO FAST", Toast.LENGTH_SHORT)
                .show()
            // you can insert you speed control here
            findViewById<Button>(R.id.lock).text = "Unlock"
        }
    }

    fun unlock(view: View){
        findViewById<Button>(R.id.lock).text = "lock"
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the variables to the outState bundle
        outState.putDouble("latitudeFrom", latitudeFrom)
        outState.putDouble("longitudeFrom", longitudeFrom)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the variables from the savedInstanceState bundle
        latitudeFrom = savedInstanceState.getDouble("latitudeFrom")
        longitudeFrom = savedInstanceState.getDouble("longitudeFrom")
    }

}