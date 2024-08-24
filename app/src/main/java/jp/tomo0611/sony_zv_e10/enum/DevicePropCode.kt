package jp.tomo0611.sony_zv_e10.enum

enum class DevicePropCode(val mCode: Int, val mString: String) {
    Undefined(0, "Undefined"),
    WhiteBalance(20485, "White Balance"),
    FNumber(20487, "F-Number"),
    FocusMode(20490, "Focus Mode"),
    ExposureMeteringMode(20491, "Exposure Metering Mode"),
    FlashMode(20492, "Flash Mode"),
    ExposureProgramMode(20494, "Exposure Program Mode"),
    ExposureBiasCompensation(20496, "Exposure Bias Compensation"),
    StillCaptureMode(20499, "Still Capture Mode");

    override fun toString(): String {
        return this.mString
    }

    companion object {
        fun valueOf(i: Int): DevicePropCode {
            for (enumDevicePropCode in entries) {
                if (enumDevicePropCode.mCode == i) {
                    return enumDevicePropCode
                }
            }
            return Undefined
        }
    }
}