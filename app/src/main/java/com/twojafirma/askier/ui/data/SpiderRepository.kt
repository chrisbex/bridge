package com.twojafirma.askier.ui.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
// Potrzebne do sprawdzania typu węzła - nieużywane bezpośrednio w nowym kodzie, ale zostawiam, jeśli jest gdzieś indziej
// import org.jsoup.nodes.TextNode 

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.* // Dla bodyAsText()

data class TournamentData(
    val month: String,
    val day: String,
    val name: String,
    val url: String,
    val isPzbs: Boolean
)

class SpiderRepository {

    private val httpClient = HttpClient() // Domyślnie użyje silnika z classpath, np. AndroidClient

    suspend fun fetchTournamentLinks(): Result<List<TournamentData>> {
        return withContext(Dispatchers.IO) {
            try {
                val tournamentList = mutableListOf<TournamentData>()
                val doc = Jsoup.connect("https://bridgespider.com/").get()

                val contentBlock = doc.selectFirst("div#block-block-7 div.content")
                if (contentBlock == null) {
                    return@withContext Result.failure(Exception("Nie znaleziono bloku z kalendarzami (div#block-block-7 div.content)"))
                }

                var currentMonth = ""
                // Iteracja po elementach w głównym bloku zawartości (nagłówki miesięcy i kontenery tabel)
                for (child in contentBlock.children()) {
                    if (child.tagName().equals("h3", ignoreCase = true)) {
                        currentMonth = child.text()
                    } else if (child.hasClass("overflow-x-auto") && currentMonth.isNotEmpty()) {
                        val table = child.selectFirst("table.friendly-calendar")
                        val tbody = table?.selectFirst("tbody")
                        if (tbody == null) continue

                        val daysForColumns = arrayOfNulls<String>(7)

                        for (row in tbody.select("tr")) {
                            for ((colIndex, td) in row.select("td").withIndex()) {
                                if (colIndex >= 7) continue

                                val potentialDayInCell = td.ownText().trim()
                                if (potentialDayInCell.matches("\\d+".toRegex())) {
                                    daysForColumns[colIndex] = potentialDayInCell
                                }

                                td.select("a[href]").forEach { link ->
                                    val dayForTournament = daysForColumns[colIndex]

                                    if (dayForTournament != null) {
                                        val tournamentName = link.html()
                                        val tournamentUrl = link.absUrl("href")
                                        val isPzbs = td.selectFirst("img.tournament-logo") != null

                                        if (tournamentUrl.startsWith("https://r.bridgespider.com/")) {
                                            Log.d("SpiderRepo", "Adding: Day='${dayForTournament}', Month='${currentMonth}', Name='${tournamentName}'")
                                            tournamentList.add(
                                                TournamentData(
                                                    month = currentMonth,
                                                    day = dayForTournament,
                                                    name = tournamentName,
                                                    url = tournamentUrl,
                                                    isPzbs = isPzbs
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Result.success(tournamentList)
            } catch (e: Exception) {
                Log.e("SpiderRepository", "Error fetching tournament links", e)
                Result.failure(e)
            }
        }
    }

    suspend fun fetchHtmlContent(url: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SpiderRepository", "Fetching HTML from: $url")
                val htmlString: String = httpClient.get(url).bodyAsText()
                if (htmlString.isBlank()) {
                    Log.w("SpiderRepository", "Fetched HTML is blank for URL: $url")
                    Result.failure(Exception("Fetched HTML content is blank for URL: $url"))
                } else {
                    Log.d("SpiderRepository", "Successfully fetched HTML (first 100 chars): ${htmlString.take(100)}")
                    Result.success(htmlString)
                }
            } catch (e: Exception) {
                Log.e("SpiderRepository", "Error fetching HTML content from $url", e)
                Result.failure(e)
            }
        }
    }
}
