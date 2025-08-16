package com.twojafirma.askier.ui.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter fun fromSuit(suit: Suit): String = suit.name
    @TypeConverter fun toSuit(name: String): Suit = Suit.valueOf(name)

    @TypeConverter fun fromPlayer(player: Player): String = player.name
    @TypeConverter fun toPlayer(name: String): Player = Player.valueOf(name)

    @TypeConverter fun fromDouble(double: Double): String = double.name
    @TypeConverter fun toDouble(name: String): Double = Double.valueOf(name)

    @TypeConverter fun fromVulnerability(vulnerability: Vulnerability): String = vulnerability.name
    @TypeConverter fun toVulnerability(name: String): Vulnerability = Vulnerability.valueOf(name)

    @TypeConverter
    fun fromPlayersMap(players: Map<Player, PlayerProfile?>?): String {
        return Gson().toJson(players)
    }
    @TypeConverter
    fun toPlayersMap(json: String): Map<Player, PlayerProfile?>? {
        val type = object : TypeToken<Map<Player, PlayerProfile?>>() {}.type
        return Gson().fromJson(json, type)
    }
}

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val players: Map<Player, PlayerProfile?>,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deals")
data class DealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val level: Int,
    val suit: Suit,
    val declarer: Player,
    val double: Double,
    val tricks: Int,
    val vulnerability: Vulnerability,
    val score: Int,
    val nsHcp: Int,
    val ewHcp: Int,
    val imps: Int
)

data class SessionWithDeals(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val deals: List<DealEntity>
)