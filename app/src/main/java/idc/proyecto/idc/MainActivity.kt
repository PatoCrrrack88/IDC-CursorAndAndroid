package idc.proyecto.idc

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import idc.proyecto.idc.databinding.MainviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    private lateinit var binding: MainviewBinding
    private lateinit var urlSetter: EditText
    private lateinit var boton: Button

    private lateinit var socket : DatagramSocket
    private lateinit var ip: String
    private val port = 25565

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var sensorEventListener: SensorEventListener

    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    private var hayQueEnviar = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**---------------------------------------------------------------------*/
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        //sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {

                x = event.values[0]
                y = event.values[1]
                z = event.values[2]

                if (hayQueEnviar){
                    CoroutineScope(Dispatchers.IO).launch {
                        val data = floatArrayOf(x, y, z)
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

        socket = DatagramSocket()

        boton.setOnClickListener {
            ip = "" + urlSetter.text
            hayQueEnviar = !hayQueEnviar
            if (boton.text == "PAUSAR"){
                boton.text = "JUGAR"
                sensorManager.unregisterListener(sensorEventListener)
                CoroutineScope(Dispatchers.IO).launch {
                    val data = floatArrayOf(69f, 69f, 69f)
                    sendValues(data)
                }
            } else {
                boton.text = "PAUSAR"
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    @Throws(Exception::class)
    fun sendValues(data: FloatArray) {
        val size = 4 * data.size
        Log.d("print", "${size}, ${data.size}, ${data}")
        val buffer = ByteBuffer.allocate(size)

        for (d in data){
            buffer.putFloat(d)
            Log.d("inData", "${d}")
        }

        val dataBytes = buffer.array()
        val packet = DatagramPacket(dataBytes, dataBytes.size, InetAddress.getByName(ip), port)

        socket.send(packet)
    }
}