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
import jp.tomo0611.sony_zv_e10.enum.DataPhaseInfo
import jp.tomo0611.sony_zv_e10.enum.FunctionMode
import jp.tomo0611.sony_zv_e10.enum.ObjectFormatCode
import jp.tomo0611.sony_zv_e10.enum.OperationCode
import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.packet.AbstractPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandAckPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandRequestPacket
import jp.tomo0611.sony_zv_e10.packet.InitEventAckPacket
import jp.tomo0611.sony_zv_e10.packet.InitEventRequestPacket
import jp.tomo0611.sony_zv_e10.packet.OperationRequestPacket
import jp.tomo0611.sony_zv_e10.packet.OperationResponsePacket
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
                var isEvent = false
                var transactionId = 0

                try {
                    var client = PtpIpClient()
                    client.connect("192.168.122.1", 15740)
                    client.sendPacket(InitCommandRequestPacket(UUID.randomUUID(), "Pixel 8"))
                    while(true) {
                        try {
                            var packet: AbstractPacket
                            if (isEvent) {
                                packet = client.readPacketFromEvent()
                            } else {
                                packet = client.readPacket()
                            }
                            if (packet is InitCommandAckPacket) {
                                str.append(packet.toString() + "\n")
                                client.sendPacketToEvent(InitEventRequestPacket(packet.mConnectionNumber))
                                isEvent = true
                            } else if (packet is InitEventAckPacket) {
                                isEvent = false
                                client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_OpenSession, transactionId, intArrayOf(1, FunctionMode.CONTENTS_TRANSFER_MODE.value)))
                            } else if(packet is OperationResponsePacket){
                                transactionId++
                                if(transactionId == 1){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_Connect, transactionId, intArrayOf(1,0,0)))
                                } else if(transactionId == 2){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_Connect, transactionId, intArrayOf(2,0,0)))
                                } else if(transactionId == 3){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetObjectHandles, transactionId, intArrayOf(0xf10001, 0x0, 0x10)))
                                    //client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_GetExtDeviceInfo, transactionId, intArrayOf(0x12c)))
                                } else if(transactionId == 4){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetObjectPropList, transactionId, intArrayOf(0x21, 0x0, 0x0, 0x10, 0x0)))
                                    //client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_Connect, transactionId, intArrayOf(3,0,0)))
                                } else if(transactionId == 5){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetObjectPropValue, transactionId, intArrayOf(0x21, 0xd8b1)))
                                    //client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.SDIO_GetAllExtDevicePropInfo, transactionId, intArrayOf()))
                                } else if(transactionId == 6){
                                    break
                                    //client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetDeviceInfo, transactionId, intArrayOf()))
                                } else if(transactionId == 7){
                                    //client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetObjectHandles, transactionId, intArrayOf(0xf10001, 0x0, 0x10)))
                                    //val data = client.sendReceiveOperationRequestPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetStorageIDs, transactionId, intArrayOf()))
                                    //val sb = StringBuilder()
                                    //for (b in data) {
                                    //    sb.append(String.format("%02X ", b))
                                    //}
                                    //Log.d("sendReceiveOperationRequestPacket#StorageIds", sb.toString())
                                } else if(transactionId == 8){
                                    client.sendPacket(OperationRequestPacket(DataPhaseInfo.NoDataOrDataInPhase, OperationCode.GetObjectPropsSupported, transactionId, intArrayOf(ObjectFormatCode.PTP_OFC_ASSOCIATION.code)))
                                } else {
                                    break
                                }
                            } else {
                                Log.d("MainActivity", packet.toString())
                                str.append(packet.toString() + "\n")
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