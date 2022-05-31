package com.lubaspc.connectios

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.lubaspc.connectios.databinding.ActivityMainBinding
import com.lubaspc.connectios.websocket.WSInterface
import com.lubaspc.connectios.websocket.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


 class MainActivity : AppCompatActivity(), WSInterface.Server {

    private lateinit var vBind: ActivityMainBinding
    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val clipboardManager by lazy { applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private lateinit var registerOpenWifi: ActivityResultLauncher<Intent>
    private lateinit var registerPermissionHospot: ActivityResultLauncher<Array<String>>
    private lateinit var registerPermissionCamera: ActivityResultLauncher<Array<String>>

    private val newMessage = MutableLiveData<String>()
    private val stateApp = MutableLiveData<Int>()
    private val wifiConfig = MutableLiveData<Pair<String, String>>()
    private val adapterString by lazy { Adapter() }

    private var ws: WebSocket? = null

    private val dialogQr by lazy { DialogHostPot() }
    private val dialogScan by lazy {
        ScannerQrDialog()
            .setOnText {
                val dataWifi = it.getDataWifi()
                if (dataWifi != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("key", dataWifi.second))
                    lifecycleScope.launch(Dispatchers.Main) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Connectar WIFI")
                            .setMessage(
                                "Porfabor contectate a la red ${dataWifi.first}, para poder continuar\n" +
                                        "la contraseña es: ${dataWifi.second} ya tiene guardarda en el portapeles listo para que la puedas pegar"
                            )
                            .setPositiveButton("Aceptar") { _, _ ->
                                registerOpenWifi.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                            }
                            .setCancelable(false)
                            .show()

                    }
                    return@setOnText
                }
                Toast.makeText(this, "Error en el QR", Toast.LENGTH_SHORT).show()
            }
    }

    private val localOnlyHotspotCallback by lazy {
        object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                super.onStarted(reservation)
                try {
                    wifiConfig.postValue(
                        (reservation?.wifiConfiguration?.SSID ?: throw Exception()) to
                                (reservation.wifiConfiguration?.preSharedKey ?: throw Exception()),
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al generar el hostpot ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Desconectado"
        vBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vBind.root)
        vBind.rv.adapter = adapterString
        vBind.btnServer.setOnClickListener {
            if (ws != null) {
                ws?.disconnect()
                ws = null
                vBind.til.isEnabled = false
                title = "Desconectado"
                return@setOnClickListener
            }
            registerPermissionHospot.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                )
            )
        }
        vBind.btnClient.setOnClickListener {
            if (ws != null) {
                ws?.disconnect()
                ws = null
                vBind.til.isEnabled = false
                title = "Desconectado"
                return@setOnClickListener
            }
            registerPermissionCamera.launch(arrayOf(Manifest.permission.CAMERA))
        }
        vBind.til.setEndIconOnClickListener {
            if (!vBind.tiet.text.isNullOrEmpty())
                ws?.sendMessage(vBind.tiet.text.toString())
        }
        wifiConfig.observe(this) {
            ws = WebSocket.connServer(this)
            dialogQr.setRed(it).show(supportFragmentManager)
        }
        newMessage.observe(this, adapterString::addMessage)
    }

    override fun onStart() {
        super.onStart()
        registerPermissionHospot =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (!it.all { p -> p.value }) finish()
                else enableWifiAp()
            }

        registerPermissionCamera =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (!it.all { p -> p.value }) finish()
                else dialogScan.show(supportFragmentManager)
            }

        registerOpenWifi =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val apiGateway = wifiManager.dhcpInfo.gateway
                Log.d("connectWifi_IP", "SSDI: ${wifiManager.connectionInfo.ssid}"+wifiManager.dhcpInfo.gateway.toString())

                if (apiGateway != 0) {
                    ws = WebSocket.connClient(apiGateway.toIp(), this)
                    return@registerForActivityResult
                }
                AlertDialog.Builder(this)
                    .setMessage("Es nesario que te conectes al wifi que se te indico")
                    .show()
                Log.d("connectWifi_IP", wifiManager.dhcpInfo.gateway.toIp())
            }
    }

    override fun newClient(conn: String?) {
        vBind.tv.text = conn
    }

    override fun onMessage(origin: String, message: String?) {
        newMessage.postValue("$origin: $message")
    }

    override fun onSuccessConnection() {
        lifecycleScope.launch(Dispatchers.Main) {
            this@MainActivity.title = ws?.typeConnection?.name
            vBind.til.isEnabled = true
            Toast.makeText(
                this@MainActivity,
                "On Ready ${ws?.typeConnection?.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onError(ex: java.lang.Exception) {
        if (ws?.typeConnection == WebSocket.TypeConnection.CLIENT && ex is java.net.ConnectException) {
            lifecycleScope.launch(Dispatchers.Main) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Por favor verifica que tu coneccion este correcta, y aceptar seguir conectado aun que no tenga internet")
                    .setPositiveButton("Reintentar") { _, _ ->
                        ws?.reconnect()
                    }.setCancelable(false)
                    .show()
            }
        }
        ex.printStackTrace()
    }

    override fun onClose(reason: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                this@MainActivity,
                "On Close $reason ${ws?.typeConnection?.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableWifiAp() {
        try {
            wifiManager.startLocalOnlyHotspot(localOnlyHotspotCallback, Handler())
        } catch (e: IllegalStateException) {
            if (wifiConfig.value != null) {
                dialogQr.show(supportFragmentManager)
            } else Toast.makeText(this, "Error al generar la red", Toast.LENGTH_SHORT).show()
        }
    }


}