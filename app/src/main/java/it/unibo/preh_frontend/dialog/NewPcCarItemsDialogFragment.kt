package it.unibo.preh_frontend.dialog

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import it.unibo.preh_frontend.R
import it.unibo.preh_frontend.utils.PermissionManager
import it.unibo.preh_frontend.model.NewPcCarData
import it.unibo.preh_frontend.utils.HistoryManager
import java.io.IOException
import java.util.Locale

class NewPcCarItemsDialogFragment : DialogFragment() {
    private lateinit var buttonToDisable: Button
    private var buttonToEnable: Button? = null
    private lateinit var locationText: TextView
    private lateinit var placeEditText: EditText
    private lateinit var replaceButton: Button
    private lateinit var exitDialog: AlertDialog
    private val reqSetting = LocationRequest.create().apply {
        fastestInterval = 1000
        interval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pccar_items_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(false)
        locationText = root.findViewById(R.id.location)
        placeEditText = root.findViewById(R.id.place_edit_text)
        replaceButton = root.findViewById(R.id.replace_button)
        createDialog()
        updateLocation()

        replaceButton.setOnClickListener {
            placeEditText.setText(locationText.text)
        }

        root.findViewById<ImageButton>(R.id.pccar_items_image_button).setOnClickListener {
            if (placeEditText.text.toString() != "") {
                buttonToDisable.isEnabled = false
                buttonToEnable?.isEnabled = true
                dialog?.cancel()
            } else if (!exitDialog.isShowing)
                exitDialog.show()
        }
        return root
    }

    private fun createDialog() {
        exitDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Uscire senza salvare?")
            setMessage("Inserimento incompleto")
            setCancelable(true)
            setPositiveButton("Si") { d, _ ->
                d.cancel()
                dialog?.dismiss()
            }
            setNegativeButton("No") { d, _ -> d.cancel() }
        }.create()
    }

    override fun onResume() {
        super.onResume()
        val metrics = resources.displayMetrics
        dialog?.window?.setLayout(metrics.widthPixels, 8*metrics.heightPixels / 10)
    }

    override fun onCancel(dialog: DialogInterface) {
        arguments?.getString("historyName")?.let {
            val newPcCarData = NewPcCarData(it, placeEditText.text.toString())
            val sharedPreferences = requireContext().getSharedPreferences("preHData", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(it, Gson().toJson(newPcCarData)).apply()
            HistoryManager.addEntry(newPcCarData, sharedPreferences)
        }
        super.onCancel(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity!!, theme) {
            override fun onBackPressed() {
            }
        }
    }

    private fun updateLocation() {
        if (PermissionManager.checkPermission(requireActivity(), requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            val locationUpdates = object : LocationCallback() {
                override fun onLocationResult(localResult: LocationResult) {
                    val locationUpdates = this
                    localResult.locations.last().apply {
                        context?.let {
                            val geocoder = Geocoder(it, Locale.getDefault())
                            try {
                                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                if (addresses.isNotEmpty()) {
                                    requireActivity().runOnUiThread {
                                        locationText.text = addresses[0].getAddressLine(0)
                                        replaceButton.isEnabled = true
                                    }
                                    fusedLocationClient?.removeLocationUpdates(locationUpdates)
                                }
                            } catch (ex: IOException) {}
                        }
                    }
                }
            }
            fusedLocationClient?.requestLocationUpdates(reqSetting, locationUpdates, null)
        }
    }

    companion object {
        fun newInstance(historyName: String, buttonToDisable: Button, buttonToEnable: Button?) = NewPcCarItemsDialogFragment().apply {
            arguments = Bundle().apply {
                putString("historyName", historyName)
            }
            this.buttonToDisable = buttonToDisable
            this.buttonToEnable = buttonToEnable
        }
    }
}
