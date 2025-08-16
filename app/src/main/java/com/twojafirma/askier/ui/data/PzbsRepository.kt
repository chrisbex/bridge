package com.twojafirma.askier.ui.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class PzbsRepository {

    private val client = HttpClient(Android) {
        engine {
            sslManager = { httpsURLConnection ->
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                })
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                httpsURLConnection.sslSocketFactory = sslContext.socketFactory
                httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
        }
    }

    suspend fun searchPlayers(query: String): List<PlayerProfile> {
        if (query.length < 1) {
            return emptyList()
        }

        val isNumeric = query.toIntOrNull() != null
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())

        val url = "https://msc.com.pl/cezar/?p=21&pid_search=$encodedQuery"

        return try {
            val htmlContent = client.get(url).bodyAsText()
            val doc = Jsoup.parse(htmlContent)

            val isListPage = doc.body().text().contains("Znaleziono kilku zawodników")

            val players = if (isListPage) {
                parsePlayerListPage(doc)
            } else {
                parsePlayerProfilePage(doc)
            }

            players.map { player ->
                if (player.photoUrl != null) {
                    try {
                        val imageData: ByteArray = client.get(player.photoUrl).body()
                        player.copy(photoData = imageData)
                    } catch (e: Exception) {
                        player
                    }
                } else {
                    player
                }
            }
        } catch (e: Exception) {
            Log.e("PzbsRepository", "Błąd sieciowy lub parsowania.", e)
            emptyList()
        }
    }

    private fun parsePlayerListPage(doc: org.jsoup.nodes.Document): List<PlayerProfile> {
        val playerRows = doc.select("table[style*='width:750px'] tr").drop(1)
        return playerRows.mapNotNull { row ->
            try {
                val columns = row.select("td")
                if (columns.size >= 8) {
                    val pid = columns[1].text().trim().toIntOrNull()
                    val name = columns[2].text().trim()
                    val club = columns[7].text().trim()
                    if (pid != null) {
                        val photoUrl = "https://msc.com.pl/cezar1/fots/${pid}a.jpg"
                        PlayerProfile(pid, name, club, "", photoUrl)
                    } else null
                } else { null }
            } catch (e: Exception) { null }
        }
    }

    private fun parsePlayerProfilePage(doc: org.jsoup.nodes.Document): List<PlayerProfile> {
        return try {
            val name = doc.select("span[style*='font-size:28px']").first()?.text()?.trim()
            val pidText = doc.select("td[style*='width:120px'] > b").first()?.text()?.replace("PID:", "")?.trim()
            val pid = pidText?.toIntOrNull()
            val club = doc.select("span[style*='font-size: 18px'] a b").first()?.text()?.trim()
            if (pid != null && name != null) {
                val photoUrl = "https://msc.com.pl/cezar1/fots/${pid}a.jpg"
                listOf(PlayerProfile(pid, name, club ?: "", "", photoUrl))
            } else { emptyList() }
        } catch (e: Exception) { emptyList() }
    }
}