package sg.edu.smu.cs461.scoutscoot

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class CustomAdapter(options: FirebaseRecyclerOptions<Ride>) :
    FirebaseRecyclerAdapter<Ride, CustomAdapter.ViewHolder>(options) {

    private var scootername: String? = null

    companion object {
        private val TAG = CustomAdapter::class.qualifiedName
    }

    /**
     * An internal view holder class used to represent the layout that shows a single `String`
     * instance in the `RecyclerView`.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.list_scooter_name)
        val time: TextView = view.findViewById(R.id.list_scooter_time)
        val price: TextView = view.findViewById(R.id.list_scooter_price)

    }

    /**
     * Called when the `RecyclerView` needs a new `ViewHolder` of the given type to represent an
     * item.
     *
     * This new `ViewHolder` should be constructed with a new `View` that can represent the items of
     * the given type. You can either create a new `View` manually or inflate it from an XML layout
     * file.
     *
     * The new `ViewHolder` will be used to display items of the adapter using
     * `onBindViewHolder(ViewHolder, int, List)`. Since it will be re-used to display different
     * items in the data set, it is a good idea to cache references to sub views of the `View` to
     * avoid unnecessary `findViewById(int)` calls.
     *
     * @param parent The `ViewGroup` into which the new `View` will be added after it is bound to an
     *      adapter position.
     * @param viewType The view type of the new `View`.
     *
     * @return A new `ViewHolder` that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_ride, parent, false)
        return ViewHolder(view)
    }
    /**
     * Called by the `RecyclerView` to display the data at the specified position. This method
     * should update the contents of the `itemView()` to reflect the item at the given position.
     *
     * Note that unlike `ListView`, `RecyclerView` will not call this method again if the position
     * of the item changes in the data set unless the item itself is invalidated or the new position
     * cannot be determined. For this reason, you should only use the `position` parameter while
     * acquiring the related data item inside this method and should not keep a copy of it. If you
     * need the position of an item later on (e.g., in a click listener), use
     * `getBindingAdapterPosition()` which will have the updated adapter position.
     *
     * Override `onBindViewHolder(ViewHolder, int, List)` instead if Adapter can handle efficient
     * partial bind.
     *
     * @param holder The `ViewHolder` which should be updated to represent the contents of the item
     *      at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param Scooter An instance of `Dummy` class.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int, ride: Ride) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Log.i(TAG, "Populate an item at position: $position")


        // Bind the view holder with the selected `String` data.
        holder.apply {

            title.text = ride.scooter_Name
            time.text = ride.getTime()
            price.text = ride.price.toString().plus("SGD")

            // Listen for long clicks in the current item.
            /*itemView.setOnLongClickListener {
                itemClickListener.onItemClickListener(dummy, position)
                true
            }*/
        }
    }

}