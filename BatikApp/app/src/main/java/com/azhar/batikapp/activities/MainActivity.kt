package com.azhar.batikapp.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.azhar.batikapp.R
import com.azhar.batikapp.adapter.MainAdapter
import com.azhar.batikapp.adapter.MainAdapter.onSelectData
import com.azhar.batikapp.adapter.SliderImageAdapter
import com.azhar.batikapp.model.ModelMain
import com.azhar.batikapp.model.ModelSlide
import com.azhar.batikapp.networking.ApiEndpoint
import com.azhar.batikapp.service.GetAddressIntentService
import com.google.android.gms.location.*
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.slider_imageview.*
import kotlinx.android.synthetic.main.toolbar_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), onSelectData {

    private var progressDialog: ProgressDialog? = null
    private var mainAdapter: MainAdapter? = null
    private var modelMain: MutableList<ModelMain> = ArrayList()
    private val modelSlide: MutableList<ModelSlide> = ArrayList()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        addressResultReceiver = LocationAddressResultReceiver(Handler())
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Mohon Tunggu")
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Sedang menampilkan data")

        searchBatik.setQueryHint("Cari Apa?")
        searchBatik.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                setSearchBatik(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") getAllBatik()
                return false
            }
        })

        val searchPlateId = searchBatik.context.resources
                .getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchBatik.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        mainAdapter = MainAdapter(this, modelMain, this)

        rvAllBatik.setHasFixedSize(true)
        rvAllBatik.layoutManager = LinearLayoutManager(this)
        rvAllBatik.adapter = mainAdapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                getAddress()
            }
        }

        startLocationUpdates()

        //method get slide
        getSlideData()

        //method get data
        getAllBatik()
    }

    private fun setSearchBatik(query: String) {
        progressDialog?.show()
        AndroidNetworking.get(ApiEndpoint.URL_SEARCH + query)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            progressDialog?.dismiss()

                            if (modelMain.isNotEmpty()) modelMain.clear()

                            val jsonArray = response.getJSONArray("hasil")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val dataApi = ModelMain()
                                dataApi.id = jsonObject.getInt("id")
                                dataApi.namaBatik = jsonObject.getString("nama_batik")
                                dataApi.daerahBatik = jsonObject.getString("daerah_batik")
                                dataApi.maknaBatik = jsonObject.getString("makna_batik")
                                dataApi.hargaRendah = jsonObject.getInt("harga_rendah")
                                dataApi.hargaTinggi = jsonObject.getInt("harga_tinggi")
                                dataApi.hitungView = jsonObject.getString("hitung_view")
                                dataApi.linkBatik = jsonObject.getString("link_batik")
                                modelMain.add(dataApi)
                            }
                            mainAdapter?.notifyDataSetChanged()

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        progressDialog?.dismiss()
                        Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun getSlideData() {
        AndroidNetworking.get(ApiEndpoint.BASEURL_POPULAR)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    override fun onResponse(response: JSONObject) {
                        try {
                            val jsonArray = response.getJSONArray("hasil")
                            for (y in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(y)
                                val mdlSlide = ModelSlide()
                                mdlSlide.namaBatik = jsonObject.getString("nama_batik")
                                mdlSlide.linkBatik = jsonObject.getString("link_batik")
                                modelSlide.add(mdlSlide)
                            }
                            setImgSlide()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gambar tidak ditemukan!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        Toast.makeText(this@MainActivity, "Error Slide", Toast.LENGTH_SHORT).show()
                    }
                })
    }


    private fun getAllBatik() {
        Log.d("debug", "get all batik")
        progressDialog?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL_ALL)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        Log.d("debug", "all batik response : $response")
                        try {
                            progressDialog?.dismiss()

                            val jsonArray = response.getJSONArray("hasil")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val dataApi = ModelMain()
                                dataApi.id = jsonObject.getInt("id")
                                dataApi.namaBatik = jsonObject.getString("nama_batik")
                                dataApi.daerahBatik = jsonObject.getString("daerah_batik")
                                dataApi.maknaBatik = jsonObject.getString("makna_batik")
                                dataApi.hargaRendah = jsonObject.getInt("harga_rendah")
                                dataApi.hargaTinggi = jsonObject.getInt("harga_tinggi")
                                dataApi.hitungView = jsonObject.getString("hitung_view")
                                dataApi.linkBatik = jsonObject.getString("link_batik")
                                modelMain.add(dataApi)
                            }
                            Log.d("debug", "data size ${modelMain.size}")
                            // notify adapter
                            mainAdapter?.notifyDataSetChanged()

                        } catch (e: JSONException) {
                            Log.e("error", "error ${e.localizedMessage}")
                            Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        Log.e("error", "error request: ${anError.localizedMessage}")
                        progressDialog?.dismiss()
                        Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                    }
                })
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun setImgSlide() {
        val sliderImageAdapter = SliderImageAdapter(this, modelSlide)
        sliderImageAdapter.count = modelSlide.size
        imgSlider.setSliderAdapter(sliderImageAdapter)
        imgSlider.setIndicatorAnimation(IndicatorAnimations.DROP)
        imgSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        imgSlider.indicatorSelectedColor = Color.WHITE
        imgSlider.indicatorUnselectedColor = getColor(R.color.colorAccent)
        imgSlider.startAutoCycle()
        imgSlider.setOnIndicatorClickListener { position -> imgSlider.currentPagePosition = position }
    }

    override fun onSelected(modelMain: ModelMain) {
        val intent = Intent(this@MainActivity, DetailBatikActivity::class.java)
        intent.putExtra("detailBatik", modelMain)
        startActivity(intent)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 2000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(this@MainActivity, "Can't find current address, ", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GetAddressIntentService::class.java)
        intent.putExtra("add_receiver", addressResultReceiver)
        intent.putExtra("add_location", currentLocation)
        startService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                getAddress()
            }
            if (resultCode == 1) {
                Toast.makeText(this@MainActivity, "Address not found, ", Toast.LENGTH_SHORT).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        tvLocation.text = currentAdd
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val winParams = window.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            window.attributes = winParams
        }
    }
}
