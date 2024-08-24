package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import jp.tomo0611.sony_zv_e10.enum.ResponseCode
import java.nio.ByteBuffer
import java.util.LinkedList


class OperationResponsePacket(
    enumResponseCode: ResponseCode,
    i: Int,
    linkedList: LinkedList<Int>
) :
    AbstractPacket((linkedList.size * 4) + 6, PacketType.OperationResponsePacket) {
    val mParameters: List<Int>
    val mResponseCode: ResponseCode
    val mTransactionId: Int

    init {
        this.mResponseCode = enumResponseCode
        this.mTransactionId = i
        this.mParameters = linkedList
    }

    companion object {
        fun valueOf(byteBuffer: ByteBuffer): OperationResponsePacket {
            var enumResponseCode: ResponseCode
            val i = byteBuffer.getShort().toInt()
            val values: Array<ResponseCode> = ResponseCode.entries.toTypedArray()
            val length = values.size
            var i2 = 0
            while (true) {
                if (i2 < length) {
                    enumResponseCode = values[i2]
                    if (enumResponseCode.mCode === i) {
                        break
                    }
                    i2++
                } else {
                    enumResponseCode = ResponseCode.Undefined
                    break
                }
            }
            val transactionId = byteBuffer.getInt()
            val linkedList: LinkedList<Int> = LinkedList<Int>()
            if (byteBuffer.remaining() != 0) {
                linkedList.add(byteBuffer.getInt())
            }
            if (byteBuffer.remaining() != 0) {
                linkedList.add(byteBuffer.getInt())
            }
            if (byteBuffer.remaining() != 0) {
                linkedList.add(byteBuffer.getInt())
            }
            if (byteBuffer.remaining() != 0) {
                linkedList.add(byteBuffer.getInt())
            }
            if (byteBuffer.remaining() != 0) {
                linkedList.add(byteBuffer.getInt())
            }
            return OperationResponsePacket(enumResponseCode, transactionId, linkedList)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("OperationResponsePacket{")
        sb.append("mResponseCode=").append(mResponseCode)
        sb.append(", mTransactionId=").append(mTransactionId)
        sb.append(", mParameters=").append(mParameters)
        sb.append('}')
        return sb.toString()
    }
}