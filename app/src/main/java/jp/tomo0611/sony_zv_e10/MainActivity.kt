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
import jp.tomo0611.sony_zv_e10.client.PtpIpClient
import jp.tomo0611.sony_zv_e10.databinding.ActivityMainBinding
import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.packet.InitCommandAckPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandRequestPacket
import jp.tomo0611.sony_zv_e10.packet.InitEventRequestPacket
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

                val str = StringBuilder()

                try {
                    val client = PtpIpClient()
                    client.connect("192.168.122.1", 15740)
                    client.sendPacket(InitCommandRequestPacket(UUID.randomUUID(), "Pixel 8"))
                    while(true) {
                        try {
                            val packet = client.readPacket()
                            if (packet is InitCommandAckPacket) {
                                str.append(packet.toString() + "\n")
                                client.sendPacket(InitEventRequestPacket(packet.mConnectionNumber))
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                            break
                        }
                    }
                    client.close()
                    runOnUiThread {
                        findViewById<TextView>(R.id.textview_first).text = str.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        findViewById<TextView>(R.id.textview_first).text = e.stackTraceToString()
                    }
                }
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