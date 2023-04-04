package sg.edu.smu.cs461.scoutscoot

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@IgnoreExtraProperties
open class Ride(
    val scooter_ID: Int? = null,
    val scooter_Name: String? =null,
    val initial_latlong: String? = null,
    val final_latlong:String? = null,
    val start_Time: Long? = null,
    val price: Double? = null,
    val locked: Boolean? =null,
    val end_time: Long? = null


) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

//    fun getLat(): Double {
//        val lat= this.initial_latlong?.substring(0,this.initial_latlong.indexOf(","))
//        return lat?.toDouble()!!
//    }
//
//    fun getLon(): Double {
//        val long = this.initial_latlong?.substring(this.initial_latlong.indexOf(",")+1, this.initial_latlong.length)
//        return long?.toDouble()!!
//    }
//    fun getFLat(): Double {
//        val lat= this.final_latlong?.substring(0,this.final_latlong.indexOf(","))
//        return lat?.toDouble()!!
//    }
//
//    fun getFLon(): Double {
//        val long = this.final_latlong?.substring(this.final_latlong.indexOf(",")+1, this.final_latlong.length)
//        return long?.toDouble()!!
//    }
//
//    fun getInitialPosition(): LatLng {
//        return let { LatLng(it.getLat(), it.getLon()) }
//    }
//    fun getFinalPosition(): LatLng {
//        return let { LatLng(it.getFLat(), it.getFLon()) }
//    }

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