package jp.tomo0611.sony_zv_e10.enum

enum class DataPhaseInfo(var mValue: Int) {
    UnknownDataPhase(0),
    NoDataOrDataInPhase(1),
    DataOutPhase(2)
}