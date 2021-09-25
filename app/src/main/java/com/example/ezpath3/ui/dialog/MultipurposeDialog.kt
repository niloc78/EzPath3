package com.example.ezpath3.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.example.ezpath3.R
import com.example.ezpath3.databinding.AddErrandDialogBinding
import com.example.ezpath3.databinding.ConfirmLoadPathDialogBinding
import com.example.ezpath3.databinding.LocationDialogBinding
import com.example.ezpath3.databinding.SaveSetDialogBinding
import java.lang.ClassCastException

class MultipurposeDialog
    constructor(val type : DialogTypes): AppCompatDialogFragment() {

    private lateinit var listener: MultipurposeDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        return when (type) {
            is DialogTypes.AddErrandDialog -> { //DONE
                val binding = AddErrandDialogBinding.inflate(requireActivity().layoutInflater, null , false)
                builder.setView(binding.root)
                        .setPositiveButton(type.POSITIVE_BUTTON) { _, _ ->
                            val errand = binding.editErrand.text.toString().replaceFirst(binding.editErrand.text.toString().first(), binding.editErrand.text.toString().first().toUpperCase())
                            listener.addErrand(errandName = errand)
                        }
                        .setNegativeButton(type.NEGATIVE_BUTTON) { _, _ ->

                        }
                        .show()

            }
            is DialogTypes.ConfirmLoadSetDialog -> { //DONE
                val binding = ConfirmLoadPathDialogBinding.inflate(requireActivity().layoutInflater, null, false)
                binding.loadThisErrandSet.text = type.CONTENT
                //info: get errands within set and display
               var setInfo = ""
                type.set.forEach {
                    setInfo += "$it, "
                }
                binding.setListText.text = setInfo

                //info: list out all errands in content
                builder.setView(binding.root)
                        .setPositiveButton(type.POSITIVE_BUTTON) { _, _ ->
                            listener.confirmLoadSet(setName = type.setName)
                        }
                        .setNegativeButton(type.NEGATIVE_BUTTON) { _, _ ->

                        }
                        .show().setButtonColors()

            }
            is DialogTypes.EnableLocationDialog -> {
                val binding = LocationDialogBinding.inflate(requireActivity().layoutInflater, null, false)
                binding.enableLocationText.text = if (type.isLocationCurrentlyEnabled) type.CONTENT_LOCATION_ENABLED else type.CONTENT_LOCATION_DISABLED
                builder.setView(binding.root)
                        .setPositiveButton(if(type.isLocationCurrentlyEnabled) type.POSITIVE_BUTTON_LOCATION_ENABLED else type.POSITIVE_BUTTON_LOCATION_DISABLED) { _, _ -> // URGENT todo positive button based on condition if location enabled type.POSITIVEBUTTONLOCATIONENABLED or type.POSITIVEBUTTONLOCATIONDISABLED DONE
                            listener.toggleLocationEnabled(enable = !type.isLocationCurrentlyEnabled) // todo set based on condition URGENT DONE
                        }
                        .setNegativeButton(type.NEGATIVE_BUTTON) { _, _ ->

                        }
                        .show().setButtonColors()
            }
            is DialogTypes.SaveSetDialog -> { //todo if 0 errands show toast DONE
                val binding = SaveSetDialogBinding.inflate(requireActivity().layoutInflater, null, false)
                builder.setView(binding.root)
                        .setPositiveButton(type.POSITIVE_BUTTON) { _, _ ->
                            val name = binding.editSetName.text.toString()
                            listener.saveSet(setName = name)
                        }
                        .setNegativeButton(type.NEGATIVE_BUTTON) { _, _ ->

                        }
                        .show()
            }
        }


    }

    private fun AlertDialog.setButtonColors() : AlertDialog {
                this.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.white
                        ))
                this.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.buttonteal
                        ))

                this.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.black
                        ))
        return this
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as MultipurposeDialogListener
        } catch (e : ClassCastException) {
            throw ClassCastException(type.EXCEPTION_MESSAGE)
        }
    }

    interface MultipurposeDialogListener {
        fun addErrand(errandName : String) {}
        fun confirmLoadSet(setName : String) {}
        fun toggleLocationEnabled(enable : Boolean) {}
        fun saveSet(setName : String) {}
    }

}