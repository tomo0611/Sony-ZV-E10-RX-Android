package jp.tomo0611.sony_zv_e10.packet

import androidx.annotation.CallSuper
import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class AbstractPacket(i: Int, enumPacketType: PacketType) {
    val mLength: Int = i + 4 + 4
    val mPacketType: PacketType = enumPacketType

    @get:CallSuper
    open val bytes: ByteBuffer
        get() {
            val allocate = ByteBuffer.allocate(this.mLength)
            allocate.order(ByteOrder.LITTLE_ENDIAN)
            allocate.putInt(this.mLength)
            allocate.putInt(mPacketType.mValue)
            return allocate
        }

    override fun toString(): String {
        return "[PACKET] " + this.mLength + ", " + this.mPacketType
    }
}