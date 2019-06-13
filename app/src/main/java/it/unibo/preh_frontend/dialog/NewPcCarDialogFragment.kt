package it.unibo.preh_frontend.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import it.unibo.preh_frontend.model.*
import androidx.fragment.app.DialogFragment
import it.unibo.preh_frontend.R





class NewPcCarDialogFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pccar, container, false)

        val saveAndExitButton = root.findViewById<ImageButton>(R.id.pcCar_image_button)
        saveAndExitButton.setOnClickListener {
           dialog!!.cancel()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels)
        val height = (metrics.heightPixels)
        dialog!!.window!!.setLayout(95 * width / 100, 60 * height / 100)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {

            }
        }
    }
}