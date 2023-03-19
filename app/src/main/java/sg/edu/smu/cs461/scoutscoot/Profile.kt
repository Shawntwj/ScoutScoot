package sg.edu.smu.cs461.scoutscoot


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.FragmentProfileBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth
    private lateinit var database: DatabaseReference

    // need to change this
    val DATABASE_URL = ""

    private lateinit var myAdapter:ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        user = FirebaseAuth.getInstance()

        if(user.currentUser !=null){
            user.currentUser?.let {
                binding.userName.text = it.email
            }
            database = Firebase.database(DATABASE_URL).reference
            database.keepSynced(true)

            // Create the search query.
            val query = database.child("rides").child(FirebaseAuth.getInstance().currentUser!!.uid)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    Log.i("Profile", snapshot.toString())
                    val name = snapshot.child("name").getValue(String::class.java)

                    with(binding) {
                        userName.setText(name)
                    }
                } else {
                    Log.i("Profile", task.exception!!.message!!) //Don't ignore potential errors!
                }
            }

            // Execute a query in the database to fetch appropriate data.
            val options = FirebaseRecyclerOptions.Builder<Profile>()
                .setQuery(query
                ) { snapshot ->
                    val id = snapshot.child("id").getValue(String::class.java)
                    val status = snapshot.child("status").getValue(String::class.java)
                    val startTime = snapshot.child("start_time").getValue(Long::class.java)
                    val endTime = snapshot.child("end_time").getValue(Long::class.java)
                    val rentalTime = snapshot.child("rental_time").getValue(Long::class.java)
                    val initialLat = snapshot.child("initial_lat").getValue(Double::class.java)
                    val initialLong = snapshot.child("initial_long").getValue(Double::class.java)
                    val currentLat = snapshot.child("current_lat").getValue(Double::class.java)
                    val currentLong = snapshot.child("current_long").getValue(Double::class.java)
                    val price = snapshot.child("price").getValue(Double::class.java)
                    val userId = snapshot.child("user_id").getValue(String::class.java)
                    val scooterId = snapshot.child("scooter_id").getValue(Int::class.java)

                    Profile()
                }
                .setLifecycleOwner(this)
                .build()

//            myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)
//            val lv = findViewById<ListView>(R.id.transactionsLV)
//            lv.adapter = myAdapter


        }

        binding.logout.setOnClickListener{
            user.signOut()
            startActivity(Intent(activity,LoginActivity::class.java))
            activity?.finish()
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}