package jp.tomo0611.sony_zv_e10.packet

import jp.tomo0611.sony_zv_e10.enum.DataPhaseInfo
import jp.tomo0611.sony_zv_e10.enum.OperationCode
import jp.tomo0611.sony_zv_e10.enum.PacketType
import java.nio.ByteBuffer


class OperationRequestPacket(
    enumDataPhaseInfo: DataPhaseInfo,
    enumOperationCode: OperationCode,
    i: Int? = null,
    iArr: IntArray
) :
    AbstractPacket((iArr.size * 4) + 10, PacketType.OperationRequestPacket) {
    val mDataPhaseInfo: DataPhaseInfo
    val mOperationCode: OperationCode
    val mTransactionId: Int?
    val mParameters: IntArray

    init {
        this.mDataPhaseInfo = enumDataPhaseInfo
        this.mOperationCode = enumOperationCode
        this.mTransactionId = i
        this.mParameters = iArr
    }

    override val bytes: ByteBuffer
        get() {
            val bytes = super.bytes
            bytes.putInt(mDataPhaseInfo.mValue)
            bytes.putShort(mOperationCode.mCode.toShort())
            if(mTransactionId != null){
                bytes.putInt(mTransactionId)
            } else {
                bytes.putInt(0)
            }
            for (i in this.mParameters) {
                bytes.putInt(i)
            }
            bytes.flip()
            return bytes
        }

    override fun toString(): String {
        return "OperationRequestPacket(mDataPhaseInfo=$mDataPhaseInfo, mOperationCode=$mOperationCode, mTransactionId=$mTransactionId, mParameters=${mParameters.contentToString()})"
    }
}