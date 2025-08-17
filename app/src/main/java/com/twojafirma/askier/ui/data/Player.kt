package com.twojafirma.askier.ui.data

data class PklHistoryEntry(
    val year: String,
    val pkl: String,
    val wk: String
)

data class PlayerProfile(
    val pid: Int,
    val name: String,
    val club: String,
    val city: String,
    val photoUrl: String? = null,
    val photoData: ByteArray? = null,
    val title: String? = null,
    val pklHistory: List<PklHistoryEntry> = emptyList(),
    val totalPkl: Float? = null,
    val currentWk: Float? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlayerProfile
        if (pid != other.pid) return false
        return true
    }
    override fun hashCode(): Int {
        return pid
    }
}