package jp.tomo0611.sony_zv_e10.client

import android.util.Log
import jp.tomo0611.sony_zv_e10.*
import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.enum.ResponseCode
import jp.tomo0611.sony_zv_e10.packet.AbstractPacket
import jp.tomo0611.sony_zv_e10.packet.DataPacket
import jp.tomo0611.sony_zv_e10.packet.EndDataPacket
import jp.tomo0611.sony_zv_e10.packet.InitCommandAckPacket
import jp.tomo0611.sony_zv_e10.packet.InitEventAckPacket
import jp.tomo0611.sony_zv_e10.packet.OperationRequestPacket
import jp.tomo0611.sony_zv_e10.packet.OperationResponsePacket
import jp.tomo0611.sony_zv_e10.packet.StartDataPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.file.Files


class PtpIpClient {
    companion object {
        private const val TAG = "PtpIpClient"
    }

    private lateinit var mSocketClient: SocketChannel
    private lateinit var mSocketClientForEvent: SocketChannel

    fun connect(ip: String, port: Int) {
        mSocketClient = SocketChannel.open()
        mSocketClient.connect(InetSocketAddress(ip, port))

        mSocketClientForEvent = SocketChannel.open()
        mSocketClientForEvent.connect(InetSocketAddress(ip, port))
        Log.d(TAG, "Connected")
    }

    fun close() {
        mSocketClient.close()
        mSocketClientForEvent.close()
        Log.d(TAG, "Closed")
    }

    fun sendPacketToEvent(packet: AbstractPacket) {
        Log.d(TAG, "Sending ${packet}")
        val sb = StringBuilder()
        for (b in packet.bytes.array()) {
            sb.append(String.format("%02X ", b))
        }
        Log.d(TAG, sb.toString())
        mSocketClientForEvent.write(packet.bytes)
    }

    fun sendPacket(packet: AbstractPacket) {
        Log.d(TAG, "Sending ${packet}")
        val sb = StringBuilder()
        for (b in packet.bytes.array()) {
            sb.append(String.format("%02X ", b))
        }
        Log.d(TAG, sb.toString())
        mSocketClient.write(packet.bytes)
    }

    fun sendReceiveOperationRequestPacket(packet: OperationRequestPacket): ByteBuffer {
        this.sendPacket(packet)
        // まずOperationResponse
        var p = this.readPacket()
        if(p is OperationResponsePacket){
            if(p.mResponseCode != ResponseCode.OK){
                throw Exception("OperationResponse is not OK. Returned ${p.mResponseCode.name}")
            }
        } else {
            throw Exception("Expected OperationResponsePacket, Received ${p}")
        }
        p = this.readPacket()
        if(p !is StartDataPacket){
            throw Exception("Expected StartDataPacket")
        }
        val totalLength = p.mTotalDataLength
        val data = ByteBuffer.allocate(totalLength.toInt())
        while(true) {
            p = this.readPacket()
            if(p is DataPacket){
                data.put(p.mData)
            } else if(p is EndDataPacket){
                data.put(p.mData)
                break
            } else {
                throw Exception("Received Unexpected PacketType")
            }
        }
        data.flip()
        return data
    }

    fun readPacket(): AbstractPacket {
        val readBuffer = ByteBuffer.allocate(8)
        readBuffer.order(ByteOrder.LITTLE_ENDIAN)
        while(mSocketClient.read(readBuffer) == 0){
            Thread.sleep(10)
        }
        readBuffer.flip()

        /*val sb = StringBuilder()
        for (b in readBuffer.array()) {
            sb.append(String.format("%02X ", b))
        }
        Log.d(TAG, sb.toString())*/

        if(readBuffer.array().contentEquals(byteArrayOf(0,0,0,0,0,0,0,0))){
            Log.d(TAG, "Failed to read packet")
            throw Exception("Failed to read packet")
        }

        val length = readBuffer.int

        // Read the body of the packet
        val buffer = ByteBuffer.allocate(length-8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val type = PacketType.entries[readBuffer.int]
        //Log.d(TAG, "Received packet of type $type")
        while (buffer.hasRemaining()) {
            mSocketClient.read(buffer)
        }
        buffer.flip()

        if(type == PacketType.InitCommandAck) {
            Log.d(TAG, "Received InitCommandAckPacket")
            val packet = InitCommandAckPacket(length, buffer)
            Log.d(TAG, packet.toString())
            return packet
        } else if(type == PacketType.InitEventAckPacket) {
            Log.d(TAG, "Received InitEventAckPacket")
            return InitEventAckPacket()
        } else if(type == PacketType.OperationResponsePacket) {
            Log.d(TAG, "Received OperationResponsePacket")
            val packet = OperationResponsePacket.valueOf(buffer)
            Log.d(TAG, packet.toString())
            return packet
        } else if(type == PacketType.StartDataPacket) {
            Log.d(TAG, "Received StartDataPacket")
            val transactionId = buffer.int
            val totalDataLength = buffer.long
            //Log.d(TAG, "TransactionId: $transactionId, TotalDataLength: $totalDataLength")
            return StartDataPacket(transactionId, totalDataLength)
        } else if(type == PacketType.DataPacket) {
            //Log.d(TAG, "Received DataPacket")
            val transactionId = buffer.int
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            //Log.d(TAG, "TransactionId: $transactionId, Data: ${data.size} bytes")
            return DataPacket(transactionId, data)
        } else if(type == PacketType.EndDataPacket) {
            Log.d(TAG, "Received EndDataPacket")
            val transactionId = buffer.int
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            //Log.d(TAG, "TransactionId: $transactionId")
            return EndDataPacket(transactionId, data)
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

    fun readPacketFromEvent(): AbstractPacket {
        val readBuffer = ByteBuffer.allocate(8)
        readBuffer.order(ByteOrder.LITTLE_ENDIAN)
        while(mSocketClientForEvent.read(readBuffer) == 0){
            Thread.sleep(10)
        }
        readBuffer.flip()

        val sb = StringBuilder()
        for (b in readBuffer.array()) {
            sb.append(String.format("%02X ", b))
        }
        Log.d(TAG, sb.toString())

        if(readBuffer.array().contentEquals(byteArrayOf(0,0,0,0,0,0,0,0))){
            Log.d(TAG, "Failed to read packet")
            throw Exception("Failed to read packet")
        }

        val length = readBuffer.int

        // Read the body of the packet
        val buffer = ByteBuffer.allocate(length-8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val type = PacketType.entries[readBuffer.int]
        Log.d(TAG, "Received packet of type $type")
        while (buffer.hasRemaining()) {
            mSocketClientForEvent.read(buffer)
        }
        buffer.flip()

        if(type == PacketType.InitCommandAck) {
            Log.d(TAG, "Received InitCommandAckPacket")
            val packet = InitCommandAckPacket(length, buffer)
            Log.d(TAG, packet.toString())
            return packet
        } else if(type == PacketType.InitEventAckPacket) {
            Log.d(TAG, "Received InitEventAckPacket")
            return InitEventAckPacket()
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