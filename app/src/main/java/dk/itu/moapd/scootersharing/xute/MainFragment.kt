/**MIT License

Copyright (c) 2023 Teh Xue Er

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package dk.itu.moapd.scootersharing.xute

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.scootersharing.xute.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainFragment::class.qualifiedName
        lateinit var ridesDB: RidesDB
        private lateinit var adapter: CustomArrayAdapter
    }

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var binding: FragmentMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            FragmentMainBinding.inflate(layoutInflater, container,
                false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Singleton to share an object between the app activities .
        ridesDB = RidesDB.get(requireContext())

//        get all rides object
        val data = ridesDB.getRidesList()

        // Create the custom adapter to populate a list of dummy objects.
        adapter = CustomArrayAdapter(data)

        // makes all variables start with [mainBinding.]
        with(binding) {
//            // The start ride button listener.
//            startRideButton.setOnClickListener {
//                contentList.recyclerView.isVisible = false
//                val intent = Intent(this@MainFragment, StartRideActivity::class.java)
//                startActivity(intent)
//            }
//
//            updateRideButton.setOnClickListener {
//                contentList.recyclerView.isVisible = false
//                val intent = Intent(this@MainFragment, UpdateRideActivity::class.java)
//                startActivity(intent)
//            }
//
            listRideButton.setOnClickListener {
//                // Define the list view adapter.
            contentList.recyclerView.isVisible = true
            contentList.recyclerView.layoutManager = LinearLayoutManager(context)
            contentList.recyclerView.adapter = MainFragment.adapter
            }
        }

    }

}