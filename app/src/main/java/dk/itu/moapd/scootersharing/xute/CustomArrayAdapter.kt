package dk.itu.moapd.scootersharing.xute

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.xute.databinding.ListRidesBinding

/**
 * A class to customize an adapter with a `ViewHolder` to populate a dummy dataset into a
 * `RecyclerView`.
 */
class CustomArrayAdapter(private val data: ArrayList<Scooter>) :
    RecyclerView.Adapter<CustomArrayAdapter.ViewHolder>() {


    /**
     * A set of private constants used in this class.
     */
    companion object {
        private val TAG = CustomArrayAdapter::class.qualifiedName
    }

    /**
     * An internal view holder class used to represent the layout that shows a single `DummyModel`
     * instance in the `ListView`.
     */
    class ViewHolder(private val binding: ListRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * This method binds the `ViewHolder` instance and more cleanly separate concerns between
         * the view holder and the adapter.
         *
         * @param dummy The current `Dummy` object.
         */
        fun bind(dummy: Scooter) {
            binding.scooterName.text = binding.root.context.getString(
                R.string.scooter_name, dummy.name
            )
            binding.scooterLocation.text = binding.root.context.getString(
                R.string.scooter_location, dummy.location
            )
            binding.scooterTime.text = binding.root.context.getString(
                R.string.scooter_time, dummy.getTimestampToString()
            )
        }
    }

    /**
     * Get the `View` instance with information about a selected `DummyModel` from the dataset.
     *
     * @param position The position of the specified item.
     * @param convertView The current view holder.
     * @param parent The parent view which will group the view holder.
     *
     * @return A new view holder populated with the selected `DummyModel` data.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Log system.
        Log.d(TAG, "Creating a new ViewHolder.")

        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
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
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val dummy = data[position]
        Log.d(TAG, "Populate an item at position: $position")

        // Bind the view holder with the selected `DummyModel` data.
        holder.bind(dummy)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = data.size


}
