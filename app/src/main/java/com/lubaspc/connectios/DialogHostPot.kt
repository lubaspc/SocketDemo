package com.lubaspc.connectios

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.lubaspc.connectios.databinding.HospotDialogBinding
import java.nio.charset.StandardCharsets


class DialogHostPot : DialogFragment() {

    private lateinit var red: Pair<String, String>

    fun setRed(red: Pair<String, String>): DialogHostPot {
        this.red = red
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = HospotDialogBinding.inflate(inflater, container, false).apply {
        val stringWifi = "WIFI:S:${red.first};T:WPA;P:${red.second};;"
        val size = 480
        val hints = mutableMapOf<EncodeHintType, Any>()
        if (!StandardCharsets.ISO_8859_1.newEncoder()
                .canEncode(stringWifi)
        ) hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8.name()
        val qrBits =
            MultiFormatWriter().encode(stringWifi, BarcodeFormat.QR_CODE, size, size, hints)
        qr.setImageBitmap(Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) for (y in 0 until size) {
                setPixel(x, y, if (qrBits.get(x, y)) Color.BLACK else Color.WHITE)
            }
        })
        tv.text =
            Html.fromHtml("<b>SSDI:</b> ${red.first}<br><b>PASS:</b> ${red.second}<br>")
    }.root

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, this::class.simpleName)
    }
}