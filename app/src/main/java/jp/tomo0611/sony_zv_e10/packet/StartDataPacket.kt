package jp.tomo0611.sony_zv_e10.packet

import android.util.Log
import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer


class StartDataPacket(transactionId: Int, totalDataLength: Long) : AbstractPacket(12, PacketType.StartDataPacket) {
    val mTransactionId: Int
    val mTotalDataLength: Long

    init {
        this.mTransactionId = transactionId
        this.mTotalDataLength = totalDataLength
    }

    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(this.mTransactionId)
            bytes.putLong(this.mTotalDataLength)
            bytes.flip()
            return bytes
        }

    override fun toString(): String {
        return "StartDataPacket(transactionId=$mTransactionId, totalDataLength=$mTotalDataLength)"
    }
}