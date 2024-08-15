package jp.tomo0611.sony_zv_e10.enum

enum class PacketType(var mValue: Int) {
    InvalidValue(0),
    InitCommandRequestPacket(1),
    InitCommandAck(2),
    InitEventRequestPacket(3),
    InitEventAckPacket(4),
    InitFailPacket(5),
    OperationRequestPacket(6),
    OperationResponsePacket(7),
    EventPacket(8),
    StartDataPacket(9),
    DataPacket(10),
    CancelPacket(11),
    EndDataPacket(12),
    ProbeRequestPacket(13),
    ProbeResponsePacket(14)
}