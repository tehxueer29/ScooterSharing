/*
 * MIT License
 *
 * Copyright (c) 2023 Teh Xue Er
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dk.itu.moapd.scootersharing.xute.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.xute.databinding.ListRidesBinding
import dk.itu.moapd.scootersharing.xute.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.xute.models.Image
import dk.itu.moapd.scootersharing.xute.models.Scooter
import dk.itu.moapd.scootersharing.xute.utils.BUCKET_URL


/**
 * A class to customize an adapter with a `ViewHolder` to populate a Dummy dataset into a
 * `RecyclerView`.
 */
class RealtimeAdapter(
    private val itemClickListener: ItemClickListener,
    private val ridesUI: String,
    options: FirebaseRecyclerOptions<Scooter>
) :
    FirebaseRecyclerAdapter<Scooter, RealtimeAdapter.ViewHolder>(options) {

    /**
     * An internal view holder class used to represent the layout that shows a single `String`
     * instance in the `RecyclerView`.
     */
    class ViewHolder(private val binding: ListRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * This method binds the `ViewHolder` instance and more cleanly separate concerns between
         * the view holder and the adapter.
         *
         * @param scooter The current `Dummy` instance.
         */
        fun bind(scooter: Scooter) {
            binding.scooterName.text = scooter.name
            binding.scooterLocation.text = scooter.location
            "Session started: ${scooter.getTimestampToString()}".also { binding.scooterTime.text = it }
        }

        fun removeDeleteIcon() {
            binding.deleteRideIcon.visibility = View.GONE
        }

        fun removeReserveButton() {
            binding.reserveRideButton.visibility = View.GONE
        }
        fun removeTime() {
            binding.scooterTime.visibility = View.GONE
        }


        /**
         * This method binds the `ViewHolder` instance and more cleanly separate concerns between
         * the view holder and the adapter.
         *
         * @param image The current `Image` instance.
         */
//        TODO
        fun imgBind(image: Image?) {

            // Get the public thumbnail URL.
            val storage = Firebase.storage(BUCKET_URL)
//            TODO
            var imageRef = storage.reference.child("scooter_1.png")
            if (image != null) {
                    imageRef = storage.reference.child("images/${image.path}_thumbnail")
            }

            // Clean the image UI component.
            binding.imageView.setImageResource(0)

            // Download and set an image into the ImageView.
            imageRef.downloadUrl.addOnSuccessListener {
                Glide.with(itemView.context)
                    .load(it)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.imageView)
            }

            // Set the TextView visibility.
//            binding.textView.text = image.createdAt?.toDateString()
//            binding.textView.isVisible = displayDate
        }

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
        // Log system.
//        Log.d(TAG(), "Creating a new ViewHolder.")

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
     * @param scooter An instance of `Dummy` class.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element.
//        Log.i(TAG(), "Populate an item at position: $position")

        // Bind the view holder with the selected `Dummy` data.
        holder.apply {
            bind(scooter)
//            TODO

            imgBind(scooter.image)

            if (ridesUI == "StartRideUI") {
                removeDeleteIcon()
                removeTime()
            } else {
                removeReserveButton()
            }

            // Listen for long clicks in the current item.
            itemView.setOnLongClickListener {
                itemClickListener.onItemClickListener(scooter, position)
                true
            }
        }
    }

}
