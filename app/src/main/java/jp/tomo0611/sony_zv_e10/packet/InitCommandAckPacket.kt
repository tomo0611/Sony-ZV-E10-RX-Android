package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.UUID


class InitCommandAckPacket(length: Int, byteBuffer: ByteBuffer) : AbstractPacket(length, PacketType.InitCommandAck) {

    val mConnectionNumber: Int
    val mUuid: UUID
    val mFriendlyName: String
    val mProtocolVersion: Int

    init {
        this.mConnectionNumber = byteBuffer.getInt()
        val bArr = ByteArray(16)
        byteBuffer[bArr]
        val wrap = ByteBuffer.wrap(bArr)
        wrap.order(ByteOrder.LITTLE_ENDIAN)
        val uuid = UUID(wrap.getLong(), wrap.getLong())
        this.mUuid = uuid
        val arrayList: ArrayList<Char> = ArrayList()
        while (true) {
            val c = byteBuffer.getChar()
            if (c.code == 0) {
                break
            } else {
                arrayList.add(Character.valueOf(c))
            }
        }
        val array = arrayList.toArray()
        val cArr2 = CharArray(array.size)
        for (i4 in array.indices) {
            cArr2[i4] = (array[i4] as Char)
        }
        val friendlyName = String(cArr2, 0, cArr2.size)

        this.mFriendlyName = friendlyName
        this.mProtocolVersion = byteBuffer.getInt()
    }

    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(mConnectionNumber)
            val uuidBytes = ByteBuffer.allocate(16)
            uuidBytes.order(ByteOrder.LITTLE_ENDIAN)
            uuidBytes.putLong(mUuid.mostSignificantBits)
            uuidBytes.putLong(mUuid.leastSignificantBits)
            bytes.put(uuidBytes.array())
            val strBytes = mFriendlyName.toByteArray(Charset.forName("UTF-16LE"))
            bytes.put(strBytes)
            bytes.putInt(mProtocolVersion)
            bytes.flip()
            return bytes
        }

    override fun toString(): String {
        return "InitCommandAckPacket(mConnectionNumber=$mConnectionNumber, mUuid=$mUuid, mFriendlyName='$mFriendlyName', mProtocolVersion=$mProtocolVersion)"
    }
}