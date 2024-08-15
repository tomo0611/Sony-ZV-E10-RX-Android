package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.UUID

class InitCommandRequestPacket(uuid: UUID, str: String) :
    AbstractPacket(str.toByteArray(sUtf16).size + 2 + 16 + 4, PacketType.InitCommandRequestPacket) {

    val mFriendlyName: String
    val mGuid: UUID
    val mProtocolVersion: Int

    init {
        this.mGuid = uuid
        this.mFriendlyName = str
        this.mProtocolVersion = 65536
    }

    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            val uuid = this.mGuid
            val wrap = ByteBuffer.wrap(ByteArray(16))
            wrap.order(ByteOrder.LITTLE_ENDIAN)
            wrap.putLong(uuid.mostSignificantBits)
            wrap.putLong(uuid.leastSignificantBits)
            bytes.put(wrap.array())
            bytes.put(mFriendlyName.toByteArray(sUtf16))
            bytes.putChar(0.toChar())
            bytes.putInt(this.mProtocolVersion)
            bytes.flip()
            return bytes
        }

    companion object {
        val sUtf16: Charset = Charset.forName("UTF-16LE")
    }
}