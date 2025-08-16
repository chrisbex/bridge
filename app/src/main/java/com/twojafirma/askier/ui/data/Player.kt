package com.twojafirma.askier.ui.data

// Klasa przechowująca dane o punktach w danym roku
data class PklHistoryEntry(
    val year: String,
    val pkl: String,
    val wk: String
)

// Rozbudowany profil zawodnika
data class PlayerProfile(
    val pid: Int,
    val name: String,
    val club: String,
    val city: String,
    val photoUrl: String? = null,
    val photoData: ByteArray? = null,
    // Nowe, opcjonalne pola na szczegółowe dane
    val title: String? = null, // Tytuł (np. Mistrz Krajowy)
    val pklHistory: List<PklHistoryEntry> = emptyList() // Historia punktów
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