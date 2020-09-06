package com.azhar.batikapp.service

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import java.util.*

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class GetAddressIntentService : IntentService(IDENTIFIER) {

    private var addressResultReceiver: ResultReceiver? = null

    //handle the address request
    override fun onHandleIntent(intent: Intent?) {
        var msg = ""
        //get result receiver from intent
        addressResultReceiver = intent!!.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null) {
            return
        }
        val location = intent.getParcelableExtra<Location>("add_location")

        //send no location error to results receiver
        if (location == null) {
            msg = "No location, can't go further without location"
            sendResultsToReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1)
        } catch (ignored: Exception) {
        }
        if (addresses == null || addresses.size == 0) {
            msg = "No address found for the location"
            sendResultsToReceiver(1, msg)
        } else {
            val address = addresses[0]
            val addressDetails = StringBuffer()
            addressDetails.append(address.subAdminArea)
            //addressDetails.append("\n");

            /*addressDetails.append(address.getThoroughfare());
            addressDetails.append("\n");

            addressDetails.append("Locality: ");
            addressDetails.append(address.getLocality());
            addressDetails.append("\n");

            addressDetails.append("County: ");
            addressDetails.append(address.getFeatureName());
            addressDetails.append("\n");

            addressDetails.append("State: ");
            addressDetails.append(address.getAdminArea());
            addressDetails.append("\n");

            addressDetails.append("Country: ");
            addressDetails.append(address.getCountryName());
            addressDetails.append("\n");

            addressDetails.append("Postal Code: ");
            addressDetails.append(address.getPostaalCode());
            addressDetails.append("\n");*/
            sendResultsToReceiver(2, addressDetails.toString())
        }
    }

    //to send results to receiver in the source activity
    private fun sendResultsToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString("address_result", message)
        addressResultReceiver?.send(resultCode, bundle)
    }

    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
    }
}