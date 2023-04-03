package sg.edu.smu.cs461.scoutscoot

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@IgnoreExtraProperties
open class Ride(
    val scooter_ID: Int? = null,
    val scooter_Name: String? =null,
    val start_Time: Long? = null,
    val price: Double? = null,
    val locked: Boolean? =null,
    val end_time: Long? = null


) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.


    fun getTime(): String {
        var timeString: String = ""
        if(end_time!=null && start_Time!= null ){
            val diff: Long = end_time -start_Time as Long
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            timeString = hours.toString().plus("h : ").plus(minutes.toString().plus("m : ").plus(seconds.toString()).plus("s"))
        }
        return timeString
    }
}