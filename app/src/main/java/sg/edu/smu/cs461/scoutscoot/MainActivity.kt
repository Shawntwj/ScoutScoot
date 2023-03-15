package sg.edu.smu.cs461.scoutscoot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import sg.edu.smu.cs461.scoutscoot.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //show home fragment first
        replaceFragment(Home())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(Home())
                R.id.qr -> replaceFragment(ScanQR())
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

}