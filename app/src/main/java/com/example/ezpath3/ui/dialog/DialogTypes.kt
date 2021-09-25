package com.example.ezpath3.ui.dialog

import com.example.ezpath3.R

sealed class DialogTypes {
    val EXCEPTION_MESSAGE : String = "must implement MultipurposeDialogListener"

    object AddErrandDialog : DialogTypes() {
        const val POSITIVE_BUTTON : String = "Add"
        const val NEGATIVE_BUTTON : String = "Cancel"
        const val LAYOUT_ID : Int = R.layout.add_errand_dialog
    }

    data class ConfirmLoadSetDialog(var setName : String, var set : Set<String>) : DialogTypes() {
         val POSITIVE_BUTTON : String = "Yes"
         val NEGATIVE_BUTTON : String = "No"
         val LAYOUT_ID : Int = R.layout.confirm_load_path_dialog
         val CONTENT : String = "Load $setName?"

    }

    data class EnableLocationDialog(var isLocationCurrentlyEnabled : Boolean = false) : DialogTypes() {
         val POSITIVE_BUTTON_LOCATION_ENABLED : String = "Disable"
         val POSITIVE_BUTTON_LOCATION_DISABLED : String = "Enable"
         val NEGATIVE_BUTTON : String = "Cancel"
         val CONTENT_LOCATION_ENABLED : String = "Disable location tracking?"
         val CONTENT_LOCATION_DISABLED : String = "Enable location tracking?"
         val LAYOUT_ID : Int = R.layout.location_dialog

    }

    object SaveSetDialog : DialogTypes() {
        const val POSITIVE_BUTTON : String = "Save"
        const val NEGATIVE_BUTTON : String = "Cancel"
        const val LAYOUT_ID : Int = R.layout.save_set_dialog
    }


}