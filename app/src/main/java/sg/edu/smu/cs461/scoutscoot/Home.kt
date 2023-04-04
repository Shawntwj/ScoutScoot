package sg.edu.smu.cs461.scoutscoot

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.*
import com.google.android.gms.maps.MapsInitializer.Renderer
import sg.edu.smu.cs461.scoutscoot.R
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.FragmentHomeBinding

class Home : Fragment(), OnMapsSdkInitializedCallback {
    private lateinit var markerIcon: BitmapDescriptor
    private lateinit var binding: FragmentHomeBinding
    private val markerIds: HashMap<Marker, String> = HashMap()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mapFragment: SupportMapFragment? = null

    companion object {
        private val TAG = Home::class.qualifiedName

    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), Renderer.LATEST, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        markerIcon = getMarkerIconFromDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_electric_scooter_24)!!)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        // Add a marker in SMU and move the camera
        val smu = LatLng(1.2963,103.8502)
        Firebase.database(DATABASE_URL).reference.child("Scooter").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                googleMap.clear();

                //val toast = Toast.makeText(activity, "data chnaged", Toast.LENGTH_SHORT)
                //toast.show()
                for(postSnapshot in dataSnapshot.children){
                    if(postSnapshot.getValue<Scooter>()?.rented==false){
                        val tempScooter = postSnapshot.getValue<Scooter>()
                        val key = postSnapshot.key

                        val marker = tempScooter?.let {
                            MarkerOptions()
                                .position(tempScooter.position)
                                .position(tempScooter.position)
                                .title(tempScooter.name)
                                .icon(markerIcon)
                        }?.let { googleMap.addMarker(it) }
                        marker?.let { markerIds.put(it, key!!) }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })


        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener {
            val intent = Intent(context, StartRideActivity::class.java)
            intent.putExtra("scooterindex", markerIds.get(it))
            startActivity(intent)

        })

        if (!checkPermission()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location!=null){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude), 16f))
                }
                else{
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(1.2963,103.8502), 12f))
                }

            }
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

        }
        else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(1.2963,103.8502), 12f))
            (activity as MainActivity).requestUserPermissions()
        }


    }
    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST ->
                Log.d(TAG, "The latest version of the renderer is used.")
            Renderer.LEGACY ->
                Log.d(TAG, "The legacy version of the renderer is used.")
        }
    }

}