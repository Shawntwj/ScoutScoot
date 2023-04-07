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
        var priceContext = it.getStringExtra("priceKey").toString()
        val price = findViewById<TextView>(R.id.priceValue)
        val priceDisplay = (priceContext.toInt() / 100 ).toString()
        price.text= priceDisplay

        // HERE: fetch distance and location postal
        var fromValue = it.getStringExtra("fromKey")
        var toValue = it.getStringExtra("toKey")


//        var distanceValue = it.getStringExtra("distance").toString()
        val from = findViewById<TextView>(R.id.fromDestination)
        val to = findViewById<TextView>(R.id.toDestination)
//        val distance = findViewById<TextView>(R.id.distanceValue)
        from.text = fromValue
        to.text = toValue
//        distance.text = distanceValue


        // default payment for stripe is 0.5
        if (priceContext.toInt() == 0){
            priceContext = "50"
            price.text= "0.5"
        }

        // transform time to appropriate hours and minutes
        val defaultValue = 0 // default value to return if the extra is not found
        val timeContext = it.getIntExtra("timeKey", defaultValue)
        val timeHr = timeContext.div(60)
        val timeMin = timeContext % 60
        var timeMinString = timeMin.toString()
        if (timeMin < 10){
            timeMinString = "0$timeMin"
        }
        val timeHrView = findViewById<TextView>(R.id.timeValueHr)
        val timeMinView = findViewById<TextView>(R.id.timeValueMin)
        timeHrView.text= timeHr.toString()
        timeMinView.text= timeMinString

        // disable button before fetch API runs
        val paymentButton = findViewById<Button>(R.id.payment)
        paymentButton.isEnabled = false

        // fetch API calls the stripe API (stored in a node.js on firebase)
        // takes in a price parameter used for query "https://payment?amt=$price"
        fetchAPI(priceContext)

        // set the stripe variables on top into a function that will be ran when payment button is clicked
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Pass the payment information to Stripe SDK upon payment button click and continue with payment flow
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
            Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("paymentSuccessful", true)
            startActivity(intent)
        }
    }


    private fun fetchAPI(price: String) {
        val url = "https://us-central1-stripepayment-ac54d.cloudfunctions.net/stripePayments?amt=$price"
        val queue = Volley.newRequestQueue(this)

        println("running fetchAPI")

        val request = object : JsonObjectRequest(Method.POST, url, null,
            { response ->
                try {
                    println("request has arrived")
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

                    // enable the button again after request has arrived
                    val paymentButton = findViewById<Button>(R.id.payment)
                    paymentButton.isEnabled = true


                } catch (e: JSONException) {
                    println("inside response failed")
                    println(e)
                    e.printStackTrace()
                }
            },
            { error ->
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


