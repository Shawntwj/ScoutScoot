package sg.edu.smu.cs461.scoutscoot

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val name: String? = null,
    val inRental: String? = "false"
    //val Rides: List<Ride> = listOf()
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}