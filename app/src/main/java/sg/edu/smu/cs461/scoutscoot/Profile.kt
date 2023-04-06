package sg.edu.smu.cs461.scoutscoot

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityListRideBinding
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityMainBinding
import sg.edu.smu.cs461.scoutscoot.databinding.FragmentProfileBinding


class Profile : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : FragmentProfileBinding
    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database(DATABASE_URL).reference
        user = auth.currentUser!!
        if (user != null){
            user.let{
                binding.greeting.text ="Welcome, ${it.email}"
            }
        }
//        binding.test.setOnClickListener{
//            switchPayment()
//        }
//
        binding.newtest.setOnClickListener{
            goendride()
        }

        binding.newnewtest.setOnClickListener({
            gostartride()
        })

        binding.RideLists.setOnClickListener{
            val intent = Intent (getActivity(), ListRideActivity::class.java)
            getActivity()?.startActivity(intent)

        }
        binding.logout.setOnClickListener{
            activity?.let{
                auth.signOut()
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
            }

        }

    }
//
//    fun switchPayment(){
//        val intent = Intent(requireActivity(), PaymentActivity::class.java)
//        intent.putExtra("priceKey","3000")
//        startActivity(intent)
//    }
//
    fun goendride(){
        val intent = Intent(requireActivity(),RideInfoActivity::class.java)
        startActivity(intent)
    }

    fun gostartride(){
        val intent = Intent(requireActivity(),StartRideActivity::class.java)
        intent.putExtra("scooterindex", "1002")
        startActivity(intent)
    }


}