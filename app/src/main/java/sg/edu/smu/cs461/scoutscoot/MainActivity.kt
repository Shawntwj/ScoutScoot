package sg.edu.smu.cs461.scoutscoot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityMainBinding
    companion object {
        private val TAG = MainActivity::class.qualifiedName
        private const val ALL_PERMISSIONS_RESULT = 1011
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val rideEnded = intent.getBundleExtra("rideEnded", false)
        auth = Firebase.auth
        binding = ActivityMainBinding.inflate(layoutInflater)

//        if (zuth.currentUser == null){
//            loadFragment(Home())
//            requestUserPermissions()
//            if(rideEnded){
//                rideEndedDialog()
//            }
//        }
        setContentView(binding.root)
        //show home fragment first
        val paymentSuccessful = intent.getBooleanExtra("paymentSuccessful", false)

        if (paymentSuccessful) {
            // Navigate to the Home fragment
//            loadFragment(Profile())

            supportFragmentManager.popBackStackImmediate()

            val bundle = Bundle()
            bundle.putString("myKey", "paymentSuccessful")
            val fragment = Profile()
            fragment.arguments = bundle

            // Replace with the Profile fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
        } else {

            replaceFragment(Home())


        }


        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(Home())
                R.id.qr -> replaceFragment(ScanQRButton())
                R.id.profile -> replaceFragment(Profile())

                else ->{

                }

            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction().apply{
                replace(R.id.frame_layout, fragment)
                commit()
            }
    }
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
    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission)
        return result
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadFragment(Home())
                } else {

                }
                return
            }
        }
    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction
            .replace(R.id.fragment_container_view,fragment)

        transaction.addToBackStack(null)
        transaction.commit()
    }
}