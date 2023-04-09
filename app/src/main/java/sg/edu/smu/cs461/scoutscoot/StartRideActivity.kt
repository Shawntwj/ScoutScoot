package sg.edu.smu.cs461.scoutscoot

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityStartRideBinding

class StartRideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartRideBinding
    private var auth = FirebaseAuth.getInstance()
    private val database = Firebase.database(DATABASE_URL).reference
    private var scooter_id: Int? = null
    private lateinit var scooter: Scooter
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var latitude = 0.0
    private var longitude = 0.0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartRideBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothAdapter = bluetoothManager.adapter

        val index = intent.getStringExtra("scooterindex")

        latitude = intent.getDoubleExtra("latitude",0.0)
        longitude = intent.getDoubleExtra("longitude",0.0)


        database.child("Scooter").orderByKey().equalTo(index).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    for(postSnapshot in dataSnapshot.children ){
                        if(postSnapshot.getValue<Scooter>()?.rented==false){
                            scooter_id = postSnapshot.key!!.toInt()
                            scooter = postSnapshot.getValue<Scooter>()!!
                            binding.scootername.setText(scooter.name)
                            binding.range.setText(scooter.getRange().toString().plus(" km"))
                            binding.price.setText("$0.50 / \n 30 seconds")
                        }
                        else{
                            setResult(Activity.RESULT_OK)
                            finish() //return to back activity
                        }

                    }
                }else {
                    setResult(Activity.RESULT_OK)
                    finish() //return to back activity
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.startRide.setOnClickListener(){
            if (index != null) {
                startride(index)
            }
//            val broadcast =Intent(this, Vibration::class.java)
//            sendBroadcast(broadcast)
//            if((!bluetoothAdapter.isEnabled)){
//                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//            }else{
//                startride()
//
//            }
        }
    }

    private fun startride(scooterIndex:String){
        val fragment = ScanQR()
        val mBundle = Bundle()
        mBundle.putString("scooterindex",scooterIndex)

        fragment.arguments = mBundle
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment).commit()
        //Rental ID generated by firebase realtime database
        val uuid =  database.child("Users")
            .child(auth.currentUser?.uid!!)
            .push()
            .key
        database.child("Users")
            .child((auth.currentUser?.uid!!))
            .child("Rides")
            .child(uuid!!)
            .setValue(Ride(scooter_id,scooter.name,scooter.where,"",System.currentTimeMillis(),0.0, false))

        database.child("Scooter")
            .child(scooter_id.toString())
            .child("rented")
            .setValue(true)

        database.child("Users")
            .child((auth.currentUser?.uid!!))
            .child("inRental")
            .setValue(uuid)

        val intent = Intent(this,RideInfoActivity::class.java)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        startActivity(intent)

    }




}