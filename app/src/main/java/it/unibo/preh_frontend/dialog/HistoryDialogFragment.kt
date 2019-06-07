package it.unibo.preh_frontend.dialog


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment

import it.unibo.preh_frontend.R
import android.widget.ArrayAdapter
import android.widget.ListView


class HistoryDialogFragment : DialogFragment() {

    private var aList: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root =  inflater.inflate(R.layout.fragment_history_dialog, container, false)

        val storiaList = root.findViewById(R.id.history_list) as ListView
        val mAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, aList)

        storiaList.adapter = mAdapter

        val buttonTest = root.findViewById<ImageButton>(R.id.imageButton4)
        buttonTest.setOnClickListener{
            aList.add("coccode")
            aList.add("piscioi")
            mAdapter.notifyDataSetChanged()
        }


        return root
    }

    override fun onResume() {
        super.onResume()
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels)
        val height = (metrics.heightPixels)
        dialog!!.window!!.setLayout(9 * width / 10,height)
    }


}
