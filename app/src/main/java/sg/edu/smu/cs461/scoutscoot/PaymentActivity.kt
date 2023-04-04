package sg.edu.smu.cs461.scoutscoot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONException



class PaymentActivity : AppCompatActivity() {

    // stripe needs these variables to make the payment sheet
    lateinit var paymentIntentClientSecret: String
    lateinit var configuration: PaymentSheet.CustomerConfiguration
    lateinit var paymentSheet: PaymentSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)


        // fetch by price displayed on the priceTV (i assume this will be loaded in after scan
        val it = intent
        val priceContext = it.getStringExtra("priceKey").toString()
        println(priceContext)
        val price = findViewById<TextView>(R.id.priceValue)
        val priceDisplay = (priceContext.toInt() / 100 ).toString()
        price.text= priceDisplay


        val timeContext = it.getStringExtra("timeKey").toString()
        println(timeContext)
        val time = findViewById<TextView>(R.id.timeValue)
        time.text= timeContext


        val paymentButton = findViewById<Button>(R.id.payment)

        paymentButton.isEnabled = false
        fetchAPI(priceContext)
        paymentButton.isEnabled = true

        // fetch API calls the stripe API (stored in a node.js on firebase)
        // takes in a price parameter used for query "https://payment?amt=$price"
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        // set the stripe variables on top into a function that will be ran when payment button is clicked


        paymentButton.setOnClickListener {
            if (paymentIntentClientSecret != null){
                paymentSheet?.presentWithPaymentIntent(paymentIntentClientSecret, PaymentSheet.Configuration("Codes Easy", configuration));
            }
        }

        // upon receiving payment result create a payment sheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    // implement function to change price will call fetchAPI again
//    fun changePrice(view: View){
//        val priceTV = findViewById<TextView>(R.id.priceValue)
//        priceTV.text = "2000"
//        val price = priceTV.text.toString()
//        fetchAPI(price)
//    }

    // handles payment result can be used to reroute after successful payment
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        if (paymentSheetResult is PaymentSheetResult.Failed) {
            Toast.makeText(this, (paymentSheetResult as PaymentSheetResult.Failed).error.message, Toast.LENGTH_SHORT).show()
        }
        if (paymentSheetResult is PaymentSheetResult.Completed) {
            println("success")
            Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("paymentSuccessful", true)
            startActivity(intent)
        }
    }


    private fun fetchAPI(price: String) {
//        val url = "https://demo.codeseasy.com/apis/stripe/"
        val url = "https://us-central1-stripepayment-ac54d.cloudfunctions.net/stripePayments?amt=$price"
        val queue = Volley.newRequestQueue(this)

        println("running fetchAPI")

        val request = object : JsonObjectRequest(Method.POST, url, null,
            { response ->
                try {
                    println("request here")
                    val paymentIntent = response.getString("paymentIntent")
                    val ephemeralKey = response.getString("ephemeralKey")
                    val customer = response.getString("customer")
                    val publishableKey = response.getString("publishableKey")

                    configuration = PaymentSheet.CustomerConfiguration(
                        customer,
                        ephemeralKey
                    )
                    paymentIntentClientSecret = paymentIntent
                    PaymentConfiguration.init(applicationContext, publishableKey)
                    // Pass the payment information to Stripe SDK and continue with payment flow

                } catch (e: JSONException) {
                    println("inside response failed")
                    println(e)
                    e.printStackTrace()
                }
            },
            { error ->
                println("no response")
                error.printStackTrace()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(request)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the variables to the outState bundle
        outState.putString("paymentIntentClientSecretKey", paymentIntentClientSecret)
        outState.putParcelable("configurationKey", configuration)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the variables from the savedInstanceState bundle
        paymentIntentClientSecret = savedInstanceState.getString("paymentIntentClientSecretKey").toString()
        configuration = savedInstanceState.getParcelable("configurationKey")!!
    }






}


