package jp.tomo0611.sony_zv_e10.enum

enum class ObjectFormatCode(val code: Int, val string: String) {
    UNDEFINED(12288, "Undefined"),
    PTP_OFC_ASSOCIATION(12289, "Association"), // 0x3001
    PTP_OFC_EXIF_JPEG(14337, "JPEG"),
    PTP_OFC_SONY_MPO(45825, "3D"),
    PTP_OFC_SONY_RAW(45313, "RAW"),
    PTP_OFC_MPEG(12299, ""), // 0x300b
    MTP_OFC_MP4CONTAINER(47490, "MP4"), // 0xb982
    PTP_OFC_WAV(12296, ""),
    PTP_OFC_SONY_HEIF(45328, "HEIF");
}