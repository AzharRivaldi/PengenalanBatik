package com.azhar.batikapp.model

import java.io.Serializable

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

class ModelMain : Serializable {
    var id = 0
    var namaBatik: String? = null
    var daerahBatik: String? = null
    var maknaBatik: String? = null
    var hargaRendah = 0
    var hargaTinggi = 0
    var hitungView: String? = null
    var linkBatik: String? = null

}