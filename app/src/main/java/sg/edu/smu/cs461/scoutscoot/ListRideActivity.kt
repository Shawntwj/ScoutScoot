package sg.edu.smu.cs461.scoutscoot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
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
        val query = database.child("Users").child(auth.currentUser!!.uid).child("Rides")
        val options = FirebaseRecyclerOptions.Builder<Ride>().setQuery(query,Ride::class.java).setLifecycleOwner(this).build()


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