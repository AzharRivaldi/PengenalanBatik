package com.azhar.batikapp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.azhar.batikapp.R
import com.azhar.batikapp.model.ModelMain
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_detail_batik.*
import java.text.NumberFormat
import java.util.*

class DetailBatikActivity : AppCompatActivity() {

    var NamaBatik: String? = null
    var AsalBatik: String? = null
    var DescBatik: String? = null
    var Cover: String? = null
    var HargaRendah = 0
    var HargaTinggi = 0
    var modelMain: ModelMain? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_batik)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        toolbar.setTitle("")
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        modelMain = intent.getSerializableExtra("detailBatik") as ModelMain
        if (modelMain != null) {
            NamaBatik = modelMain?.namaBatik
            AsalBatik = modelMain?.daerahBatik
            HargaRendah = modelMain!!.hargaRendah
            HargaTinggi = modelMain!!.hargaTinggi
            DescBatik = modelMain?.maknaBatik
            Cover = modelMain?.linkBatik

            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)

            tvTitle.setText("Nama Batik : $NamaBatik")
            tvAsalBatik.setText("Asal Batik : $AsalBatik")
            tvHargaRendah.setText(formatRupiah.format(HargaRendah.toDouble()))
            tvHargaTinggi.setText(formatRupiah.format(HargaTinggi.toDouble()))
            tvDescBatik.setText(DescBatik)

            Glide.with(this)
                    .load(Cover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgCover)
        }

        fabShare.setOnClickListener(View.OnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val subject = modelMain?.namaBatik
            val description = modelMain?.maknaBatik
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, subject + "\n\n" + description);
            startActivity(Intent.createChooser(shareIntent, "Bagikan dengan :"))
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
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