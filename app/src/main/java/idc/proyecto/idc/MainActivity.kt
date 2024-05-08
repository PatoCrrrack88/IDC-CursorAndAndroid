package idc.proyecto.idc

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import idc.proyecto.idc.databinding.MainviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : ComponentActivity() {

    private lateinit var binding: MainviewBinding
    private lateinit var urlSetter: EditText
    private lateinit var boton: Button
    private lateinit var retry: Button

    private lateinit var socket : DatagramSocket
    private lateinit var ip: String
    private val port = 25565

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var sensorEventListener: SensorEventListener

    private var z: Float = 0f
    private var acc: Float = 0f
    private var tiempoActual: Long = 0
    private var tiempoPrevio: Long = 0
    private var t: Long = 0

    private var sensorRegistered = true
    private var hayQueEnviar = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**---------------------------------------------------------------------*/
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!


        var prevz = 0
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                z = event.values[2]
                if (hayQueEnviar){
                    CoroutineScope(Dispatchers.IO).launch {
                        val data = z
                        sendValues(data)
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        /**---------------------------------------------------------------------*/

        urlSetter = findViewById(R.id.urlSetter)
        boton = findViewById(R.id.boton)
        retry = findViewById(R.id.retry)

        socket = DatagramSocket()

        retry.setOnClickListener {
            hayQueEnviar = false
            boton.text = "CONECTAR"
        }

        boton.setOnClickListener {
            ip = "" + urlSetter.text
            hayQueEnviar = !hayQueEnviar
            if (boton.text == "PAUSAR") boton.text = "JUGAR"
            else boton.text = "PAUSAR"
        }
    }

    @Throws(Exception::class)
    fun sendValues(data: Float) {
        val buffer = ByteBuffer.allocate(4)
        buffer.putFloat(data)
        val dataBytes = buffer.array()
        val packet = DatagramPacket(dataBytes, dataBytes.size, InetAddress.getByName(ip), port)

        socket.send(packet)
    }
}