package jp.tomo0611.sony_zv_e10.client

import android.util.Log
import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.packet.AbstractPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandAckPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel

class PtpIpClient {
    companion object {
        private const val TAG = "PtpIpClient"
    }

    private val mSocketChannel = SocketChannel.open()

    init {
        mSocketChannel.socket().tcpNoDelay = true
        mSocketChannel.configureBlocking(false)
    }

    fun connect(ip: String, port: Int) {
        mSocketChannel.connect(InetSocketAddress(ip, port))
        // Wait for connection, avoid NotYetConnectedException
        while (!mSocketChannel.finishConnect()) {
            Thread.sleep(100)
        }
        Log.d(TAG, "Connected")
    }

    fun close() {
        mSocketChannel.close()
    }

    fun sendPacket(packet: AbstractPacket) {
        mSocketChannel.write(packet.bytes)
    }

    fun readPacket(): AbstractPacket {
        val readBuffer = ByteBuffer.allocate(8)
        readBuffer.order(ByteOrder.LITTLE_ENDIAN)
        while(mSocketChannel.read(readBuffer) == 0){
            Thread.sleep(100)
        }
        readBuffer.flip()
        if(readBuffer.array().contentEquals(byteArrayOf(0,0,0,0,0,0,0,0))){
            Log.d(TAG, "Failed to read packet")
            throw Exception("Failed to read packet")
        }

        val length = readBuffer.int

        // Read the body of the packet
        val buffer = ByteBuffer.allocate(length-8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val type = PacketType.entries[readBuffer.int]
        while (buffer.hasRemaining()) {
            mSocketChannel.read(buffer)
        }
        buffer.flip()

        if(type == PacketType.InitCommandAck){
            Log.d(TAG, "Received InitCommandAckPacket")
            val packet = InitCommandAckPacket(length, buffer)
            Log.d(TAG, packet.toString())
            return packet
        } else {
            Log.d(TAG, "Received packet of type $type")

            val sb = StringBuilder()
            for (b in readBuffer.array()) {
                sb.append(String.format("%02X ", b))
            }
            sb.append("\n")
            for (b in buffer.array()) {
                sb.append(String.format("%02X ", b))
            }
            sb.append("\n\n")

            Log.d(TAG, sb.toString())
            throw Exception("Received packet of type $type")
        }
    }
}