package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer

class InitEventRequestPacket(val mConnectionNumber: Int) :
    AbstractPacket(4, PacketType.InitEventRequestPacket) {
    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(this.mConnectionNumber)
            bytes.flip()
            return bytes
        }
}