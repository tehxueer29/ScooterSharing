package dk.itu.moapd.scootersharing.xute

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * A class to customize an adapter with a `ViewHolder` to populate a dummy dataset into a `ListView`.
 */
class CustomArrayAdapter(context: Context, private var resource: Int, data: List<Scooter>) :
    ArrayAdapter<Scooter>(context, R.layout.list_rides, data) {

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
    private class ViewHolder(view: View) {
        val scooterName: TextView = view.findViewById(R.id.scooter_name)
        val scooterLocation: TextView = view.findViewById(R.id.scooter_location)
        val scooterTime: TextView = view.findViewById(R.id.scooter_time)
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
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder

        // The old view should be reused, if possible. You should check that this view is non-null
        // and of an appropriate type before using.
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(resource, parent, false)
            viewHolder = ViewHolder(view)
        } else
            viewHolder = view.tag as ViewHolder

        // Get the selected item in the dataset.
        val dummy = getItem(position)
        Log.d(TAG, "Populate an item at position: $position")

        // Populate the view holder with the selected `Scooter` data.
        viewHolder.scooterName.text = parent.context.getString(R.string.scooter_name, dummy?.name)
        viewHolder.scooterLocation.text = parent.context.getString(R.string.scooter_location, dummy?.location)
        viewHolder.scooterTime.text = parent.context.getString(R.string.scooter_time, dummy?.getTimestampToString())

        // Set the new view holder and return the view object.
        view?.tag = viewHolder
        return view!!
    }

}
