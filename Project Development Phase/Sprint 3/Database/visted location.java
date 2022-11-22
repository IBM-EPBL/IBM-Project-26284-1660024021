package com.example.containmentzonealertingapplication
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log


class AddressReceiver(
    handler: Handler?, // for interface
    private val view: AddressView
) :
    ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultdata == null) {
            Log.d(LogTags.Address_TAG, "onReceiveResult: null resultData")
            return
        }

        // receive data from the FetchAddress
        var addressOutput = resultData.getString(GEO_ADDRESS)
        val listPosition = resultData.getInt(LIST_POSITION)
        if (addressOutput == null || resultCode == FetchAddress.getGeoFailure()) addressOutput =
            "no address available (tap to see in map)"
        Log.d(
            LogTags.Address_TAG,
            "onReceiveResult: address received = $addressOutput"
        )
        view.updateAddress(addressOutput, listPosition)
    }

    fun startAddressFetchService(
        activity: Activity,
        latitude: Double,
        longitude: Double,
        listPosition: Int
    ) {
        val location = Location("dummy-provider")
        location.latitude = latitude
        location.longitude = longitude
        val intent = Intent(activity.applicationContext, FetchAddress::class.java)
        intent.putExtra(GEO_LOCATION, location)
        intent.putExtra(GEO_RECEIVER, this)
        intent.putExtra(LIST_POSITION, listPosition)
        Log.d(
            LogTags.Address_TAG,
            "startAddressFetchService: starting address service for position = $listPosition"
        )
        activity.startService(intent)
    }

    interface AddressView {
        fun updateAddress(address: String?, listPosition: Int)
    }

    companion object {
        private const val GEO_ADDRESS = "geo_address"
        private const val GEO_LOCATION = "geo_location"
        private const val GEO_RECEIVER = "geo_receiver"
        private const val LIST_POSITION = "position@list"
    }
}