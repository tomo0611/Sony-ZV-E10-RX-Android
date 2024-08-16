package jp.tomo0611.sony_zv_e10

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import jp.tomo0611.sony_zv_e10.databinding.ActivityMainBinding
import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.packet.InitCommandAckPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandRequestPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Thread{
                val socketChannel = SocketChannel.open()
                socketChannel.socket().tcpNoDelay = true
                socketChannel.configureBlocking(false)
                socketChannel.connect(InetSocketAddress("192.168.122.1", 15740))
                // Wait for connection, avoid NotYetConnectedException
                while (!socketChannel.finishConnect()) {
                    Thread.sleep(100)
                }
                Log.d("Sony ZV-E10 Socket", "Connected")
                socketChannel.write(InitCommandRequestPacket(UUID.randomUUID(), "Pixel 8").bytes)
                val readBuffer = ByteBuffer.allocate(8)
                readBuffer.order(ByteOrder.LITTLE_ENDIAN)
                while(socketChannel.read(readBuffer) == 0){
                    Thread.sleep(100)
                }
                readBuffer.flip()
                if(readBuffer.array().contentEquals(byteArrayOf(0,0,0,0,0,0,0,0))){
                    Log.d("Sony ZV-E10 RX # InitCommandAckPacket", "Failed")
                    return@Thread
                }
                var sb = StringBuilder()
                for (b in readBuffer.array()) {
                    sb.append(String.format("%02X ", b))
                }
                Log.d("Sony ZV-E10 RX # InitCommandAckPacket", sb.toString())

                val length = readBuffer.int
                Log.d("Sony ZV-E10 RX # InitCommandAckPacket", "Length: $length")
                val buffer = ByteBuffer.allocate(length-8)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                val type = PacketType.entries[readBuffer.int]
                while (buffer.hasRemaining()) {
                    socketChannel.read(buffer)
                }
                buffer.flip()
                sb = StringBuilder()
                if(type == PacketType.InitCommandAck){
                    Log.d("Sony ZV-E10 RX # InitCommandAckPacket", "Received")
                    val packet = InitCommandAckPacket(length, buffer)
                    Log.d("Sony ZV-E10 RX # InitCommandAckPacket", packet.toString())
                    for (b in readBuffer.array()) {
                        sb.append(String.format("%02X ", b))
                    }
                    sb.append("\n")
                    for (b in buffer.array()) {
                        sb.append(String.format("%02X ", b))
                    }
                    sb.append("\n\n")
                    sb.append(packet.toString())
                }

                Log.d("Sony ZV-E10 RX # InitCommandRequestPacket", sb.toString())
                runOnUiThread {
                    findViewById<TextView>(R.id.textview_first).text = sb.toString()
                }
                socketChannel.close()
            }.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}