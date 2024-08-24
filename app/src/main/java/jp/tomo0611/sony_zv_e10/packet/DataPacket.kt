package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer


class DataPacket(transactionId: Int, data: ByteArray) :
    AbstractPacket(data.size + 4, PacketType.DataPacket) {
    val mData: ByteArray
    val mTransactionId: Int

    init {
        this.mTransactionId = transactionId
        this.mData = data
    }

    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(this.mTransactionId)
            bytes.put(this.mData)
            bytes.flip()
            return bytes
        }

    override fun toString(): String {
        val sb = StringBuilder()
        for (b in mData) {
            sb.append(String.format("%02X ", b))
        }
        return "DataPacket(transactionId=$mTransactionId, data=${sb})"
    }
}