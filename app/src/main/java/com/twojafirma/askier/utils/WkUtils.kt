package com.twojafirma.askier.utils

// ... (data class WkProgress) ...
data class WkProgress(
    val progress: Float,             // Postęp od 0.0f do 1.0f
    val label: String,               // Etykieta, np. "150 PKL do 1.5 WK" lub "Najwyższy poziom WK!"
    val currentWkDisplay: String,    // Tekstowa reprezentacja obecnego progu WK gracza, np. "1.0 WK"
    val nextWkDisplay: String        // Tekstowa reprezentacja następnego progu WK, np. "1.5 WK"
)

object WkUtils {

    // Mapuje poziom WK na *CAŁKOWITĄ (skumulowaną)* liczbę punktów PKL wymaganą do jego osiągnięcia.
    // Musi być posortowane według WK (Float), aby poniższa logika działała poprawnie.
    private val wkPklThresholds: LinkedHashMap<Float, Int> = linkedMapOf(
        0.0f to 0,        // Baza: 0 WK wymaga 0 PKL.
        // --- Obliczenia na podstawie Twojej tabeli i założenia o przyrostowym PKL (norma) ---
        0.5f to 50,       // Kandydat (0 + 50)
        1.0f to 150,      // Adept (50 + 100)
        1.5f to 350,      // Mistrz Klubowy (150 + 200)
        2.0f to 850,      // Mistrz Okręgowy (350 + 500)
        2.5f to 1650,     // Mistrz Wojewódzki (850 + 800)
        3.0f to 2850,     // Mistrz Regionalny (1650 + 1200)
        4.0f to 4850,     // Mistrz Krajowy (szczebel 7) (2850 + 2000)
        5.0f to 7850,     // Mistrz Krajowy (szczebel 8) (4850 + 3000)
        // Dla wyższych poziomów, sumujemy odpowiednie normy PKL z tabeli:
        // WK 7.0f (szczebel 9) = 7850 (do 5.0) + 5000 (norma na 7.0) = 12850
        7.0f to 12850,    // Mistrz Międzynarodowy
        // WK 12.0f (szczebel 12) = 12850 (do 7.0)
        //                        + 8000 (norma na 9.0 z tabeli)
        //                        + 8000 (norma na 11.0 z tabeli)
        //                        + 10000 (norma na 12.0 z tabeli) = 38850
        12.0f to 38850,   // Arcymistrz
        // WK 18.0f (szczebel 16) = 38850 (do 12.0)
        //                        + 0 (norma PKL na 13.0 jest pusta, zakładamy 0 przyrostu PKL)
        //                        + 10000 (norma na 15.0 z tabeli)
        //                        + 15000 (norma na 17.0 z tabeli)
        //                        + 20000 (norma na 18.0 z tabeli) = 83850
        18.0f to 83850    // Arcymistrz Międzynarodowy
        // Można dodać więcej poziomów (np. 21.0f -> 103850), jeśli są potrzebne i zdefiniowane w Twojej aplikacji.
    )

    // Twoja funkcja calculateWkProgress(currentPlayerPkl: Float?, playerReportedWk: Float?)
    // powinna teraz działać poprawniej z tymi skumulowanymi progami.
    // ... (reszta Twojego kodu WkUtils.kt) ...
    fun calculateWkProgress(currentPlayerPkl: Float?, playerReportedWk: Float?): WkProgress {
        // Default values for invalid or missing data
        if (currentPlayerPkl == null || currentPlayerPkl < 0 || playerReportedWk == null || playerReportedWk < 0) {
            return WkProgress(0f, "Brak danych PKL/WK", "0.0 WK", wkPklThresholds.keys.firstOrNull { it > 0.0f }?.let { "${"%.1f".format(it)} WK" } ?: "0.5 WK")
        }

        var pklForCurrentDisplayRank: Int = 0
        var currentDisplayRankWk: Float = 0.0f

        val sortedThresholdsList = wkPklThresholds.entries.toList()

        for ((wkValue, pklNeeded) in sortedThresholdsList.reversed()) {
            if (playerReportedWk >= wkValue) {
                currentDisplayRankWk = wkValue
                pklForCurrentDisplayRank = pklNeeded
                break
            }
        }

        var nextTargetWk: Float? = null
        var pklForNextTargetWk: Int? = null

        for ((wkValue, pklNeeded) in sortedThresholdsList) {
            if (wkValue > currentDisplayRankWk) {
                nextTargetWk = wkValue
                pklForNextTargetWk = pklNeeded
                break
            }
        }

        val currentWkText = "${"%.1f".format(currentDisplayRankWk)} WK"

        if (nextTargetWk == null || pklForNextTargetWk == null) {
            val highestDefinedWkFromMap = sortedThresholdsList.lastOrNull()?.key ?: currentDisplayRankWk
            val pklForHighestRank = wkPklThresholds[highestDefinedWkFromMap] ?: Int.MAX_VALUE
            if (currentPlayerPkl >= pklForHighestRank && currentDisplayRankWk >= highestDefinedWkFromMap) {
                return WkProgress(1f, "Najwyższy poziom WK!", currentWkText, currentWkText)
            }
            val pklNeededForThisRank = wkPklThresholds[currentDisplayRankWk] ?: 0
            if (currentDisplayRankWk >= highestDefinedWkFromMap && currentPlayerPkl < pklNeededForThisRank) {
                val prog = if (pklNeededForThisRank > 0) (currentPlayerPkl.toFloat() / pklNeededForThisRank).coerceIn(0f, 1f) else 0f
                // Poprawka dla etykiety, gdy gracz jest na najwyższym zdefiniowanym poziomie, ale nie ma wystarczająco PKL
                val pklPointsStillNeededForThisRank = pklNeededForThisRank - currentPlayerPkl
                val labelForHighestNotReached = if (pklPointsStillNeededForThisRank > 0) {
                    "${"%.0f".format(pklPointsStillNeededForThisRank)} PKL do $currentWkText"
                } else {
                    "Cel: $currentWkText"
                }
                return WkProgress(prog, labelForHighestNotReached, currentWkText, currentWkText)
            }
            return WkProgress(1f, "Najwyższy poziom WK!", currentWkText, currentWkText)
        }

        val nextWkText = "${"%.1f".format(nextTargetWk)} WK"

        val pklRangeForProgress = (pklForNextTargetWk - pklForCurrentDisplayRank).toFloat()
        val pklAchievedInCurrentRange = currentPlayerPkl - pklForCurrentDisplayRank

        val progress = if (pklRangeForProgress <= 0) {
            if (currentPlayerPkl >= pklForNextTargetWk) 1f else 0f
        } else {
            (pklAchievedInCurrentRange / pklRangeForProgress).coerceIn(0f, 1f)
        }

        val label: String
        if (currentPlayerPkl >= pklForNextTargetWk) {
            label = "Osiągnięto $nextWkText!"
        } else {
            // PKL potrzebne do następnego progu (nextTargetWk) od zera, a nie od obecnego progu gracza (pklForCurrentDisplayRank)
            val pklPointsStillNeeded = pklForNextTargetWk - currentPlayerPkl
            label = if (pklPointsStillNeeded > 0) {
                "${"%.0f".format(pklPointsStillNeeded)} PKL do $nextWkText"
            } else {
                "Cel: $nextWkText"
            }
        }

        return WkProgress(progress, label, currentWkText, nextWkText)
    }
}

