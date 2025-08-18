package com.twojafirma.askier.util

import com.twojafirma.askier.ui.data.TournamentDetails
import com.twojafirma.askier.ui.data.TournamentPairResult
import com.twojafirma.askier.ui.data.TournamentPlayerInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class TournamentHtmlParser {

    fun parseTournamentDetails(htmlContent: String): TournamentDetails {
        val document = Jsoup.parse(htmlContent)

        // TODO: Implement parsing logic for tournament title, date, center count

        val pairResults = mutableListOf<TournamentPairResult>()
        val tableRows = document.select("table tr") // Select all table rows

        // TODO: Iterate through tableRows (skipping header) to parse pair results

        // Placeholder data for now
        val tournamentTitle = document.selectFirst("div#logoh h1")?.text() ?: "N/A"
        val tournamentDate = document.selectFirst("div#logoh h4.i")?.text() ?: "N/A"
        val tournamentCenterCount = document.select("div#logoh h4").getOrNull(1)?.text()?.replace("Wyniki z ", "")?.replace(" ośrodków", "") ?: "N/A"


        // Dummy implementation for pair results - will be replaced by actual parsing
        if (tableRows.size > 1) { // Check if there are data rows beyond the header
            for (i in 1 until tableRows.size) { // Start from 1 to skip header row
                val row = tableRows[i]
                val columns = row.select("td")

                if (columns.size >= 9) { // Ensure all expected columns are present
                    val place = columns[0].text().toIntOrNull() ?: 0
                    val pairNumberText = columns[1].selectFirst("a")?.text()
                    val pairNumber = pairNumberText?.toIntOrNull() ?: 0

                    val playerNamesHtml = columns[2].html() // Get HTML to split <br />
                    val playerLinks = columns[2].select("a")
                    val names = playerNamesHtml.split("<br>").map { Jsoup.parse(it).text().trim() }

                    val wkHtml = columns[3].html().split("<br>")
                    val districtHtml = columns[4].html().split("<br>")


                    val player1Name = names.getOrElse(0) { "N/A" }
                    val player1Link = playerLinks.getOrNull(0)?.attr("href")
                    val player1CezarId = extractCezarId(player1Link)
                    val player1wk = wkHtml.getOrNull(0)?.trim()
                    val player1district = districtHtml.getOrNull(0)?.trim()?.replace("&nbsp;", "")


                    val player2Name = names.getOrElse(1) { "N/A" }
                    val player2Link = playerLinks.getOrNull(1)?.attr("href")
                    val player2CezarId = extractCezarId(player2Link)
                    val player2wk = wkHtml.getOrNull(1)?.trim()
                    val player2district = districtHtml.getOrNull(1)?.trim()?.replace("&nbsp;", "")


                    val player1Info = TournamentPlayerInfo(
                        name = player1Name,
                        cezarId = player1CezarId,
                        wk = player1wk,
                        district = player1district
                    )
                    val player2Info = TournamentPlayerInfo(
                        name = player2Name,
                        cezarId = player2CezarId,
                        wk = player2wk,
                        district = player2district
                    )

                    val clubCode = columns[5].selectFirst("a")?.text()
                    val resultPlusMinus = columns[6].text()
                    val imps = columns[7].text()
                    val pkl = columns[8].text()

                    pairResults.add(
                        TournamentPairResult(
                            place = place,
                            pairNumber = pairNumber,
                            player1 = player1Info,
                            player2 = player2Info,
                            clubCode = clubCode,
                            resultPlusMinus = resultPlusMinus,
                            imps = imps,
                            pkl = pkl
                        )
                    )
                }
            }
        }


        return TournamentDetails(
            tournamentTitle = tournamentTitle,
            tournamentDate = tournamentDate,
            tournamentCenterCount = tournamentCenterCount,
            pairResults = pairResults
        )
    }

    private fun extractCezarId(url: String?): Int? {
        if (url == null) return null
        // Example URL: http://www.msc.com.pl/cezar/?p=21&pid=4079
        return url.substringAfter("pid=", "").toIntOrNull()
    }
}
