package sg.edu.smu.cs461.scoutscoot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityListRideBinding
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityMainBinding

class ListRideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRideBinding
    private lateinit var data: ArrayList<Scooter>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser
    companion object {
        //lateinit var ridesDB: RidesDB
        private lateinit var adapter: CustomAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityListRideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Firebase.database(DATABASE_URL).reference


        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            auth.currentUser?.let {
//                binding.userName.text = it.email
            }
            database = Firebase.database(DATABASE_URL).reference
            database.keepSynced(true)

            // Create the search query.
            val query = database.child("Users").child(auth.currentUser!!.uid).child("Rides")
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    Log.i("Profile", snapshot.toString())
                    val name = snapshot.child("name").getValue(String::class.java)

                    with(binding) {
//                        userName.setText(name)
                    }
                } else {
                    Log.i("Profile", task.exception!!.message!!) //Don't ignore potential errors!
                }
            }
            val options = FirebaseRecyclerOptions.Builder<Ride>().setQuery(query, Ride::class.java)
                .setLifecycleOwner(this).build()
//            // Execute a query in the database to fetch appropriate data.
//            val options = FirebaseRecyclerOptions.Builder<Ride>()
//                .setQuery(query
//                ) { snapshot ->
//                    val id = snapshot.child("id").getValue(String::class.java)
//                    val status = snapshot.child("status").getValue(String::class.java)
//                    val startTime = snapshot.child("start_time").getValue(Long::class.java)
//                    val endTime = snapshot.child("end_time").getValue(Long::class.java)
//                    val rentalTime = snapshot.child("rental_time").getValue(Long::class.java)
//                    val initialLat = snapshot.child("initial_lat").getValue(Double::class.java)
//                    val initialLong = snapshot.child("initial_long").getValue(Double::class.java)
//                    val currentLat = snapshot.child("current_lat").getValue(Double::class.java)
//                    val currentLong = snapshot.child("current_long").getValue(Double::class.java)
//                    val price = snapshot.child("price").getValue(Double::class.java)
//                    val userId = snapshot.child("user_id").getValue(String::class.java)
//                    val scooterId = snapshot.child("scooter_id").getValue(Int::class.java)
//
//                    Ride()
//                }
//                .setLifecycleOwner(this)
//                .build()


            adapter = CustomAdapter(options)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.addItemDecoration(
                DividerItemDecoration(
                    this,
                    LinearLayoutManager(this).orientation
                )
            )

        }
    }
}