package sg.edu.smu.cs461.scoutscoot

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.FragmentScanQRBinding
import java.io.IOException


class ScanQR : Fragment() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private var scooter_id: Int? = null
    private lateinit var scooter: Scooter
    private var auth = FirebaseAuth.getInstance()
    private val database = Firebase.database(DATABASE_URL).reference
    private lateinit var _binding: FragmentScanQRBinding
    private val binding get() = _binding!!
    private var index=""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach() called")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate() called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState)
        _binding = FragmentScanQRBinding.inflate(inflater, container, false)
        index = arguments?.getString("scooterindex").toString()
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        Log.i(TAG, "onCreateView() called")
        return binding.root
    }


    private fun setupControls() {
//        var args = this.arguments
//        var rideId = args?.get("scanner_ride_id")
//        Log.i("rideid FROM scanner fragment", rideId.toString())

        barcodeDetector =
            BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(requireContext(), "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue
                    Log.i(TAG, "Scanned QR code, value: $scannedValue")
//                    val bundle = arguments
//                    val message = bundle!!.getString("mText")
                    if(scannedValue == index) {
                        database.child("Scooter").orderByKey().equalTo(index)
                            .addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (postSnapshot in dataSnapshot.children) {
                                            if (postSnapshot.getValue<Scooter>()?.rented == false) {
                                                scooter_id = postSnapshot.key!!.toInt()
                                                scooter = postSnapshot.getValue<Scooter>()!!
                                                Toast.makeText(
                                                    activity,
                                                    "Scanned Successfully! Starting Ride",
                                                    Toast.LENGTH_SHORT
                                                )
                                                //Don't forget to add this line printing value or finishing activity must run on main thread
                                                activity?.runOnUiThread {
                                                    cameraSource.stop()

                                                    requireActivity().supportFragmentManager
                                                        .beginTransaction()
                                                        .remove(this@ScanQR)
                                                        .commit()
                                                    requireActivity().supportFragmentManager.popBackStack()
                                                    startride()
                                                }
                                            } else {
//                                        setResult(Activity.RESULT_OK)
//                                        finish() //return to back activity
                                            }

                                        }
                                    } else {
                                        Toast.makeText(
                                            activity,
                                            "QR Code does not match, try again",
                                            Toast.LENGTH_SHORT
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }



                } else {
                    Toast.makeText(requireContext(), "value- else", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun startride(){

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

        val intent = Intent (activity, RideInfoActivity::class.java)
        activity?.startActivity(intent)


    }
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            requireContext() as Activity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(requireContext(), requireContext().getString(R.string.toast_camera_deny), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}