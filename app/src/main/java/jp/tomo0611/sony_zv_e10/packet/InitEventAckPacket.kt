package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer

class InitEventAckPacket : AbstractPacket(8, PacketType.InitEventAckPacket) {
    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(8)
            bytes.putInt(PacketType.InitEventAckPacket.mValue)
            bytes.flip()
            return bytes
        }

    override fun toString(): String {
        return "InitEventAckPacket()"
    }
}