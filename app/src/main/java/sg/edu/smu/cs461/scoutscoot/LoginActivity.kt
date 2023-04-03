package sg.edu.smu.cs461.scoutscoot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var toast: Toast
    private  val database = Firebase.database(DATABASE_URL).reference

    private val signInLauncher =
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result -> this.onSignInResult(result) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database.keepSynced(true)
        setContentView(R.layout.activity_login)
        createSignInIntent()
    }
    public fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            //AuthUI.IdpConfig.PhoneBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build(),
            //AuthUI.IdpConfig.FacebookBuilder().build(),
            //AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.Theme_LoginTheme)
            .setLogo(R.drawable.ic_launcher_foreground)
            .build()
        signInLauncher.launch(signInIntent)

        // [END auth_fui_create_intent]
    }
    private fun onSignInResult(
        result: FirebaseAuthUIAuthenticationResult
    ) {
        if (result.resultCode == RESULT_OK) {
            val auth = Firebase.auth
            database.child("Users").orderByKey().equalTo(auth.currentUser?.uid!!).addListenerForSingleValueEvent(object:
                ValueEventListener {
                override fun onDataChange(datasnapshot: DataSnapshot) {
                    if(datasnapshot.exists().not()){
                        database.child("Users").child(auth.currentUser?.uid!!).setValue(User(auth.currentUser?.displayName))
                    }
                    toast = Toast.makeText(applicationContext, "User logged in the app", Toast.LENGTH_SHORT)
                    toast.show()
                    startMainActivity()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        } else{
            toast = Toast.makeText(applicationContext, "User not logged in the app", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    private fun startMainActivity() {
        val intent = Intent(this,
            MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent)
        //overridePendingTransition(0, 0);
        finish()
    }
}