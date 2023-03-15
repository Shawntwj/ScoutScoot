package sg.edu.smu.cs461.scoutscoot
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.maps.android.clustering.ClusterItem

@IgnoreExtraProperties
open class Scooter(
    var name: String? = null,
    val where: String? = null,
    val rented: Boolean? = null,
    val battery: Double? = null,
    val address: String? = null

) :ClusterItem {

    /**
     * Convert a instance of `Scooter` class into a `Map` to update the database on the RealTime
     * Firebase.
     *
     * @return A map with the table column name as the `key` and the class attribute as the `value`.
     */
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "where" to where,
            "rented" to rented

        )
    }

    fun getLat(): Double {
        val lat= this.where?.substring(0,this.where.indexOf(","))
        return lat?.toDouble()!!
    }

    fun getLon(): Double {
        val long = this.where?.substring(this.where.indexOf(",")+1, this.where.length)
        return long?.toDouble()!!
    }

    override fun getPosition(): LatLng {
        return let { LatLng(it.getLat(), it.getLon()) }
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String? {
        return address
    }





}
